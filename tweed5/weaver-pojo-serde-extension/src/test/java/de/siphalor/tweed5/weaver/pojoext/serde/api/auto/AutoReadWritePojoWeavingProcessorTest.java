package de.siphalor.tweed5.weaver.pojoext.serde.api.auto;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CollectionConfigEntry;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.NullableConfigEntry;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.DefaultWeavingExtensions;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.NullablePojoWeaver;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import lombok.Data;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@NullUnmarked
class AutoReadWritePojoWeavingProcessorTest {
	private ConfigContainer<AnnotatedConfig> container;
	private ReadWriteExtension readWriteExtension;

	@BeforeEach
	void setUp() {
		var bootstrapper = TweedPojoWeaverBootstrapper.create(AnnotatedConfig.class);

		container = bootstrapper.weave();
		container.initialize();

		readWriteExtension = container.extension(ReadWriteExtension.class).orElseThrow();
	}

	@SuppressWarnings("unchecked")
	@Test
	void testConfiguration() {
		var rootEntry = (CompoundConfigEntry<AnnotatedConfig>) container.rootEntry();
		assertReaderAndWriter(rootEntry, TweedEntryReaderWriterImpls.COMPOUND_READER_WRITER);

		var primitiveIntEntry = rootEntry.subEntries().get("primitiveInt");
		assertReaderAndWriter(primitiveIntEntry, TweedEntryReaderWriterImpls.INT_READER_WRITER);

		var boxedLongEntry = rootEntry.subEntries().get("boxedLong");
		assertReaderAndWriter(boxedLongEntry, TweedEntryReaderWriterImpls.NULLABLE_READER_WRITER);
		assertThat(boxedLongEntry).asInstanceOf(type(NullableConfigEntry.class))
				.satisfies(nullableEntry -> assertReaderAndWriter(
						nullableEntry.nonNullEntry(), TweedEntryReaderWriterImpls.LONG_READER_WRITER
				));

		var stringEntry = rootEntry.subEntries().get("string");
		assertReaderAndWriter(stringEntry, TweedEntryReaderWriterImpls.STRING_READER_WRITER);

		var nestedsEntry = (CollectionConfigEntry<Nested, List<Nested>>) rootEntry.subEntries().get("nesteds");
		assertReaderAndWriter(nestedsEntry, TweedEntryReaderWriterImpls.COLLECTION_READER_WRITER);

		var nestedEntry = (CompoundConfigEntry<Nested>) nestedsEntry.elementEntry();
		assertReaderAndWriter(nestedEntry, TweedEntryReaderWriterImpls.COMPOUND_READER_WRITER);

		var nestedValueEntry = nestedEntry.subEntries().get("value");
		assertReaderAndWriter(nestedValueEntry, TweedEntryReaderWriterImpls.BOOLEAN_READER_WRITER);
	}

	private void assertReaderAndWriter(ConfigEntry<?> entry, TweedEntryReaderWriter<?, ?> readerWriter) {
		assertThat(readWriteExtension.getDefinedEntryReader(entry)).isSameAs(readerWriter);
		assertThat(readWriteExtension.getDefinedEntryWriter(entry)).isSameAs(readerWriter);
	}

	@Test
	void testUsage() {
		var bootstrapper = TweedPojoWeaverBootstrapper.create(AnnotatedConfig.class);

		var container = bootstrapper.weave();
		container.initialize();

		AnnotatedConfig config = new AnnotatedConfig()
				.primitiveInt(123)
				.boxedLong(456L)
				.string("test")
				.nesteds(Arrays.asList(
						new Nested().value(true),
						new Nested().value(false),
						new Nested().value(true)
				));

		StringWriter writer = new StringWriter();

		container.rootEntry().apply(write(
				new HjsonWriter(writer, new HjsonWriter.Options()),
				config
		));

		assertThat(writer.toString()).isEqualTo("""
				{
				\tprimitiveInt: 123
				\tboxedLong: 456
				\tstring: test
				\tnesteds: [
				\t\t{
				\t\t\tvalue: true
				\t\t}
				\t\t{
				\t\t\tvalue: false
				\t\t}
				\t\t{
				\t\t\tvalue: true
				\t\t}
				\t]
				}
				""");
	}

	@PojoWeaving(extensions = ReadWriteExtension.class)
	@PojoWeavingExtension(NullablePojoWeaver.class)
	@DefaultWeavingExtensions
	@PojoWeavingExtension(AutoReadWritePojoWeavingProcessor.class)
	@DefaultReadWriteMappings
	@CompoundWeaving
	@Data
	public static class AnnotatedConfig {
		private int primitiveInt;
		private @Nullable Long boxedLong;
		private String string;
		private List<@CompoundWeaving Nested> nesteds;
	}

	@Data
	public static class Nested {
		boolean value;
	}
}

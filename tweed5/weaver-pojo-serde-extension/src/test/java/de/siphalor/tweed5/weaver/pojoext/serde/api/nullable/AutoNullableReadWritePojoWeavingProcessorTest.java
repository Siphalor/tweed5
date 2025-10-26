package de.siphalor.tweed5.weaver.pojoext.serde.api.nullable;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriteException;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.DefaultWeavingExtensions;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import de.siphalor.tweed5.weaver.pojoext.serde.api.ReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.AutoReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.DefaultReadWriteMappings;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@NullUnmarked
class AutoNullableReadWritePojoWeavingProcessorTest {
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
		assertNonNullableReaderWriter(rootEntry);

		var primitiveIntEntry = rootEntry.subEntries().get("primitiveInt");
		assertNonNullableReaderWriter(primitiveIntEntry);

		var boxedIntegerEntry = rootEntry.subEntries().get("boxedInteger");
		assertNonNullableReaderWriter(boxedIntegerEntry);

		var nullableBoxedIntegerEntry = rootEntry.subEntries().get("nullableBoxedInteger");
		assertNullableReaderWriter(nullableBoxedIntegerEntry);

		var nestedEntry = (CompoundConfigEntry<Nested>) rootEntry.subEntries().get("nested");
		assertNonNullableReaderWriter(nestedEntry);

		var nestedPrimitiveIntEntry = nestedEntry.subEntries().get("primitiveInt");
		assertNonNullableReaderWriter(nestedPrimitiveIntEntry);

		var nestedBoxedIntegerEntry = nestedEntry.subEntries().get("boxedInteger");
		assertNullableReaderWriter(nestedBoxedIntegerEntry);

		var nestedNonNullBoxedIntegerEntry = nestedEntry.subEntries().get("nonNullBoxedInteger");
		assertNonNullableReaderWriter(nestedNonNullBoxedIntegerEntry);
	}

	private void assertNullableReaderWriter(ConfigEntry<?> entry) {
		assertNullableReader(entry);
		assertNullableWriter(entry);
	}

	private void assertNonNullableReaderWriter(ConfigEntry<?> entry) {
		assertNonNullableReader(entry);
		assertNonNullableWriter(entry);
	}

	private void assertNullableReader(ConfigEntry<?> entry) {
		assertThat(readWriteExtension.getDefinedEntryReader(entry))
				.isInstanceOf(TweedEntryReaderWriterImpls.NullableReader.class);
	}

	private void assertNonNullableReader(ConfigEntry<?> entry) {
		assertThat(readWriteExtension.getDefinedEntryReader(entry))
				.isNotInstanceOf(TweedEntryReaderWriterImpls.NullableReader.class);
	}

	private void assertNullableWriter(ConfigEntry<?> entry) {
		assertThat(readWriteExtension.getDefinedEntryWriter(entry))
				.isInstanceOf(TweedEntryReaderWriterImpls.NullableWriter.class);
	}

	private void assertNonNullableWriter(ConfigEntry<?> entry) {
		assertThat(readWriteExtension.getDefinedEntryWriter(entry))
				.isNotInstanceOf(TweedEntryReaderWriterImpls.NullableWriter.class);
	}

	@SneakyThrows
	@Test
	void testUsage() {
		AnnotatedConfig config = new AnnotatedConfig()
				.primitiveInt(123)
				.boxedInteger(456)
				.nullableBoxedInteger(null)
				.nested(new Nested().primitiveInt(789).boxedInteger(null).nonNullBoxedInteger(654));

		StringWriter writer = new StringWriter();

		readWriteExtension.write(
				new HjsonWriter(writer, new HjsonWriter.Options()),
				config,
				container.rootEntry(),
				readWriteExtension.createReadWriteContextExtensionsData()
		);

		assertThat(writer.toString()).isEqualTo("""
				{
				\tprimitiveInt: 123
				\tboxedInteger: 456
				\tnullableBoxedInteger: null
				\tnested: {
				\t\tprimitiveInt: 789
				\t\tboxedInteger: null
				\t\tnonNullBoxedInteger: 654
				\t}
				}
				""");

		config.boxedInteger(null);
		assertThatThrownBy(() -> readWriteExtension.write(
				new HjsonWriter(new StringWriter(), new HjsonWriter.Options()),
				config,
				container.rootEntry(),
				readWriteExtension.createReadWriteContextExtensionsData()
		)).isInstanceOf(TweedEntryWriteException.class)
				.hasMessageContaining("at .boxedInteger");
	}

	@PojoWeaving(extensions = {ReadWriteExtension.class, PatherExtension.class})
	@DefaultWeavingExtensions
	@PojoWeavingExtension(AutoReadWritePojoWeavingProcessor.class)
	@PojoWeavingExtension(ReadWritePojoWeavingProcessor.class)
	@PojoWeavingExtension(AutoNullableReadWritePojoWeavingProcessor.class)
	@DefaultReadWriteMappings
	@CompoundWeaving
	@Data
	@NoArgsConstructor
	public static class AnnotatedConfig {
		private int primitiveInt;
		private Integer boxedInteger;
		private @Nullable Integer nullableBoxedInteger;
		private Nested nested;
	}

	@CompoundWeaving
	@AutoNullableReadWriteBehavior(defaultNullability = AutoReadWriteNullability.NULLABLE)
	@Data
	@NoArgsConstructor
	public static class Nested {
		private int primitiveInt;
		private Integer boxedInteger;
		private @NonNull Integer nonNullBoxedInteger;
	}
}

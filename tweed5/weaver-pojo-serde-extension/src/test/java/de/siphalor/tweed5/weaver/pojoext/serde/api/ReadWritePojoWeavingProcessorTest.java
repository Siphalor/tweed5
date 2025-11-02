package de.siphalor.tweed5.weaver.pojoext.serde.api;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.weaver.pojo.api.annotation.*;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.read;
import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.write;
import static org.assertj.core.api.Assertions.assertThat;

class ReadWritePojoWeavingProcessorTest {

	@Test
	@SneakyThrows
	void testAnnotated() {
		TweedPojoWeaverBootstrapper<AnnotatedConfig> weaverBootstrapper = TweedPojoWeaverBootstrapper.create(
				AnnotatedConfig.class);

		ConfigContainer<AnnotatedConfig> configContainer = weaverBootstrapper.weave();
		configContainer.initialize();

		AnnotatedConfig config = new AnnotatedConfig(123, "test", new TestClass(987));

		StringWriter stringWriter = new StringWriter();
		HjsonWriter hjsonWriter = new HjsonWriter(stringWriter, new HjsonWriter.Options());
		configContainer.rootEntry().apply(write(hjsonWriter, config));

		assertThat(stringWriter).hasToString("""
				{
				\tanInt: 123
				\ttext: test
				\ttest: my cool custom writer
				}
				""");

		HjsonReader reader = new HjsonReader(new HjsonLexer(new StringReader("""
						{
						\tanInt: 987
						\ttext: abdef
						\ttest: { inner: 29 }
						}"""
		)));
		assertThat(configContainer.rootEntry().call(read(reader)))
				.isEqualTo(new AnnotatedConfig(987, "abdef", new TestClass(29)));
	}

	@AutoService(TweedReaderWriterProvider.class)
	public static class TestWriterProvider implements TweedReaderWriterProvider {
		@Override
		public void provideReaderWriters(ProviderContext context) {
			context.registerWriterFactory("tweed5.test.dummy", delegates -> new TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>() {
				@Override
				public void write(
						TweedDataVisitor writer,
						@Nullable Object value,
						ConfigEntry<Object> entry,
						TweedWriteContext context
				) throws TweedDataWriteException {
					writer.visitString("my cool custom writer");
				}
			});
		}
	}

	@PojoWeaving
	@TweedExtension(ReadWriteExtension.class)
	@DefaultWeavingExtensions
	@PojoWeavingExtension(ReadWritePojoWeavingProcessor.class)
	@CompoundWeaving
	@EntryReadWriteConfig("tweed5.compound")
	// lombok
	@AllArgsConstructor
	@NoArgsConstructor
	@EqualsAndHashCode
	@ToString
	public static class AnnotatedConfig {
		@EntryReadWriteConfig("tweed5.integer")
		public int anInt;

		@EntryReadWriteConfig("tweed5.nullable(tweed5.string)")
		public String text;

		@EntryReadWriteConfig(writer = "tweed5.test.dummy", reader = "tweed5.compound")
		@CompoundWeaving
		public TestClass test;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@EqualsAndHashCode
	@ToString
	public static class TestClass {
		@EntryReadWriteConfig("tweed5.integer")
		public int inner;
	}
}

package de.siphalor.tweed5.weaver.pojoext.serde;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import de.siphalor.tweed5.weaver.pojoext.serde.api.EntryReadWriteConfig;
import de.siphalor.tweed5.weaver.pojoext.serde.api.ReadWritePojoPostProcessor;
import lombok.*;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class WeaverPojoSerdeExtensionTest {

	@Test
	@SneakyThrows
	void testAnnotated() {
		TweedPojoWeaverBootstrapper<AnnotatedConfig> weaverBootstrapper = TweedPojoWeaverBootstrapper.create(AnnotatedConfig.class);

		ConfigContainer<AnnotatedConfig> configContainer = weaverBootstrapper.weave();
		configContainer.initialize();

		ReadWriteExtension readWriteExtension = configContainer.extension(ReadWriteExtension.class).orElseThrow();

		AnnotatedConfig config = new AnnotatedConfig(123, "test", new TestClass(987));

		StringWriter stringWriter = new StringWriter();
		HjsonWriter hjsonWriter = new HjsonWriter(stringWriter, new HjsonWriter.Options());
		readWriteExtension.write(hjsonWriter, config, configContainer.rootEntry(), readWriteExtension.createReadWriteContextExtensionsData());

		assertThat(stringWriter).hasToString("{\n\tanInt: 123\n\ttext: test\n\ttest: my cool custom writer\n}\n");

		HjsonReader reader = new HjsonReader(new HjsonLexer(new StringReader(
				"{\n\tanInt: 987\n\ttext: abdef\n\ttest: { inner: 29 }\n}"
		)));
		assertThat(readWriteExtension.read(
				reader,
				configContainer.rootEntry(),
				readWriteExtension.createReadWriteContextExtensionsData()
		)).isEqualTo(new AnnotatedConfig(987, "abdef", new TestClass(29)));
	}

	@AutoService(TweedReaderWriterProvider.class)
	public static class TestWriterProvider implements TweedReaderWriterProvider {
		@Override
		public void provideReaderWriters(ProviderContext context) {
			context.registerWriterFactory("tweed5.test.dummy", delegates -> new TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>() {
				@Override
				public void write(
						@NonNull TweedDataVisitor writer,
						Object value,
						ConfigEntry<Object> entry,
						@NonNull TweedWriteContext context
				) throws TweedDataWriteException {
					writer.visitString("my cool custom writer");
				}
			});
		}
	}

	@PojoWeaving(extensions = ReadWriteExtension.class, postProcessors = ReadWritePojoPostProcessor.class)
	@CompoundWeaving
	@EntryReadWriteConfig("tweed5.compound")
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

package de.siphalor.tweed5.data.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.testutils.serde.json.JsonWriterTest;
import lombok.SneakyThrows;

import java.io.StringWriter;

class JacksonWriterTest implements JsonWriterTest {
	@Override
	@SneakyThrows
	public TweedDataWriter createPrettyJsonWriter(StringWriter stringWriter) {
		return new JacksonWriter(JsonFactory.builder()
				.build()
				.createGenerator(stringWriter)
				.setPrettyPrinter(new DefaultPrettyPrinter()
						.withSeparators(new Separators().withObjectFieldValueSpacing(Separators.Spacing.AFTER))
						.withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
				), JacksonWriter.CommentWriteMode.MAP_ENTRIES
		);
	}
}

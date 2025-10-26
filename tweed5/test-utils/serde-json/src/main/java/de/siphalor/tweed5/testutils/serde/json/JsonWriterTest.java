package de.siphalor.tweed5.testutils.serde.json;

import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataCommentDecoration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public interface JsonWriterTest {
	TweedDataWriter createPrettyJsonWriter(StringWriter stringWriter);

	@ParameterizedTest
	@CsvSource(ignoreLeadingAndTrailingWhitespace = false, textBlock = """
			123,"123"
			 abc def ," abc def "
			'line
			breaks
			',"line\\nbreaks\\n"
			"quotes","\\"quotes\\""
			""")
	@SneakyThrows
	default void jsonString(String string, String expected) {
		var stringWriter = new StringWriter();
		try (var writer = createPrettyJsonWriter(stringWriter)) {
			writer.visitString(string);
		}
		assertThat(stringWriter.toString()).isEqualTo(expected);
	}

	@Test
	@SneakyThrows
	default void jsonComplex() {
		var stringWriter = new StringWriter();
		try (var writer = createPrettyJsonWriter(stringWriter)) {
			writer.visitMapStart();

			writer.visitDecoration((TweedDataCommentDecoration) () -> "The first is the best!");
			writer.visitMapEntryKey("first");
			writer.visitListStart();
			writer.visitInt(123);
			writer.visitListStart();
			writer.visitBoolean(false);
			writer.visitListEnd();
			writer.visitListEnd();

			writer.visitDecoration((TweedDataCommentDecoration) () -> "Hello\nWorld");
			writer.visitDecoration((TweedDataCommentDecoration) () -> "!");
			writer.visitMapEntryKey("second");
			writer.visitMapStart();
			writer.visitMapEntryKey("nested");
			writer.visitDouble(12.34);
			writer.visitMapEnd();

			writer.visitMapEnd();
		}

		assertThat(stringWriter.toString()).isEqualToNormalizingNewlines("""
				{
				  "first__comment": "The first is the best!",
				  "first": [
				    123,
				    [
				      false
				    ]
				  ],
				  "second__comment": [
				    "Hello",
				    "World",
				    "!"
				  ],
				  "second": {
				    "nested": 12.34
				  }
				}""");
	}
}

package de.siphalor.tweed5.data.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataCommentDecoration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonWriterTest {
	@SneakyThrows
	@Test
	void object() {
		var stringWriter = new StringWriter();
		try (var generator = JsonFactory.builder().build().createGenerator(stringWriter)) {
			generator.setPrettyPrinter(
					new DefaultPrettyPrinter()
							.withSeparators(new Separators().withObjectFieldValueSpacing(Separators.Spacing.AFTER))
			);
			var writer = new JacksonWriter(generator, JacksonWriter.CommentWriteMode.MAP_ENTRIES);
			writer.visitMapStart();
			writer.visitDecoration((TweedDataCommentDecoration) () -> "Hello\nWorld");
			writer.visitDecoration((TweedDataCommentDecoration) () -> "!");
			writer.visitMapEntryKey("test");
			writer.visitInt(1234);
			writer.visitMapEnd();
		}

		assertThat(stringWriter.toString()).isEqualTo("""
				{
				  "test__comment": "Hello\\nWorld\\n!",
				  "test": 1234
				}""");
	}

	@SneakyThrows
	@Test
	void complex() {
		var stringWriter = new StringWriter();
		try (var generator = JsonFactory.builder().build().createGenerator(stringWriter)) {
			generator.setPrettyPrinter(
					new DefaultPrettyPrinter()
							.withSeparators(new Separators().withObjectFieldValueSpacing(Separators.Spacing.AFTER))
			);
			var writer = new JacksonWriter(generator, JacksonWriter.CommentWriteMode.MAP_ENTRIES);
			writer.visitMapStart();
			writer.visitMapEntryKey("first");
			writer.visitListStart();
			writer.visitInt(1);
			writer.visitDecoration((TweedDataCommentDecoration) () -> "not written");
			writer.visitInt(2);
			writer.visitListEnd();
			writer.visitDecoration((TweedDataCommentDecoration) () -> "second object");
			writer.visitMapEntryKey("second");
			writer.visitMapStart();
			writer.visitDecoration((TweedDataCommentDecoration) () -> "inner entry");
			writer.visitMapEntryKey("inner");
			writer.visitBoolean(true);
			writer.visitMapEnd();
			writer.visitMapEnd();
		}

		assertThat(stringWriter.toString()).isEqualTo("""
				{
				  "first": [ 1, 2 ],
				  "second__comment": "second object",
				  "second": {
				    "inner__comment": "inner entry",
				    "inner": true
				  }
				}""");
	}
}

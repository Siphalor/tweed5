package de.siphaolor.tweed5.data.gson;

import com.google.gson.GsonBuilder;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataCommentDecoration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class GsonWriterTest {

	@SneakyThrows
	@Test
	void complex() {
		var stringWriter = new StringWriter();
		var writer = new GsonWriter(new GsonBuilder().setPrettyPrinting().create().newJsonWriter(stringWriter));

		writer.visitMapStart();

		writer.visitMapEntryKey("first");
		writer.visitListStart();
		writer.visitInt(123);
		writer.visitListStart();
		writer.visitBoolean(false);
		writer.visitListEnd();
		writer.visitListEnd();

		writer.visitDecoration((TweedDataCommentDecoration) () -> "Hello");
		writer.visitDecoration((TweedDataCommentDecoration) () -> "World");
		writer.visitMapEntryKey("second");
		writer.visitMapStart();
		writer.visitMapEntryKey("nested");
		writer.visitDouble(12.34);
		writer.visitMapEnd();

		writer.visitMapEnd();

		assertThat(stringWriter.toString()).isEqualTo("""
				{
				  "first": [
				    123,
				    [
				      false
				    ]
				  ],
				  "second__comment": "Hello\\nWorld",
				  "second": {
				    "nested": 12.34
				  }
				}""");
	}
}

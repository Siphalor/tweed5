package de.siphalor.tweed5.testutils.serde.json;

import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public interface JsonReaderTest {
	TweedDataReader createJsonReader(String text);

	@Test
	@SneakyThrows
	default void complexJsonReadTest() {
		String text = """
				{
					"first": [
						[ 1 ]
					],
					"second": {
						"test": "Hello World!"
					}
				}
				""";

		try (var reader = createJsonReader(text)) {
			var token = reader.peekToken();
			assertThat(token.isMapStart()).isTrue();
			token = reader.readToken();
			assertThat(token.isMapStart()).isTrue();
			token = reader.readToken();
			assertThat(token.isMapEntryKey()).isTrue();
			assertThat(token.canReadAsString()).isTrue();
			assertThat(token.readAsString()).isEqualTo("first");
			token = reader.readToken();
			assertThat(token.isMapEntryValue()).isTrue();
			assertThat(token.isListStart()).isTrue();
			token = reader.readToken();
			assertThat(token.isMapEntryValue()).isFalse();
			assertThat(token.isListValue()).isTrue();
			assertThat(token.isListStart()).isTrue();
			token = reader.readToken();
			assertThat(token.isListValue()).isTrue();
			assertThat(token.canReadAsInt()).isTrue();
			assertThat(token.readAsInt()).isEqualTo(1);
			token = reader.readToken();
			assertThat(token.isListValue()).isTrue();
			assertThat(token.isListEnd()).isTrue();
			token = reader.readToken();
			assertThat(token.isListValue()).isFalse();
			assertThat(token.isMapEntryValue()).isTrue();
			assertThat(token.isListEnd()).isTrue();
			token = reader.readToken();
			assertThat(token.isMapEntryKey()).isTrue();
			assertThat(token.canReadAsString()).isTrue();
			assertThat(token.readAsString()).isEqualTo("second");
			token = reader.readToken();
			assertThat(token.isMapEntryValue()).isTrue();
			assertThat(token.isMapStart()).isTrue();
			token = reader.readToken();
			assertThat(token.isMapEntryValue()).isFalse();
			assertThat(token.isMapEntryKey()).isTrue();
			assertThat(token.canReadAsString()).isTrue();
			assertThat(token.readAsString()).isEqualTo("test");
			token = reader.readToken();
			assertThat(token.isMapEntryValue()).isTrue();
			assertThat(token.canReadAsString()).isTrue();
			assertThat(token.readAsString()).isEqualTo("Hello World!");
			token = reader.readToken();
			assertThat(token.isMapEnd()).isTrue();
			token = reader.readToken();
			assertThat(token.isMapEnd()).isTrue();
		}
	}
}

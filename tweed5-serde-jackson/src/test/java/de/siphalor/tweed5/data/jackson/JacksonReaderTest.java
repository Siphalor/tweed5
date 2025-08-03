package de.siphalor.tweed5.data.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonReaderTest {
	@SneakyThrows
	@Test
	void complex() {
		var inputStream = new ByteArrayInputStream("""
				{
					"first": [
						[ 1 ]
					],
					"second": {
						"test": "Hello World!"
					}
				}
				""".getBytes(StandardCharsets.UTF_8));
		try (var parser = JsonFactory.builder().enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION).build().createParser(inputStream)) {
			var reader = new JacksonReader(parser);

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

package de.siphalor.tweed5.testutils.serde.json;

import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import lombok.SneakyThrows;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

public interface JsonReaderTest {
	default Offset<Double> getDoublePrecision() {
		return Offset.offset(0.000000001D);
	}

	TweedDataReader createJsonReader(String text);

	@ParameterizedTest
	@CsvSource({
			"127,127",
			"-128,-128",
	})
	@SneakyThrows
	default void jsonByte(String input, byte expected) {
		try (var reader = createJsonReader(input)) {
			var token = reader.readToken();
			assertThat(token.canReadAsByte()).as("%s should be a valid byte", input).isTrue();
			assertThat(token.readAsByte()).isEqualTo(expected);
		}
	}

	@ParameterizedTest
	@CsvSource({
			"1.23e2,123",
			"120e-1,12",
	})
	@SneakyThrows
	default void jsonByteFloaty(String input, byte expected) {
		try (var reader = createJsonReader(input)) {
			var token = reader.readToken();
			assertThat(token.canReadAsByte()).as("%s should be a valid byte", input).isTrue();
			assertThat(token.readAsByte()).isEqualTo(expected);
		}
	}

	@ParameterizedTest
	@CsvSource({
			"128",
			"1.23",
			"-129",
			"1.23e3",
			"123E-1",
	})
	@SneakyThrows
	default void jsonByteIllegal(String input) {
		try (var reader = createJsonReader(input)) {
			var token = reader.readToken();
			assertThat(token.canReadAsByte()).as("%s should not be a valid byte", input).isFalse();
		}
	}

	@ParameterizedTest
	@CsvSource(value = {
			"123,123",
			"-132893892,-132893892",
	})
	@SneakyThrows
	default void jsonInt(String input, int expected) {
		try (var reader = createJsonReader(input)) {
			var token = reader.readToken();
			assertThat(token.canReadAsInt()).as("%s should be a valid int", input).isTrue();
			assertThat(token.readAsInt()).isEqualTo(expected);
		}
	}

	@ParameterizedTest
	@CsvSource(value = {
			"123e4,1230000",
			"9.87e3,9870",
			"45670E-1,4567",
			"-123.56E+5,-12356000",
	})
	@SneakyThrows
	default void jsonIntFloaty(String input, int expected) {
		try (var reader = createJsonReader(input)) {
			var token = reader.readToken();
			assertThat(token.canReadAsInt()).as("%s should be a valid int", input).isTrue();
			assertThat(token.readAsInt()).isEqualTo(expected);
		}
	}

	@ParameterizedTest
	@CsvSource({
			"123,123",
			"12.34,12.34",
			"123456789.123456789,123456789.123456789",
			"1234.057e8,123405700000",
			"987.654E-5,0.00987654",
	})
	@SneakyThrows
	default void jsonDouble(String input, double expected) {
		try (var reader = createJsonReader(input)) {
			var token = reader.readToken();
			assertThat(token.canReadAsDouble()).as("%s should be a valid double", input).isTrue();
			assertThat(token.readAsDouble()).isEqualTo(expected, getDoublePrecision());
		}
	}

	@ParameterizedTest
	@CsvSource({
			"123,123",
			"98765,98765"
	})
	@SneakyThrows
	default void jsonIntAsDouble(String input, double expected) {
		try (var reader = createJsonReader(input)) {
			var token = reader.readToken();
			assertThat(token.canReadAsDouble()).as("%s should be a valid double", input).isTrue();
			assertThat(token.readAsDouble()).isEqualTo(expected, getDoublePrecision());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"[]",
			"[\n\n]",
			"[     ]",
			"[\n\t\t]",
	})
	@SneakyThrows
	default void jsonEmptyArray(String input) {
		try (var reader = createJsonReader(input)) {
			TweedDataToken token;
			token = reader.readToken();
			assertThat(token.isListStart()).isTrue();
			token = reader.readToken();
			assertThat(token.isListEnd()).isTrue();
		}
	}

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

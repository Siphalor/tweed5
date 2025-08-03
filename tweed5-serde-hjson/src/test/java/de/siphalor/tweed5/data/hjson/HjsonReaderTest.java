package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import de.siphalor.tweed5.testutils.serde.json.JsonReaderTest;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class HjsonReaderTest implements JsonReaderTest {
	@Override
	public TweedDataReader createJsonReader(String text) {
		return new HjsonReader(new HjsonLexer(new StringReader(text)));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{test:abc\ncdef:123\na:true}",
			"\n{test: abc \ncdef:123,a\n:\ntrue\n}",
			"// \n{\n\ttest:abc\ncdef:123e0,a: true ,}",
	})
	@SneakyThrows
	void testObject(String input) {
		try (var reader = createJsonReader(input)) {
			TweedDataToken token;
			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapStart());

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapEntryKey());
			assertTrue(token.canReadAsString());
			assertEquals("test", assertDoesNotThrow(token::readAsString));
			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapEntryValue());
			assertTrue(token.canReadAsString());
			assertEquals("abc", assertDoesNotThrow(token::readAsString));

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapEntryKey());
			assertTrue(token.canReadAsString());
			assertEquals("cdef", assertDoesNotThrow(token::readAsString));
			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapEntryValue());
			assertTrue(token.canReadAsInt());
			assertEquals(123, assertDoesNotThrow(token::readAsInt));

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapEntryKey());
			assertTrue(token.canReadAsString());
			assertEquals("a", assertDoesNotThrow(token::readAsString));
			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapEntryValue());
			assertTrue(token.canReadAsBoolean());
			assertEquals(true, assertDoesNotThrow(token::readAsBoolean));

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isMapEnd());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"[12,34,56]",
			"[12\n34\n\t56]",
			"[\n12\n\t\t34\n\t56\n]",
			"[\n12,34\n\t56\n]",
	})
	@SneakyThrows
	void testArray(String input) {
		try (var reader = createJsonReader(input)) {
			TweedDataToken token;
			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isListStart());

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isListValue());
			assertTrue(token.canReadAsInt());
			assertEquals(12, assertDoesNotThrow(token::readAsInt));

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isListValue());
			assertTrue(token.canReadAsInt());
			assertEquals(34, assertDoesNotThrow(token::readAsInt));

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isListValue());
			assertTrue(token.canReadAsInt());
			assertEquals(56, assertDoesNotThrow(token::readAsInt));

			token = assertDoesNotThrow(reader::readToken);
			assertTrue(token.isListEnd());
		}
	}
}

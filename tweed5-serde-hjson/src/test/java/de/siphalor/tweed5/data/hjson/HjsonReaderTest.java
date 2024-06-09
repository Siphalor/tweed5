package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class HjsonReaderTest {
	private static final double DOUBLE_PRECISION = 0.000000001D;

	@ParameterizedTest
	@CsvSource({
			"127,127",
			"-128,-128",
			"1.23e2,123",
			"1230E-1,123",
	})
	void testByte(String input, byte expected) {
		HjsonReader hjsonReader = setupReaderWithLexer(input);
		TweedDataToken token = assertDoesNotThrow(hjsonReader::readToken);
		assertEquals(expected, assertDoesNotThrow(token::readAsByte));
		assertTrue(token.canReadAsByte());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"128",
			"1.23",
			"-129",
			"1.23e3",
			"123E-1",
	})
	void testByteIllegal(String input) {
		HjsonReader hjsonReader = setupReaderWithLexer(input);
		TweedDataToken token = assertDoesNotThrow(hjsonReader::readToken);
		assertThrows(TweedDataReadException.class, token::readAsByte);
		assertFalse(token.canReadAsByte());
	}

	@ParameterizedTest
	@CsvSource(value = {
			"123,123",
			"123e4,1230000",
			"9.87e3,9870",
			"45670E-1,4567",
			"-123.56E+5,-12356000",
	})
	void testInteger(String input, int expected) {
		HjsonReader hjsonReader = setupReaderWithLexer(input);
		TweedDataToken token = assertDoesNotThrow(hjsonReader::readToken);
		assertEquals(expected, assertDoesNotThrow(token::readAsInt));
		assertTrue(token.canReadAsInt());
	}

	@ParameterizedTest
	@CsvSource(
			ignoreLeadingAndTrailingWhitespace = false,
			value = {
					"123,123",
					"12.34,12.34",
					"123456789.123456789,123456789.123456789",
					"1234.057e8,123405700000",
					"987.654E-5,0.00987654",
			}
	)
	void testDouble(String input, double expected) {
		HjsonReader hjsonReader = setupReaderWithLexer(input);
		TweedDataToken token = assertDoesNotThrow(hjsonReader::readToken);
		assertEquals(expected, assertDoesNotThrow(token::readAsDouble), DOUBLE_PRECISION);
		assertTrue(token.canReadAsDouble());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{test:abc\ncdef:123\na:true}",
			"\n{test: abc \ncdef:123,a\n:\ntrue\n}",
			"// \n{\n\ttest:abc\ncdef:123e0,a: true ,}",
	})
	void testObject(String input) {
		HjsonReader hjsonReader = setupReaderWithLexer(input);
		TweedDataToken token;
		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapStart());

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapEntryKey());
		assertTrue(token.canReadAsString());
		assertEquals("test", assertDoesNotThrow(token::readAsString));
		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapEntryValue());
		assertTrue(token.canReadAsString());
		assertEquals("abc", assertDoesNotThrow(token::readAsString));

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapEntryKey());
		assertTrue(token.canReadAsString());
		assertEquals("cdef", assertDoesNotThrow(token::readAsString));
		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapEntryValue());
		assertTrue(token.canReadAsInt());
		assertEquals(123, assertDoesNotThrow(token::readAsInt));

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapEntryKey());
		assertTrue(token.canReadAsString());
		assertEquals("a", assertDoesNotThrow(token::readAsString));
		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapEntryValue());
		assertTrue(token.canReadAsBoolean());
		assertEquals(true, assertDoesNotThrow(token::readAsBoolean));

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isMapEnd());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"[12,34,56]",
			"[12\n34\n\t56]",
			"[\n12\n\t\t34\n\t56\n]",
			"[\n12,34\n\t56\n]",
	})
	void testArray(String input) {
		HjsonReader hjsonReader = setupReaderWithLexer(input);
		TweedDataToken token;
		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isListStart());

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isListValue());
		assertTrue(token.canReadAsInt());
		assertEquals(12, assertDoesNotThrow(token::readAsInt));

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isListValue());
		assertTrue(token.canReadAsInt());
		assertEquals(34, assertDoesNotThrow(token::readAsInt));

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isListValue());
		assertTrue(token.canReadAsInt());
		assertEquals(56, assertDoesNotThrow(token::readAsInt));

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isListEnd());
	}

	 @ParameterizedTest
	 @ValueSource(strings = {
			 "[]",
			 "[\n\n]",
			 "[     ]",
			 "[\n\t\t]",
	 })
	 void testEmptyArray(String input) {
		HjsonReader hjsonReader = setupReaderWithLexer(input);
		TweedDataToken token;
		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isListStart());

		token = assertDoesNotThrow(hjsonReader::readToken);
		assertTrue(token.isListEnd());
	 }

	private HjsonReader setupReaderWithLexer(String input) {
		return new HjsonReader(new HjsonLexer(new StringReader(input)));
	}
}
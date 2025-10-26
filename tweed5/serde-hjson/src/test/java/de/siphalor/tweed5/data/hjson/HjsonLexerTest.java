package de.siphalor.tweed5.data.hjson;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class HjsonLexerTest {

	@Test
	void generalEof() {
		HjsonLexer lexer = createLexer("");
		assertGeneralEof(lexer, new HjsonReadPosition(1, 1));
	}

	@Test
	void innerObjectEof() {
		HjsonLexer lexer = createLexer("");
		assertEquals(
				new HjsonLexerToken(
						HjsonLexerToken.Type.EOF,
						new HjsonReadPosition(1, 1),
						new HjsonReadPosition(1, 1),
						null
				),
				assertDoesNotThrow(lexer::nextGeneralToken)
		);
	}

	@ParameterizedTest
	@CsvSource(
			delimiter = ';',
			value = {
					"[;BRACKET_OPEN",
					"];BRACKET_CLOSE",
					"{;BRACE_OPEN",
					"};BRACE_CLOSE",
					":;COLON",
					",;COMMA",
			}
	)
	void generalTerminalToken(String input, HjsonLexerToken.Type tokenType) {
		HjsonLexer lexer = createLexer(input);

		assertEquals(new HjsonLexerToken(
				tokenType,
				new HjsonReadPosition(1, 1),
				new HjsonReadPosition(1, 1),
				null
		), assertDoesNotThrow(lexer::nextGeneralToken));

		assertGeneralEof(lexer, new HjsonReadPosition(1, 2));
	}

	@ParameterizedTest
	@CsvSource(
			delimiter = ';',
			value = {
					"[;BRACKET_OPEN",
					"];BRACKET_CLOSE",
					"{;BRACE_OPEN",
					"};BRACE_CLOSE",
					":;COLON",
					",;COMMA",
			}
	)
	void innerObjectTerminalToken(String input, HjsonLexerToken.Type tokenType) {
		HjsonLexer lexer = createLexer(input);

		assertEquals(new HjsonLexerToken(
				tokenType,
				new HjsonReadPosition(1, 1),
				new HjsonReadPosition(1, 1),
				null
		), assertDoesNotThrow(lexer::nextInnerObjectToken));

		assertGeneralEof(lexer, new HjsonReadPosition(1, 2));
	}

	@ParameterizedTest
	@CsvSource({
			"null,NULL",
			"true,TRUE",
			"false,FALSE",
	})
	void generalConstants(String constant, HjsonLexerToken.Type tokenType) {
		HjsonLexer lexer = createLexer(constant);

		assertEquals(new HjsonLexerToken(
				tokenType,
				new HjsonReadPosition(1, 1),
				new HjsonReadPosition(1, constant.length()),
				constant
		), assertDoesNotThrow(lexer::nextGeneralToken));

		assertGeneralEof(lexer, new HjsonReadPosition(1, constant.length() + 1));
	}

	@ParameterizedTest
	@CsvSource(
			value = {
					"123,0,3",
					"  123  ,2,5",
					"123.45,0,6",
					"500e8,0,5",
					"500E8,0,5",
					" 789.45e-9 ,1,10",
					"-45e+8,0,6",
					"  -12.34E-81 ,2,12",
			},
			ignoreLeadingAndTrailingWhitespace = false
	)
	void generalNumber(String input, int begin, int end) {
		HjsonLexer lexer = createLexer(input);

		assertEquals(new HjsonLexerToken(
				HjsonLexerToken.Type.NUMBER,
				new HjsonReadPosition(1, begin + 1),
				new HjsonReadPosition(1, end),
				input.substring(begin, end)
		), assertDoesNotThrow(lexer::nextGeneralToken));

		assertGeneralEof(lexer, new HjsonReadPosition(1, input.length() + 1));
	}

	private HjsonLexer createLexer(String input) {
		return new HjsonLexer(new StringReader(input));
	}

	private static void assertGeneralEof(HjsonLexer lexer, HjsonReadPosition position) {
		assertEquals(
				new HjsonLexerToken(HjsonLexerToken.Type.EOF, position, position, null),
				assertDoesNotThrow(lexer::nextGeneralToken)
		);
	}
}
package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.PrimitiveIterator;

@ApiStatus.Internal
@RequiredArgsConstructor
public class HjsonLexer {
	private static final int EMPTY_CODEPOINT = -2;

	private final Reader reader;

	private final HjsonReadPosition currentPos = new HjsonReadPosition();

	private int peekedCodePoint = EMPTY_CODEPOINT;
	private int peeked2CodePoint = EMPTY_CODEPOINT;

	public HjsonLexerToken nextGeneralToken() throws TweedDataReadException {
		chompInlineWhitespaceAndComments();
		int codePoint = eatCodePoint();

		HjsonLexerToken.Type terminalTokenType = getTerminalTokenType(codePoint);
		if (terminalTokenType != null) {
			return createTerminalToken(terminalTokenType);
		}

		HjsonLexerToken token = tryReadQuotedString(codePoint);
		if (token != null) {
			return token;
		}

		return readQuotelessLiteral(codePoint);
	}

	public HjsonLexerToken nextInnerObjectToken() throws TweedDataReadException {
		chompInlineWhitespaceAndComments();
		int codePoint = eatCodePoint();

		HjsonLexerToken.Type terminalTokenType = getTerminalTokenType(codePoint);
		if (terminalTokenType != null) {
			return createTerminalToken(terminalTokenType);
		}

		if (codePoint == '"') {
			return readJsonQuotedString(codePoint);
		} else if (codePoint == '\'') {
			return readJsonQuotedString(codePoint);
		} else if (codePoint < 0x21) {
			throw TweedDataReadException.builder().message("Illegal character \"" + String.copyValueOf(Character.toChars(codePoint)) + "\"").build();
		} else {
			return readQuotelessMemberName(codePoint);
		}
	}

	@Nullable
	private HjsonLexerToken.Type getTerminalTokenType(int codePoint) {
		switch (codePoint) {
			case -1: return HjsonLexerToken.Type.EOF;
			case '[': return HjsonLexerToken.Type.BRACKET_OPEN;
			case ']': return HjsonLexerToken.Type.BRACKET_CLOSE;
			case '{': return HjsonLexerToken.Type.BRACE_OPEN;
			case '}': return HjsonLexerToken.Type.BRACE_CLOSE;
			case ':': return HjsonLexerToken.Type.COLON;
			case ',': return HjsonLexerToken.Type.COMMA;
			case '\n': return HjsonLexerToken.Type.LINE_FEED;
			default: return null;
		}
	}

	private HjsonLexerToken createTerminalToken(HjsonLexerToken.Type tokenType) {
		HjsonReadPosition position = currentPos.copy();
		return new HjsonLexerToken(tokenType, position, position, null);
	}

	@Nullable
	private HjsonLexerToken tryReadQuotedString(int codePoint) throws TweedDataReadException {
		if (codePoint == '"') {
			return readJsonQuotedString('"');
		} else if (codePoint == '\'') {
			int peek = peekCodePoint();
			if (peek == '\'') {
				int peek2 = peek2CodePoint();
				if (peek2 == '\'') {
					return readMultilineString();
				} else {
					HjsonReadPosition beginPos = currentPos.copy();
					eatCodePoint();
					return new HjsonLexerToken(
							HjsonLexerToken.Type.JSON_STRING,
							beginPos,
							currentPos.copy(),
							"''"
					);
				}
			} else {
				return readJsonQuotedString('\'');
			}
		} else {
			return null;
		}
	}

	private HjsonLexerToken readJsonQuotedString(int quoteCodePoint) throws TweedDataReadException {
		HjsonReadPosition beginPos = currentPos.copy();
		StringBuilder tokenBuffer = new StringBuilder();
		tokenBuffer.appendCodePoint(quoteCodePoint);

		while (true) {
			int codePoint = eatCodePoint();
			if (codePoint == -1) {
				throw TweedDataReadException.builder().message("Unterminated quoted string at " + currentPos + ", started at " + beginPos).build();
			} else if (codePoint == quoteCodePoint) {
				tokenBuffer.appendCodePoint(codePoint);
				return new HjsonLexerToken(
						HjsonLexerToken.Type.JSON_STRING,
						beginPos,
						currentPos.copy(),
						tokenBuffer
				);
			} else if (codePoint == '\\') {
				tokenBuffer.appendCodePoint(codePoint);
				tokenBuffer.appendCodePoint(eatCodePoint());
			} else {
				tokenBuffer.appendCodePoint(codePoint);
			}
		}
	}

	private HjsonLexerToken readMultilineString() throws TweedDataReadException {
		HjsonReadPosition beginPos = currentPos.copy();
		int indentToChomp = beginPos.index() - 1;
		eatCodePoint();
		eatCodePoint();

		StringBuilder tokenBuffer = new StringBuilder();
		tokenBuffer.append("'''");

		boolean chompIndent = false;
		while (true) {
			int codePoint = eatCodePoint();
			if (codePoint == -1) {
				throw TweedDataReadException.builder().message("Unexpected end of multiline string at " + currentPos + ", started at " + beginPos).build();
			} else if (isInlineWhitespace(codePoint)) {
				tokenBuffer.appendCodePoint(codePoint);
			} else {
				if (codePoint == '\n') {
					chompIndent = true;
					tokenBuffer.setLength(3);
				}
				break;
			}
		}

		while (true) {
			if (chompIndent) {
				chompMultilineStringIndent(indentToChomp);
			} else {
				chompIndent = true;
			}
			int singleQuoteCount = 0;
			while (true) {
				int codePoint = eatCodePoint();
				if (codePoint == -1) {
					throw TweedDataReadException.builder().message("Unexpected end of multiline string at " + currentPos + ", started at " + beginPos).build();
				}
				if (codePoint == '\'') {
					singleQuoteCount++;
					if (singleQuoteCount == 3) {
						char lastActualChar = tokenBuffer.charAt(tokenBuffer.length() - 3);
						if (lastActualChar == '\n') {
							tokenBuffer.delete(tokenBuffer.length() - 3, tokenBuffer.length() - 2);
						}
						tokenBuffer.append('\'');

						return new HjsonLexerToken(
								HjsonLexerToken.Type.MULTILINE_STRING,
								beginPos,
								currentPos.copy(),
								tokenBuffer
						);
					}
				} else {
					singleQuoteCount = 0;
				}

				tokenBuffer.appendCodePoint(codePoint);

				if (codePoint == '\n') {
					break;
				}
			}
		}
	}

	private HjsonLexerToken readQuotelessMemberName(int codepoint) throws TweedDataReadException {
		HjsonReadPosition beginPos = currentPos.copy();
		StringBuilder tokenBuffer = new StringBuilder();
		tokenBuffer.appendCodePoint(codepoint);

		while (true) {
			int peek = peekCodePoint();
			if (peek == -1 || peek == '\n' || isPunctuator(peek)) {
				break;
			}
			tokenBuffer.appendCodePoint(eatCodePoint());
		}

		return new HjsonLexerToken(
				HjsonLexerToken.Type.QUOTELESS_STRING,
				beginPos,
				currentPos.copy(),
				tokenBuffer
		);
	}

	private HjsonLexerToken readQuotelessLiteral(int codePoint) throws TweedDataReadException {
		if (codePoint == 'n') {
			return readConstantOrQuotelessString(codePoint, "null", HjsonLexerToken.Type.NULL);
		} else if (codePoint == 't') {
			return readConstantOrQuotelessString(codePoint, "true", HjsonLexerToken.Type.TRUE);
		} else if (codePoint == 'f') {
			return readConstantOrQuotelessString(codePoint, "false", HjsonLexerToken.Type.FALSE);
		} else if (codePoint == '-' || isDigit(codePoint)) {
			return readNumberLiteralOrQuotelessString(codePoint);
		} else {
			StringBuilder tokenBuffer = new StringBuilder();
			tokenBuffer.appendCodePoint(codePoint);
			return readQuotelessStringToEndOfLine(currentPos.copy(), tokenBuffer);
		}
	}

	private HjsonLexerToken readConstantOrQuotelessString(
			int firstCodePoint,
			String rest,
			HjsonLexerToken.Type tokenType
	) throws TweedDataReadException {
		HjsonReadPosition beginPos = currentPos.copy();
		StringBuilder tokenBuffer = new StringBuilder();
		tokenBuffer.appendCodePoint(firstCodePoint);

		PrimitiveIterator.OfInt restIterator = rest.codePoints().iterator();
		restIterator.nextInt(); // skip first, as already checked and consumed
		while (restIterator.hasNext()) {
			int codePoint = eatCodePoint();
			tokenBuffer.appendCodePoint(codePoint);
			if (codePoint != restIterator.nextInt()) {
				return readQuotelessStringToEndOfLine(beginPos, tokenBuffer);
			}
		}

		return chompAfterLiteralOrReadToQuotelessString(tokenType, beginPos, tokenBuffer);
	}

	private HjsonLexerToken readNumberLiteralOrQuotelessString(int firstCodePoint) throws TweedDataReadException {
		HjsonReadPosition beginPos = currentPos.copy();
		StringBuilder tokenBuffer = new StringBuilder();
		tokenBuffer.appendCodePoint(firstCodePoint);

		int codePoint = firstCodePoint;

		if (codePoint == '-') {
			codePoint = eatCodePoint();
			if (codePoint == -1) {
				throw TweedDataReadException.builder().message("Unexpected end of number at " + currentPos).build();
			}
			tokenBuffer.appendCodePoint(codePoint);
		}

		if (!isDigit(codePoint)) {
			return readQuotelessStringToEndOfLine(beginPos, tokenBuffer);
		}
		boolean startsWithZero = codePoint == '0';
		codePoint = peekCodePoint();
		if (startsWithZero && isDigit(codePoint)) {
			return readQuotelessStringToEndOfLine(beginPos, tokenBuffer);
		}

		eatManyDigitsToBuffer(tokenBuffer);

		if (peekCodePoint() == '.') {
			tokenBuffer.appendCodePoint(eatCodePoint());
			codePoint = eatCodePoint();
			tokenBuffer.appendCodePoint(codePoint);
			if (!isDigit(codePoint)) {
				return readQuotelessStringToEndOfLine(beginPos, tokenBuffer);
			}
			eatManyDigitsToBuffer(tokenBuffer);
		}
		if (peekCodePoint() == 'e' || peekCodePoint() == 'E') {
			tokenBuffer.appendCodePoint(eatCodePoint());
			codePoint = eatCodePoint();
			tokenBuffer.appendCodePoint(codePoint);
			if (codePoint == '+' || codePoint == '-') {
				codePoint = eatCodePoint();
				tokenBuffer.appendCodePoint(codePoint);
			}
			if (!isDigit(codePoint)) {
				return readQuotelessStringToEndOfLine(beginPos, tokenBuffer);
			}
			eatManyDigitsToBuffer(tokenBuffer);
		}

		return chompAfterLiteralOrReadToQuotelessString(HjsonLexerToken.Type.NUMBER, beginPos, tokenBuffer);
	}

	private void eatManyDigitsToBuffer(StringBuilder buffer) throws TweedDataReadException {
		while (true) {
			int codePoint = peekCodePoint();
			if (!isDigit(codePoint)) {
				break;
			}
			buffer.appendCodePoint(eatCodePoint());
		}
	}

	private HjsonLexerToken chompAfterLiteralOrReadToQuotelessString(
			HjsonLexerToken.Type tokenType,
			HjsonReadPosition beginPos,
			StringBuilder tokenBuffer
	) throws TweedDataReadException {
		int literalEndLength = tokenBuffer.length();
		HjsonReadPosition literalEndPos = currentPos.copy();

		while (true) {
			int peek = peekCodePoint();
			if (peek == -1 || peek == ',' || peek == '\n' || peek == '#' || peek == ']' || peek == '}') {
				tokenBuffer.setLength(literalEndLength);
				return new HjsonLexerToken(tokenType, beginPos, literalEndPos, tokenBuffer);
			} else if (peek == '/') {
				int peek2 = peek2CodePoint();
				if (peek2 == '/' || peek2 == '*') {
					tokenBuffer.setLength(literalEndLength);
					return new HjsonLexerToken(tokenType, beginPos, literalEndPos, tokenBuffer);
				} else {
					return readQuotelessStringToEndOfLine(beginPos, tokenBuffer);
				}
			} else if (!isInlineWhitespace(peek)) {
				return readQuotelessStringToEndOfLine(beginPos, tokenBuffer);
			}

			tokenBuffer.appendCodePoint(eatCodePoint());
		}
	}

	private HjsonLexerToken readQuotelessStringToEndOfLine(
			HjsonReadPosition beginPos,
			StringBuilder tokenBuffer
	) throws TweedDataReadException {
		int lastNonWhitespaceLength = tokenBuffer.length();
		while (true) {
			int codePoint = peekCodePoint();
			if (codePoint == -1 || codePoint == '\n') {
				tokenBuffer.setLength(lastNonWhitespaceLength);
				return new HjsonLexerToken(
						HjsonLexerToken.Type.QUOTELESS_STRING,
						beginPos,
						currentPos.copy(),
						tokenBuffer
				);
			} else {
				tokenBuffer.appendCodePoint(eatCodePoint());
				if (!isInlineWhitespace(codePoint)) {
					lastNonWhitespaceLength = tokenBuffer.length();
				}
			}
		}
	}

	private void chompMultilineStringIndent(int count) throws TweedDataReadException {
		for (int i = 0; i < count; i++) {
			int codePoint = eatCodePoint();
			if (codePoint == -1) {
				return;
			} else if (!isInlineWhitespace(codePoint)) {
				throw TweedDataReadException.builder().message("Illegal indent at " + currentPos + ", expected " + count + " whitespace characters").build();
			}
		}
	}

	private void chompInlineWhitespaceAndComments() throws TweedDataReadException {
		while (true) {
			int peek = peekCodePoint();
			if (isInlineWhitespace(peek)) {
				eatCodePoint();
			} else if (peek == '#') {
				eatCodePoint();
				chompToEndOfLine();
			} else if (peek == '/') {
				int peek2 = peek2CodePoint();
				if (peek2 == '/') {
					eatCodePoint();
					eatCodePoint();
					chompToEndOfLine();
				} else if (peek2 == '*') {
					eatCodePoint();
					eatCodePoint();
					chompToEndOfBlockComment();
				}
			} else {
				break;
			}
		}
	}

	private void chompToEndOfLine() throws TweedDataReadException {
		while (true) {
			int codePoint = eatCodePoint();
			if (codePoint == -1 || codePoint == '\n') {
				break;
			}
		}
	}

	private void chompToEndOfBlockComment() throws TweedDataReadException {
		boolean lastWasAsterisk = false;
		while (true) {
			int codePoint = eatCodePoint();
			if (codePoint == -1) {
				throw TweedDataReadException.builder().message("Unterminated block comment at end of file " + currentPos).build();
			} else if (codePoint == '*') {
				lastWasAsterisk = true;
			} else if (lastWasAsterisk && codePoint == '/') {
				break;
			}
		}
	}

	private boolean isPunctuator(int codePoint) {
		return codePoint == ',' || codePoint == ':' || codePoint == '[' || codePoint == ']' || codePoint == '{' || codePoint == '}';
	}

	private boolean isDigit(int codePoint) {
		return codePoint >= '0' && codePoint <= '9';
	}

	private boolean isInlineWhitespace(int codePoint) {
		return codePoint == ' ' || codePoint == '\t' || codePoint == '\r';
	}

	private int peek2CodePoint() throws TweedDataReadException {
		if (peeked2CodePoint == EMPTY_CODEPOINT) {
			if (peekedCodePoint == EMPTY_CODEPOINT) {
				peekedCodePoint = readCodePoint();
			}
			peeked2CodePoint = readCodePoint();
		}
		return peeked2CodePoint;
	}

	private int peekCodePoint() throws TweedDataReadException {
		if (peekedCodePoint == EMPTY_CODEPOINT) {
			peekedCodePoint = readCodePoint();
		}
		return peekedCodePoint;
	}

	private int eatCodePoint() throws TweedDataReadException {
		int codePoint;
		if (peekedCodePoint != EMPTY_CODEPOINT) {
			codePoint = peekedCodePoint;
			peekedCodePoint = peeked2CodePoint;
			peeked2CodePoint = EMPTY_CODEPOINT;
		} else {
			codePoint = readCodePoint();
		}
		if (codePoint == '\n') {
			currentPos.nextLine();
		} else {
			currentPos.nextCodepoint();
		}
		return codePoint;
	}

	private int readCodePoint() throws TweedDataReadException {
		try {
			return reader.read();
		} catch (IOException e) {
			throw TweedDataReadException.builder().message("Failed to read character from input at " + currentPos).cause(e).build();
		}
	}
}

package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.*;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class HjsonReader implements TweedDataReader {
	private final HjsonLexer lexer;
	private final Deque<Context> contexts;
	private State state = State.BEFORE_VALUE;

	private @Nullable HjsonLexerToken peekedLexerToken;

	private @Nullable TweedDataToken peekedToken;

	public HjsonReader(HjsonLexer lexer) {
		this.lexer = lexer;
		this.contexts = new LinkedList<>();
		this.contexts.push(Context.VALUE);
	}

	@Override
	public TweedDataToken peekToken() throws TweedDataReadException {
		if (peekedToken == null) {
			peekedToken = nextToken();
		}
		return peekedToken;
	}

	@Override
	public TweedDataToken readToken() throws TweedDataReadException {
		if (peekedToken != null) {
			TweedDataToken token = peekedToken;
			peekedToken = null;
			return token;
		}
		return nextToken();
	}

	private TweedDataToken nextToken() throws TweedDataReadException {
		Context currentContext = currentContext();
		switch (currentContext) {
			case OBJECT:
				return nextObjectToken();
			case LIST:
				return nextListToken();
			case VALUE:
				return nextValueToken();
		}
		// unreachable
		throw new IllegalStateException("Illegal context " + currentContext);
	}

	private TweedDataToken nextObjectToken() throws TweedDataReadException {
		if (state == State.AFTER_OBJECT_KEY) {
			chompLineFeedTokensInGeneral();

			HjsonLexerToken lexerToken = eatGeneralLexerToken();
			if (lexerToken.type() == HjsonLexerToken.Type.COLON) {
				state = State.BEFORE_VALUE;
			} else {
				throw createIllegalTokenException(lexerToken, HjsonLexerToken.Type.COLON);
			}
		}

		if (state == State.BEFORE_VALUE) {
			return TweedDataTokens.asMapEntryValue(nextValueToken());
		}

		if (state == State.AFTER_VALUE) {
			HjsonLexerToken lexerToken = eatGeneralLexerToken();
			if (lexerToken.type() == HjsonLexerToken.Type.BRACE_CLOSE) {
				contexts.pop();
				state = State.AFTER_VALUE;
				return TweedDataTokens.getMapEnd();
			} else if (lexerToken.type() == HjsonLexerToken.Type.LINE_FEED || lexerToken.type() == HjsonLexerToken.Type.COMMA) {
				state = State.BEFORE_OBJECT_KEY;
			} else {
				throw createIllegalTokenException(lexerToken, HjsonLexerToken.Type.BRACE_CLOSE, HjsonLexerToken.Type.LINE_FEED, HjsonLexerToken.Type.COMMA);
			}
		}

		if (state == State.BEFORE_OBJECT_KEY) {
			chompLineFeedTokensInObject();

			HjsonLexerToken lexerToken = eatObjectLexerToken();
			if (lexerToken.type() == HjsonLexerToken.Type.BRACE_CLOSE) {
				contexts.pop();
				state = State.AFTER_VALUE;
				return TweedDataTokens.getMapEnd();
			} else if (lexerToken.type() == HjsonLexerToken.Type.QUOTELESS_STRING || lexerToken.type() == HjsonLexerToken.Type.JSON_STRING) {
				state = State.AFTER_OBJECT_KEY;
				return TweedDataTokens.asMapEntryKey(createStringToken(lexerToken));
			} else {
				throw createIllegalTokenException(lexerToken, HjsonLexerToken.Type.BRACE_CLOSE, HjsonLexerToken.Type.QUOTELESS_STRING, HjsonLexerToken.Type.JSON_STRING);
			}
		}

		throw createIllegalStateException();
	}

	private TweedDataToken nextListToken() throws TweedDataReadException {
		if (state == State.AFTER_VALUE) {
			HjsonLexerToken lexerToken = eatGeneralLexerToken();
			if (lexerToken.type() == HjsonLexerToken.Type.BRACKET_CLOSE) {
				contexts.pop();
				state = State.AFTER_VALUE;
				return TweedDataTokens.getListEnd();
			} else if (lexerToken.type() == HjsonLexerToken.Type.COMMA || lexerToken.type() == HjsonLexerToken.Type.LINE_FEED) {
				state = State.BEFORE_VALUE;
			} else {
				throw createIllegalTokenException(lexerToken, HjsonLexerToken.Type.BRACKET_CLOSE, HjsonLexerToken.Type.COMMA, HjsonLexerToken.Type.LINE_FEED);
			}
		}

		if (state == State.BEFORE_VALUE) {
			chompLineFeedTokensInGeneral();

			HjsonLexerToken lexerToken = peekGeneralLexerToken();
			if (lexerToken.type() == HjsonLexerToken.Type.BRACKET_CLOSE) {
				eatGeneralLexerToken();
				contexts.pop();
				state = State.AFTER_VALUE;
				return TweedDataTokens.getListEnd();
			}
			return TweedDataTokens.asListValue(nextValueToken());
		}

		throw createIllegalStateException();
	}

	private TweedDataToken nextValueToken() throws TweedDataReadException {
		chompLineFeedTokensInGeneral();
		HjsonLexerToken lexerToken = eatGeneralLexerToken();
		switch (lexerToken.type()) {
			case NULL:
				state = State.AFTER_VALUE;
				return TweedDataTokens.getNull();
			case TRUE:
			case FALSE:
				state = State.AFTER_VALUE;
				return createBooleanToken(lexerToken);
			case NUMBER:
				state = State.AFTER_VALUE;
				return createNumberToken(lexerToken);
			case QUOTELESS_STRING:
			case JSON_STRING:
			case MULTILINE_STRING:
				state = State.AFTER_VALUE;
				return createStringToken(lexerToken);
			case BRACKET_OPEN:
				state = State.BEFORE_VALUE;
				contexts.push(Context.LIST);
				return TweedDataTokens.getListStart();
			case BRACE_OPEN:
				state = State.BEFORE_OBJECT_KEY;
				contexts.push(Context.OBJECT);
				return TweedDataTokens.getMapStart();
			default:
				throw createIllegalTokenException(
						lexerToken,
						HjsonLexerToken.Type.NULL,
						HjsonLexerToken.Type.TRUE,
						HjsonLexerToken.Type.FALSE,
						HjsonLexerToken.Type.NUMBER,
						HjsonLexerToken.Type.QUOTELESS_STRING,
						HjsonLexerToken.Type.JSON_STRING,
						HjsonLexerToken.Type.MULTILINE_STRING,
						HjsonLexerToken.Type.BRACKET_OPEN,
						HjsonLexerToken.Type.BRACE_OPEN
				);
		}
	}

	private void chompLineFeedTokensInGeneral() throws TweedDataReadException {
		while (peekGeneralLexerToken().type() == HjsonLexerToken.Type.LINE_FEED) {
			eatGeneralLexerToken();
		}
	}

	private void chompLineFeedTokensInObject() throws TweedDataReadException {
		while (peekObjectLexerToken().type() == HjsonLexerToken.Type.LINE_FEED) {
			eatObjectLexerToken();
		}
	}

	private TweedDataToken createBooleanToken(HjsonLexerToken lexerToken) {
		return new TweedDataToken() {
			@Override
			public boolean canReadAsBoolean() {
				return true;
			}

			@Override
			public boolean readAsBoolean() {
				return lexerToken.type() == HjsonLexerToken.Type.TRUE;
			}

			@Override
			public String toString() {
				return "HJSON boolean token [" + lexerToken + "]";
			}
		};
	}

	@NullUnmarked
	private TweedDataToken createNumberToken(HjsonLexerToken lexerToken) {
		assert lexerToken.content() != null;
		return new TweedDataToken() {
			private Long tryLong;
			private Double tryDouble;
			private boolean fraction;
			private boolean mantissaTooLarge;
			private boolean exponentTooLarge;

			@Override
			public boolean canReadAsByte() {
				tryReadLong();
				return isValidIntegerValue(Byte.MIN_VALUE, Byte.MAX_VALUE);
			}

			@Override
			public byte readAsByte() throws TweedDataReadException {
				tryReadLong();
				requireValidIntegerValue(Byte.MIN_VALUE, Byte.MAX_VALUE);
				return tryLong.byteValue();
			}

			@Override
			public boolean canReadAsShort() {
				tryReadLong();
				return isValidIntegerValue(Short.MIN_VALUE, Short.MAX_VALUE);
			}

			@Override
			public short readAsShort() throws TweedDataReadException {
				tryReadLong();
				requireValidIntegerValue(Short.MIN_VALUE, Short.MAX_VALUE);
				return tryLong.shortValue();
			}

			@Override
			public boolean canReadAsInt() {
				tryReadLong();
				return isValidIntegerValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
			}

			@Override
			public int readAsInt() throws TweedDataReadException {
				tryReadLong();
				requireValidIntegerValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
				return tryLong.intValue();
			}

			@Override
			public boolean canReadAsLong() {
				tryReadLong();
				return !mantissaTooLarge && !exponentTooLarge && !fraction;
			}

			@Override
			public long readAsLong() throws TweedDataReadException {
				tryReadLong();
				requireValidIntegerValue(Long.MIN_VALUE, Long.MAX_VALUE);
				return tryLong;
			}

			private boolean isValidIntegerValue(long min, long max) {
				return !mantissaTooLarge && !exponentTooLarge && !fraction && tryLong != null && tryLong >= min && tryLong <= max;
			}
			
			private void requireValidIntegerValue(long min, long max) throws TweedDataReadException {
				if (mantissaTooLarge) {
					throw TweedDataReadException.builder()
							.message("Mantissa of number is too large! (" + lexerToken + ")")
							.recoverable(TweedDataReaderRecoverMode.SKIP)
							.build();
				}
				if (exponentTooLarge) {
					throw TweedDataReadException.builder()
							.message("Exponent of number is too large! (" + lexerToken + ")")
							.recoverable(TweedDataReaderRecoverMode.SKIP)
							.build();
				}
				if (fraction) {
					throw TweedDataReadException.builder()
							.message("Fractional number cannot be read as non-fractional! (" + lexerToken + ")")
							.recoverable(TweedDataReaderRecoverMode.SKIP)
							.build();
				}
				if (tryLong < min) {
					throw TweedDataReadException.builder()
							.message("Number is too low for data type, minimum is " + min + " at " + lexerToken)
							.recoverable(TweedDataReaderRecoverMode.SKIP)
							.build();
				}
				if (tryLong > max) {
					throw TweedDataReadException.builder()
							.message("Number is too large for data type, maximum is " + max + " at " + lexerToken)
							.recoverable(TweedDataReaderRecoverMode.SKIP)
							.build();
				}
			}

			private void tryReadLong() {
				if (tryLong != null) {
					return;
				}
				PrimitiveIterator.OfInt iterator = lexerToken.content().codePoints().iterator();
				long sign = 1;
				int codePoint = iterator.nextInt();
				if (codePoint == '-') {
					sign = -1;
					codePoint = iterator.nextInt();
				}
				int fractionDigits = 0;
				try {
					tryLong = 0L;
					boolean inFraction = false;
					do {
						tryLong = Math.addExact(Math.multiplyExact(tryLong, 10L), codePoint - '0');
						if (inFraction) {
							fractionDigits++;
						}

						if (!iterator.hasNext()) {
							tryLong *= sign;
							if (fractionDigits > 0) {
								fraction = true;
							}
							return;
						}
						codePoint = iterator.nextInt();
						if (!inFraction && codePoint == '.') {
							inFraction = true;
							codePoint = iterator.nextInt();
						}
					} while (isDigit(codePoint));
					tryLong *= sign;
				} catch (ArithmeticException ignored) {
					mantissaTooLarge = true;
					return;
				}

				int exponent = 0;
				if (codePoint == 'e' || codePoint == 'E') {
					codePoint = iterator.nextInt();
					boolean negativeExponent = false;
					if (codePoint == '+') {
						codePoint = iterator.nextInt();
					} else if (codePoint == '-') {
						codePoint = iterator.nextInt();
						negativeExponent = true;
					}
					try {
						while (true) {
							exponent = Math.addExact(Math.multiplyExact(exponent, 10), codePoint - '0');
							if (!iterator.hasNext()) {
								break;
							}
							codePoint = iterator.nextInt();
						}
						if (negativeExponent) {
							exponent = -exponent;
						}
					} catch (ArithmeticException ignored) {
						exponentTooLarge = true;
					}
				}

				exponent -= fractionDigits;

				applyLongExponent(exponent);
			}

			private void applyLongExponent(int exponent) {
				if (exponent < 0) {
					long factor = 1L;
					while (exponent < 0) {
						factor *= 10L;
						exponent++;
					}
					if (tryLong != tryLong / factor * factor) {
						fraction = true;
						return;
					}
					tryLong /= factor;
				} else {
					try {
						while (exponent > 0) {
							tryLong = Math.multiplyExact(tryLong, 10L);
							exponent--;
						}
					} catch (ArithmeticException ignored) {
						exponentTooLarge = true;
					}
				}
			}

			@Override
			public boolean canReadAsFloat() {
				tryReadDouble();
				return Float.isFinite(tryDouble.floatValue());
			}

			@Override
			public float readAsFloat() throws TweedDataReadException {
				tryReadDouble();
				float value = tryDouble.floatValue();
				if (Float.isInfinite(value)) {
					throw TweedDataReadException.builder()
							.message("Number is out of range from " + (-Float.MAX_VALUE) + " to " + Float.MAX_VALUE + " at " + lexerToken)
							.recoverable(TweedDataReaderRecoverMode.SKIP)
							.build();
				}
				return value;
			}

			@Override
			public boolean canReadAsDouble() {
				tryReadDouble();
				return Double.isFinite(tryDouble);
			}

			@Override
			public double readAsDouble() throws TweedDataReadException {
				tryReadDouble();
				if (Double.isInfinite(tryDouble)) {
					throw TweedDataReadException.builder()
							.message("Number is out of range form " + (-Double.MAX_VALUE) + " to " + Double.MAX_VALUE + " at " + lexerToken)
							.recoverable(TweedDataReaderRecoverMode.SKIP)
							.build();
				}
				return tryDouble;
			}

			private void tryReadDouble() {
				if (tryDouble != null) {
					return;
				}

				boolean negative = false;
				PrimitiveIterator.OfInt iterator = lexerToken.content().codePoints().iterator();
				int codePoint = iterator.nextInt();
				if (codePoint == '-') {
					negative = true;
					codePoint = iterator.nextInt();
				}

				double value = 0;
				while (isDigit(codePoint)) {
					value = value * 10 + (codePoint - '0');
					if (!iterator.hasNext()) {
						tryDouble = negative ? -1D * value : value;
						return;
					}
					codePoint = iterator.nextInt();
				}

				if (codePoint == '.') {
					double factor = 0.1;
					do {
						codePoint = iterator.nextInt();
						if (!isDigit(codePoint)) {
							break;
						}
						value += factor * (codePoint - '0');
						factor /= 10;
					} while (iterator.hasNext());
				}
				if (codePoint == 'e' || codePoint == 'E') {
					codePoint = iterator.nextInt();
					double factor = 10D;
					if (codePoint == '-') {
						factor = 0.1D;
						codePoint = iterator.nextInt();
					} else if (codePoint == '+') {
						codePoint = iterator.nextInt();
					}
					double exponent = 0D;
					while (isDigit(codePoint)) {
						exponent = exponent * 10 + (codePoint - '0');
						if (!iterator.hasNext()) {
							break;
						}
						codePoint = iterator.nextInt();
					}
					factor = Math.pow(factor, exponent);
					value *= factor;
				}
				tryDouble = value;
			}

			private boolean isDigit(int codePoint) {
				return codePoint >= '0' && codePoint <= '9';
			}

			@Override
			public String toString() {
				return "HJSON numeric token [" + lexerToken + "]";
			}
		};
	}

	private TweedDataToken createStringToken(HjsonLexerToken lexerToken) {
		assert lexerToken.content() != null;
		return new TweedDataToken() {
			@Override
			public boolean canReadAsString() {
				return true;
			}

			@Override
			public String readAsString() throws TweedDataReadException {
				if (lexerToken.type() == HjsonLexerToken.Type.QUOTELESS_STRING || lexerToken.type() == HjsonLexerToken.Type.MULTILINE_STRING) {
					return Objects.requireNonNull(lexerToken.contentString());
				} else if (lexerToken.type() == HjsonLexerToken.Type.JSON_STRING) {
					return readJsonString(lexerToken.content());
				}
				throw TweedDataReadException.builder().message("Unrecognized string token").recoverable(TweedDataReaderRecoverMode.SKIP).build();
			}

			private String readJsonString(CharSequence input) throws TweedDataReadException {
				PrimitiveIterator.OfInt iterator = input.codePoints().iterator();
				int quoteCodePoint = iterator.nextInt();

				boolean escaped = false;
				StringBuilder stringBuilder = new StringBuilder();
				while (true) {
					int codePoint = iterator.nextInt();
					if (escaped) {
						escaped = false;
						codePoint = getUnescapedCodePoint(codePoint);
					} else if (codePoint == quoteCodePoint) {
						break;
					} else if (codePoint == '\\') {
						escaped = true;
					}
					stringBuilder.appendCodePoint(codePoint);
				}
				
				return stringBuilder.toString();
			}

			private int getUnescapedCodePoint(int codePoint) throws TweedDataReadException {
				switch (codePoint) {
					case 'n':
						return '\n';
					case 'r':
						return '\r';
					case 't':
						return '\t';
					case 'f':
						return '\f';
					case 'b':
						return '\b';
					case '\\':
					case '/':
					case '"':
					case '\'':
						return codePoint;
					default:
						throw TweedDataReadException.builder()
								.message("Illegal escape sequence \"\\" + String.copyValueOf(Character.toChars(codePoint)) + "\" in string " + lexerToken)
								.recoverable(TweedDataReaderRecoverMode.SKIP)
								.build();
				}
			}

			@Override
			public String toString() {
				return "HJSON string token [" + lexerToken + "]";
			}
		};
	}

	private TweedDataReadException createIllegalTokenException(
			HjsonLexerToken actualToken,
			HjsonLexerToken.Type... expected
	) {
		return TweedDataReadException.builder().message(
				"Illegal token " + actualToken + ", expected any of " +
						Arrays.stream(expected).map(Objects::toString).collect(Collectors.joining(", "))
		).build();
	}

	private TweedDataReadException createIllegalStateException() {
		return TweedDataReadException.builder().message(
				"Internal Error: Parser is in illegal state " + state + " in context " + currentContext()
		).build();
	}

	private HjsonLexerToken peekGeneralLexerToken() throws TweedDataReadException {
		if (peekedLexerToken == null) {
			peekedLexerToken = lexer.nextGeneralToken();
		}
		return peekedLexerToken;
	}

	private HjsonLexerToken peekObjectLexerToken() throws TweedDataReadException {
		if (peekedLexerToken == null) {
			peekedLexerToken = lexer.nextInnerObjectToken();
		}
		return peekedLexerToken;
	}

	private HjsonLexerToken eatGeneralLexerToken() throws TweedDataReadException {
		if (peekedLexerToken != null) {
			HjsonLexerToken token = peekedLexerToken;
			peekedLexerToken = null;
			return token;
		}
		return lexer.nextGeneralToken();
	}

	private HjsonLexerToken eatObjectLexerToken() throws TweedDataReadException {
		if (peekedLexerToken != null) {
			HjsonLexerToken token = peekedLexerToken;
			peekedLexerToken = null;
			return token;
		}
		return lexer.nextInnerObjectToken();
	}

	private Context currentContext() {
		assert contexts.peek() != null;
		return contexts.peek();
	}

	private enum Context {
		VALUE,
		LIST,
		OBJECT,
	}

	private enum State {
		BEFORE_VALUE,
		AFTER_VALUE,
		BEFORE_OBJECT_KEY,
		AFTER_OBJECT_KEY,
	}

}

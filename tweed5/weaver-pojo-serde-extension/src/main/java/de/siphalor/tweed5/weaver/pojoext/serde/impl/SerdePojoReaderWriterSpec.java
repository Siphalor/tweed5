package de.siphalor.tweed5.weaver.pojoext.serde.impl;

import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PrimitiveIterator;

/**
 * Specification describing how to construct a reader or writer.
 * <p>
 * The formal syntax is:
 * <code><pre>
 * &lt;spec&gt; ::= &lt;identifier&gt; [ '(' &lt;spec-list&gt; ')' ]
 * &lt;spec-list&gt; ::= &lt;spec&gt; [ ',' &lt;spec-list&gt; ]
 * &lt;identifier&gt; ::= &lt;identifier-part&gt; [ '.' &lt;identifier&gt; ]
 * &lt;identifier-part&gt; ::= ( &lt;alpha&gt; | '_' ) [ &lt;alphanumeric-identifier-part&gt; ]
 * &lt;alphanumeric-identifier-part&gt; ::= ( &lt;alphanumeric&gt; | '_' ) [ &lt;alphanumeric-identifier-part&gt; ]
 * </pre></code>
 */
@Value
public class SerdePojoReaderWriterSpec {
	String identifier;
	List<SerdePojoReaderWriterSpec> arguments;

	public static SerdePojoReaderWriterSpec parse(String input) throws ParseException {
		Lexer lexer = new Lexer(input.codePoints().iterator());
		SerdePojoReaderWriterSpec spec = parseSpec(lexer);
		lexer.chompWhitespace();
		int codePoint = lexer.nextCodePoint();
		if (codePoint != -1) {
			throw lexer.createException("Found trailing text after spec", codePoint);
		}
		return spec;
	}

	private static SerdePojoReaderWriterSpec parseSpec(Lexer lexer) throws ParseException {
		lexer.chompWhitespace();
		String identifier = lexer.nextIdentifier();
		lexer.chompWhitespace();
		int codePoint = lexer.peekCodePoint();
		if (codePoint == '(') {
			lexer.nextCodePoint();
			lexer.chompWhitespace();
			if (lexer.peekCodePoint() == ')') {
				lexer.nextCodePoint();
				return new SerdePojoReaderWriterSpec(identifier, Collections.emptyList());
			}
			SerdePojoReaderWriterSpec spec = new SerdePojoReaderWriterSpec(identifier, parseSpecList(lexer));
			codePoint = lexer.nextCodePoint();
			if (codePoint != ')') {
				throw lexer.createException("Argument list must be ended with a closing parenthesis", codePoint);
			}
			return spec;
		} else {
			return new SerdePojoReaderWriterSpec(identifier, Collections.emptyList());
		}
	}

	private static List<SerdePojoReaderWriterSpec> parseSpecList(Lexer lexer) throws ParseException {
		List<SerdePojoReaderWriterSpec> specs = new ArrayList<>();
		while (true) {
			specs.add(parseSpec(lexer));
			lexer.chompWhitespace();
			int codePoint = lexer.peekCodePoint();
			if (codePoint != ',') {
				break;
			}
			lexer.nextCodePoint();
		}
		return Collections.unmodifiableList(specs);
	}

	@RequiredArgsConstructor
	private static class Lexer {
		private static final int EMPTY = -2;
		private final PrimitiveIterator.OfInt codePointIterator;
		private int peek = EMPTY;
		private int index;

		public String nextIdentifier() throws ParseException {
			int codePoint = nextCodePoint();
			if (codePoint == -1) {
				throw createException("Expected identifier, got end of input", codePoint);
			} else if (!isIdentifierStart(codePoint)) {
				throw createException("Expected identifier (alphanumeric character)", codePoint);
			}
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.appendCodePoint(codePoint);
			boolean dot = false;
			while ((codePoint = peekCodePoint()) >= 0) {
				if (isIdentifierPart(codePoint)) {
					stringBuilder.appendCodePoint(nextCodePoint());
					dot = false;
				} else if (codePoint == '.') {
					if (dot) {
						throw createException("Unexpected double dot in identifier", codePoint);
					} else {
						stringBuilder.appendCodePoint(nextCodePoint());
						dot = true;
					}
				} else {
					break;
				}
			}
			if (dot) {
				throw createException("Identifier must not end with dot", codePoint);
			}
			return stringBuilder.toString();
		}

		private boolean isIdentifierStart(int codePoint) {
			return isAlpha(codePoint) || codePoint == '_';
		}

		private boolean isIdentifierPart(int codePoint) {
			return isAlphanumeric(codePoint) || codePoint == '_';
		}

		private boolean isAlphanumeric(int codePoint) {
			return (codePoint >= '0' && codePoint <= '9') || isAlpha(codePoint);
		}

		private boolean isAlpha(int codePoint) {
			return (codePoint >= 'a' && codePoint <= 'z')
					|| (codePoint >= 'A' && codePoint <= 'Z');
		}

		public void chompWhitespace() {
			while (Character.isWhitespace(peekCodePoint())) {
				nextCodePoint();
			}
		}

		private int peekCodePoint() {
			if (peek == EMPTY) {
				peek = nextCodePoint();
			}
			return peek;
		}

		private int nextCodePoint() {
			if (peek != EMPTY) {
				int codePoint = peek;
				peek = EMPTY;
				return codePoint;
			}
			if (codePointIterator.hasNext()) {
				index++;
				return codePointIterator.nextInt();
			} else {
				return -1;
			}
		}

		public ParseException createException(String message, int codePoint) {
			return new ParseException(message, index, codePoint);
		}
	}

	@Getter
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class ParseException extends Exception {
		private final int index;
		private final int codePoint;

		public ParseException(String message, int index, int codePoint) {
			super(message);
			this.index = index;
			this.codePoint = codePoint;
		}

		@Override
		public String getMessage() {
			String message = super.getMessage();
			StringBuilder stringBuilder = new StringBuilder(30 + message.length())
					.append("Parse error at index ")
					.append(index)
					.append(" \"");
			if (codePoint == -1) {
				stringBuilder.append("EOF");
			} else {
				stringBuilder.appendCodePoint(codePoint);
			}
			return stringBuilder
					.append("\": ")
					.append(message)
					.toString();
		}
	}
}

package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataCommentDecoration;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import lombok.Data;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HjsonWriter implements TweedDataVisitor {
	private static final int PREFILL_INDENT = 10;
	private static final Pattern LINE_FEED_PATTERN = Pattern.compile("\\n|\\r\\n");

	private final Writer writer;
	private final Options options;
	private final Deque<Context> contexts;
	private final StringBuilder indentBuffer;
	private int currentIndentLevel;
	private int currentIndentLength;

	public HjsonWriter(Writer writer, Options options) {
		this.writer = writer;
		this.options = options;

		contexts = new LinkedList<>(Collections.singleton(Context.ROOT));
		indentBuffer = new StringBuilder(options.indent.length() * PREFILL_INDENT);
		for (int i = 0; i < PREFILL_INDENT; i++) {
			indentBuffer.append(options.indent);
		}
	}

	@Override
	public void visitNull() {
		beforeValueWrite();
		write("null");
		afterValueWrite();
	}

	@Override
	public void visitBoolean(boolean value) {
		beforeValueWrite();
		write(value ? "true" : "false");
		afterValueWrite();
	}

	@Override
	public void visitByte(byte value) {
		beforeValueWrite();
		write(Byte.toString(value));
		afterValueWrite();
	}

	@Override
	public void visitShort(short value) {
		beforeValueWrite();
		write(Short.toString(value));
		afterValueWrite();
	}

	@Override
	public void visitInt(int value) {
		beforeValueWrite();
		write(Integer.toString(value));
		afterValueWrite();
	}

	@Override
	public void visitLong(long value) {
		beforeValueWrite();
		write(Long.toString(value));
		afterValueWrite();
	}

	@Override
	public void visitFloat(float value) {
		beforeValueWrite();
		write(Float.toString(value));
		afterValueWrite();
	}

	@Override
	public void visitDouble(double value) {
		beforeValueWrite();
		write(Double.toString(value));
		afterValueWrite();
	}

	@Override
	public void visitString(String value) {
		beforeValueWrite();
		writeStringValue(getValueStringStringType(value), value);
		afterValueWrite();
	}

	private HjsonStringType getValueStringStringType(String value) {
		if (value.isEmpty() || "true".equals(value) || "false".equals(value) || "null".equals(value)) {
			return HjsonStringType.INLINE_DOUBLE_QUOTE;
		}
		int firstCodePoint = value.codePointAt(0);
		if (Character.isDigit(firstCodePoint) || Character.isWhitespace(firstCodePoint)) {
			return HjsonStringType.INLINE_DOUBLE_QUOTE;
		}
		int lastCodePoint = value.codePointBefore(value.length());
		if (Character.isWhitespace(lastCodePoint)) {
			return HjsonStringType.INLINE_DOUBLE_QUOTE;
		}

		boolean singleQuoteFound = false;
		boolean doubleQuoteFound = false;
		int singleQuoteCount = 0;
		boolean tripleSingleQuoteFound = false;
		boolean punctuatorFound = false;
		boolean newLineFound = false;
		boolean escapeRequiredFound = false;
		boolean tabFound = false;

		PrimitiveIterator.OfInt codePointIterator = value.codePoints().iterator();
		while (codePointIterator.hasNext()) {
			int codePoint = codePointIterator.nextInt();
			if (codePoint == '\'') {
				singleQuoteFound = true;
				if (++singleQuoteCount >= 3) {
					tripleSingleQuoteFound = true;
				}
			} else {
				singleQuoteCount = 0;
				if (codePoint == '"') {
					doubleQuoteFound = true;
				} else if (codePoint == '\n' || codePoint == '\r') {
					newLineFound = true;
				} else if (!punctuatorFound && isPunctuatorCodePoint(codePoint)) {
					punctuatorFound = true;
				} else if (codePoint == '\t') {
					tabFound = true;
				} else if (codePoint == '\\' || codePoint < 0x20) {
					escapeRequiredFound = true;
				}
			}
		}

		if (!punctuatorFound && !newLineFound && !tabFound && !escapeRequiredFound) {
			return HjsonStringType.INLINE_QUOTELESS;
		}
		if (newLineFound && !tripleSingleQuoteFound) {
			return HjsonStringType.MULTILINE_SINGLE_QUOTE;
		}
		if (singleQuoteFound || !doubleQuoteFound) {
			return HjsonStringType.INLINE_DOUBLE_QUOTE;
		}
		return HjsonStringType.INLINE_SINGLE_QUOTE;
	}

	@Override
	public void visitEmptyList() {
		beforeValueWrite();
		write("[]");
		afterValueWrite();
	}

	@Override
	public void visitListStart() {
		beforeValueWrite();
		write("[");
		writeLineFeed();
		pushContext(Context.LIST);
	}

	@Override
	public void visitListEnd() {
		requireContext(Context.LIST);

		popContext();
		writeCurrentIndent();
		write("]");
		afterValueWrite();
	}

	@Override
	public void visitEmptyMap() {
		beforeValueWrite();
		write("{}");
		afterValueWrite();
	}

	@Override
	public void visitMapStart() {
		beforeValueWrite();
		write("{");
		writeLineFeed();
		pushContext(Context.MAP);
	}

	@Override
	public void visitMapEntryKey(String key) {
		requireContext(Context.MAP);
		writeCurrentIndent();
		writeStringValue(getMapEntryKeyStringType(key), key);
		write(": ");
		pushContext(Context.MAP_ENTRY);
	}

	private HjsonStringType getMapEntryKeyStringType(String key) {
		int firstCodePoint = key.codePointAt(0);
		if (firstCodePoint == '\'') {
			return HjsonStringType.INLINE_DOUBLE_QUOTE;
		} else if (firstCodePoint == '"'){
			return HjsonStringType.INLINE_SINGLE_QUOTE;
		}
		if (key.codePoints().allMatch(this::isValidMapEntryKeyCodePoint)) {
			return HjsonStringType.INLINE_QUOTELESS;
		}
		return HjsonStringType.INLINE_DOUBLE_QUOTE;
	}

	private boolean isValidMapEntryKeyCodePoint(int codePoint) {
		if (codePoint < 0x21) {
			return false;
		}
		return !isPunctuatorCodePoint(codePoint);
	}

	private boolean isPunctuatorCodePoint(int codePoint) {
		return codePoint == ',' || codePoint == ':' || codePoint == '[' || codePoint == ']' || codePoint == '{' || codePoint == '}';
	}

	@Override
	public void visitMapEnd() {
		requireContext(Context.MAP);

		popContext();
		writeCurrentIndent();
		write("}");
		afterValueWrite();
	}

	@Override
	public void visitDecoration(TweedDataDecoration decoration) {
		if (decoration instanceof TweedDataCommentDecoration) {
			visitComment(((TweedDataCommentDecoration) decoration).comment());
		}
	}

	private void visitComment(String comment) {
		Matcher lineFeedMatcher = LINE_FEED_PATTERN.matcher(comment);
		if (lineFeedMatcher.find()) {
			// Multiline
			writeMultilineCommentStart(options.multilineCommentType);

			int begin = 0;
			do {
				writeCommentLine(options.multilineCommentType, comment, begin, lineFeedMatcher.start());
				begin = lineFeedMatcher.end();
			} while (lineFeedMatcher.find(begin));
			writeCommentLine(options.multilineCommentType, comment, begin, comment.length());

			writeMultilineCommentEnd(options.multilineCommentType);
		} else {
			// Inline
			writeMultilineCommentStart(options.inlineCommentType);
			writeCommentLine(options.inlineCommentType, comment, 0, comment.length());
			writeMultilineCommentEnd(options.inlineCommentType);
		}
	}

	private void writeMultilineCommentStart(HjsonCommentType commentType) {
		if (commentType == HjsonCommentType.BLOCK) {
			writeCurrentIndentIfApplicable();
			write("/*");
			writeLineFeed();
		}
	}

	private void writeMultilineCommentEnd(HjsonCommentType commentType) {
		if (commentType == HjsonCommentType.BLOCK) {
			writeCurrentIndent();
			write(" */");
			writeLineFeed();
			if (isInInlineContext()) {
				writeCurrentIndent();
			}
		}
	}

	private void writeCommentLine(HjsonCommentType commentType, CharSequence text, int begin, int end) {
		writeCurrentIndentIfApplicable();
		write(getCommentLineStart(commentType));
		write(text, begin, end);
		writeLineFeed();
	}

	private CharSequence getCommentLineStart(HjsonCommentType commentType) {
		switch (commentType) {
			case HASH:
				return "# ";
			case SLASHES:
				return "// ";
			case BLOCK:
				return " * ";
			default:
				throw new IllegalStateException("Unknown comment type: " + commentType);
		}
	}

	private void beforeValueWrite() {
		requireValueContext();
		writeCurrentIndentIfApplicable();
	}

	private void afterValueWrite() {
		switch (currentContext()) {
			case ROOT:
			case LIST:
				writeLineFeed();
				break;
			case MAP_ENTRY:
				popContext();
				writeLineFeed();
				break;
			default:
				break;
		}
	}

	private void writeStringValue(HjsonStringType stringType, String text) {
		switch (stringType) {
			case INLINE_QUOTELESS:
				write(text);
				break;
			case INLINE_DOUBLE_QUOTE:
				writeJsonString(text, '"');
				break;
			case INLINE_SINGLE_QUOTE:
				writeJsonString(text, '\'');
				break;
			case MULTILINE_SINGLE_QUOTE:
				writeMultilineString(text);
				break;
		}
	}

	private void writeJsonString(String text, int quoteCodepoint) {
		writeCodepoint(quoteCodepoint);
		text.codePoints().forEach(codepoint -> {
			if (codepoint == quoteCodepoint) {
				write("\\");
				writeCodepoint(codepoint);
			} else {
				writeJsonStringQuotePoint(codepoint);
			}
		});
		writeCodepoint(quoteCodepoint);
	}

	private void writeJsonStringQuotePoint(int codepoint) {
		switch (codepoint) {
			case '\\':
				write("\\\\");
				break;
			case '\b':
				write("\\b");
				break;
			case '\f':
				write("\\f");
				break;
			case '\n':
				write("\\n");
				break;
			case '\r':
				write("\\r");
				break;
			case '\t':
				write("\\t");
				break;
			default:
				if (isValidJsonStringCodepoint(codepoint)) {
					writeCodepoint(codepoint);
				} else {
					write(codepointToHexEscape(codepoint));
				}
				break;
		}
	}

	private String codepointToHexEscape(int codepoint) {
		StringBuilder hexEscape = new StringBuilder("\\u0000");
		hexEscape.replace(5, 6, nibbleToHex(codepoint & 0xF));
		codepoint >>= 4;
		hexEscape.replace(4, 5, nibbleToHex(codepoint & 0xF));
		codepoint >>= 4;
		hexEscape.replace(3, 4, nibbleToHex(codepoint & 0xF));
		codepoint >>= 4;
		hexEscape.replace(2, 3, nibbleToHex(codepoint & 0xF));
		return hexEscape.toString();
	}

	private String nibbleToHex(int value) {
		switch (value) {
			case 0x0: return "0";
			case 0x1: return "1";
			case 0x2: return "2";
			case 0x3: return "3";
			case 0x4: return "4";
			case 0x5: return "5";
			case 0x6: return "6";
			case 0x7: return "7";
			case 0x8: return "8";
			case 0x9: return "9";
			case 0xA: return "A";
			case 0xB: return "B";
			case 0xC: return "C";
			case 0xD: return "D";
			case 0xE: return "E";
			case 0xF: return "F";
			default:
				throw new IllegalArgumentException("Invalid nibble value");
		}
	}

	private boolean isValidJsonStringCodepoint(int codepoint) {
		return codepoint >= 0x20 && codepoint <= 0x10FFFF && codepoint != 0x21 && codepoint != 0x5C;
	}

	private void writeMultilineString(String text) {
		boolean inInlineContext = isInInlineContext();
		if (inInlineContext) {
			writeLineFeed();
			increaseIndent();
		}

		write("'''");
		writeLineFeed();

		Matcher matcher = LINE_FEED_PATTERN.matcher(text);
		int begin = 0;
		while (matcher.find(begin)) {
			writeCurrentIndent();
			write(text, begin, matcher.start());
			writeLineFeed();
			begin = matcher.end();
		}
		writeCurrentIndent();
		write(text, begin, text.length());
		writeLineFeed();

		writeCurrentIndent();
		write("'''");

		if (inInlineContext) {
			decreaseIndent();
		}
	}

	private boolean isInInlineContext() {
		return currentContext() == Context.MAP_ENTRY;
	}

	private void writeCurrentIndentIfApplicable() {
		if (shouldWriteIndentInContext(currentContext())) {
			writeCurrentIndent();
		}
	}

	private boolean shouldWriteIndentInContext(Context context) {
		return context == Context.ROOT || context == Context.LIST || context == Context.MAP;
	}

	private void requireValueContext() {
		requireContext(Context.ROOT, Context.LIST, Context.MAP_ENTRY);
	}

	private void requireContext(Context... allowedContexts) {
		Context currentContext = currentContext();
		for (Context allowedContext : allowedContexts) {
			if (currentContext == allowedContext) {
				return;
			}
		}
		throw new TweedDataWriteException(
				"Writer is not in correct context, expected any of " + Arrays.toString(allowedContexts) +
						" but currently in " + currentContext
		);
	}

	private Context currentContext() {
		Context currentContext = contexts.peek();
		if (currentContext == null) {
			throw new IllegalStateException("Writing has terminated");
		}
		return currentContext;
	}

	private void pushContext(Context context) {
		switch (context) {
			case ROOT:
				throw new IllegalArgumentException("Root context may not be pushed");
			case LIST:
			case MAP:
				increaseIndent();
				break;
			default:
				break;
		}
		contexts.push(context);
	}

	private void popContext() {
		switch (currentContext()) {
			case LIST:
			case MAP:
				decreaseIndent();
				break;
			default:
				break;
		}
		contexts.pop();
	}

	private void increaseIndent() {
		currentIndentLevel++;
		currentIndentLength = currentIndentLevel * options.indent.length();
		ensureIndentBufferLength();
	}

	private void ensureIndentBufferLength() {
		while (currentIndentLength > indentBuffer.length()) {
			indentBuffer.append(options.indent);
		}
	}

	private void decreaseIndent() {
		if (currentIndentLevel == 0) {
			throw new IllegalStateException("Cannot decrease indent level, already at 0");
		}
		currentIndentLevel--;
		currentIndentLength = currentIndentLevel * options.indent.length();
	}

	private void writeCurrentIndent() {
		write(indentBuffer, 0, currentIndentLength);
	}

	private void writeLineFeed() {
		write(options.lineFeed);
	}

	private void write(CharSequence text, int begin, int end) {
		try {
			writer.append(text, begin, end);
		} catch (IOException e) {
			throw createExceptionForIOException(e);
		}
	}

	private void write(CharSequence text) {
		try {
			writer.append(text);
		} catch (IOException e) {
			throw createExceptionForIOException(e);
		}
	}

	private void writeCodepoint(int codepoint) {
		try {
			writer.write(codepoint);
		} catch (IOException e) {
			throw createExceptionForIOException(e);
		}
	}

	private TweedDataWriteException createExceptionForIOException(IOException e) {
		return new TweedDataWriteException("Writing Hjson failed", e);
	}

	private enum Context {
		ROOT,
		LIST,
		MAP,
		MAP_ENTRY,
	}

	@Data
	public static class Options {
		private boolean doubleQuotedInlineStrings = true;
		private String indent = "\t";
		private String lineFeed = "\n";
		private HjsonCommentType inlineCommentType = HjsonCommentType.SLASHES;
		private HjsonCommentType multilineCommentType = HjsonCommentType.BLOCK;
		private HjsonStringType preferredInlineStringType = HjsonStringType.INLINE_QUOTELESS;

		public void inlineCommentType(HjsonCommentType commentType) {
			if (commentType.block()) {
				throw new IllegalArgumentException("Inline comment type must not be a block comment type: " + commentType);
			}
			this.inlineCommentType = commentType;
		}
	}
}

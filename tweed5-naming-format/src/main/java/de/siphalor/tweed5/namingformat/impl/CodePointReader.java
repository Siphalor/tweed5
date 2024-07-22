package de.siphalor.tweed5.namingformat.impl;

import lombok.RequiredArgsConstructor;

import java.util.PrimitiveIterator;

@RequiredArgsConstructor
public class CodePointReader {
	int EMPTY_PEEK = Integer.MIN_VALUE;

	private final PrimitiveIterator.OfInt codePointIterator;
	private int peek = EMPTY_PEEK;

	public static CodePointReader ofString(CharSequence input) {
		return new CodePointReader(input.codePoints().iterator());
	}

	public boolean hasNext() {
		if (peek != EMPTY_PEEK) {
			return true;
		} else {
			return codePointIterator.hasNext();
		}
	}

	public int next() {
		if (peek != EMPTY_PEEK) {
			int codepoint = peek;
			peek = EMPTY_PEEK;
			return codepoint;
		} else {
			return codePointIterator.nextInt();
		}
	}

	public int peek() {
		if (peek == EMPTY_PEEK) {
			peek = codePointIterator.nextInt();
		}
		return peek;
	}
}

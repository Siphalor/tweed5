package de.siphalor.tweed5.namingformat.impl;

import de.siphalor.tweed5.namingformat.api.NamingFormat;

import java.util.ArrayList;
import java.util.PrimitiveIterator;

public class NamingFormatImpls {
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static final NamingFormat CAMEL_CASE = new NamingFormat() {
		@Override
		public String name() {
			return "camelCase";
		}

		@Override
		public String[] splitIntoWords(String name) {
			return splitAtUpperCase(name);
		}

		@Override
		public String joinToName(String[] words) {
			if (words.length == 0) {
				return "";
			}

			int totalLength = countTotalWordsLength(words);
			StringBuilder stringBuilder = new StringBuilder(totalLength);
			appendAllLower(stringBuilder, words[0]);
			for (int i = 1; i < words.length; i++) {
				appendCapitalized(stringBuilder, words[i]);
			}
			return stringBuilder.toString();
		}

		@Override
		public String toString() {
			return name();
		}
	};

	public static final NamingFormat PASCAL_CASE = new NamingFormat() {
		@Override
		public String name() {
			return "PascalCase";
		}

		@Override
		public String[] splitIntoWords(String name) {
			return splitAtUpperCase(name);
		}

		@Override
		public String joinToName(String[] words) {
			if (words.length == 0) {
				return "";
			}

			int totalLength = countTotalWordsLength(words);
			StringBuilder stringBuilder = new StringBuilder(totalLength);
			for (String word : words) {
				appendCapitalized(stringBuilder, word);
			}
			return stringBuilder.toString();
		}

		@Override
		public String toString() {
			return name();
		}
	};

	public static final NamingFormat KEBAB_CASE = createLowerDelimitedFormat("kebab-case", '-');
	public static final NamingFormat UPPER_KEBAB_CASE = createUpperDelimitedFormat("UPPER-KEBAB-CASE", '-');

	public static final NamingFormat SNAKE_CASE = createLowerDelimitedFormat("snake_case", '_');
	public static final NamingFormat UPPER_SNAKE_CASE = createUpperDelimitedFormat("UPPER_SNAKE_CASE", '_');

	public static final NamingFormat SPACE_CASE = createLowerDelimitedFormat("space case", ' ');
	public static final NamingFormat UPPER_SPACE_CASE = createUpperDelimitedFormat("UPPER SPACE CASE", ' ');
	public static final NamingFormat TITLE_CASE = createCapitalizedDelimitedFormat("Title Case", ' ');

	private static NamingFormat createLowerDelimitedFormat(String formatName, char delimiter) {
		return new NamingFormat() {
			@Override
			public String name() {
				return formatName;
			}

			@Override
			public String[] splitIntoWords(String name) {
				return splitAtCharacter(name, delimiter);
			}

			@Override
			public String joinToName(String[] words) {
				return joinAllLower(delimiter, words);
			}

			@Override
			public String toString() {
				return name();
			}
		};
	}

	private static NamingFormat createUpperDelimitedFormat(String formatName, char delimiter) {
		return new NamingFormat() {
			@Override
			public String name() {
				return formatName;
			}

			@Override
			public String[] splitIntoWords(String name) {
				return splitAtCharacter(name, delimiter);
			}

			@Override
			public String joinToName(String[] words) {
				return joinAllUpper(delimiter, words);
			}

			@Override
			public String toString() {
				return name();
			}
		};
	}

	private static NamingFormat createCapitalizedDelimitedFormat(String formatName, char delimiter) {
		return new NamingFormat() {
			@Override
			public String name() {
				return formatName;
			}

			@Override
			public String[] splitIntoWords(String name) {
				return splitAtCharacter(name, delimiter);
			}

			@Override
			public String joinToName(String[] words) {
				return joinCapitalized(delimiter, words);
			}

			@Override
			public String toString() {
				return name();
			}
		};
	}

	private static String[] splitAtUpperCase(String name) {
		ArrayList<String> words = new ArrayList<>();

		StringBuilder wordBuilder = new StringBuilder();

		CodePointReader codePointReader = CodePointReader.ofString(name);
		while (codePointReader.hasNext()) {
			if (wordBuilder.length() == 0) {
				wordBuilder.appendCodePoint(codePointReader.next());
			} else if (Character.isUpperCase(codePointReader.peek())) {
				words.add(wordBuilder.toString());
				wordBuilder.setLength(0);
			} else {
				wordBuilder.appendCodePoint(codePointReader.next());
			}
		}
		if (wordBuilder.length() > 0) {
			words.add(wordBuilder.toString());
		}

		return words.toArray(EMPTY_STRING_ARRAY);
	}

	private static String[] splitAtCharacter(String text, char delimiter) {
		ArrayList<String> words = new ArrayList<>();

		int index = 0;
		while (index < text.length()) {
			int delimiterIndex = text.indexOf(delimiter, index);
			if (delimiterIndex == -1) {
				words.add(text.substring(index));
				break;
			}

			words.add(text.substring(index, delimiterIndex));
			index = delimiterIndex + 1;
		}

		return words.toArray(EMPTY_STRING_ARRAY);
	}

	private static String joinAllLower(char joiner, String[] words) {
		if (words.length == 0) {
			return "";
		}

		int totalLength = countTotalWordsLength(words) + words.length - 1;
		StringBuilder stringBuilder = new StringBuilder(totalLength);
		for (String word : words) {
			appendAllLower(stringBuilder, word);
			if (stringBuilder.length() < totalLength) {
				stringBuilder.append(joiner);
			}
		}
		return stringBuilder.toString();
	}

	private static String joinAllUpper(char joiner, String[] words) {
		if (words.length == 0) {
			return "";
		}

		int totalLength = countTotalWordsLength(words) + words.length - 1;
		StringBuilder stringBuilder = new StringBuilder(totalLength);
		for (String word : words) {
			appendAllUpper(stringBuilder, word);
			if (stringBuilder.length() < totalLength) {
				stringBuilder.append(joiner);
			}
		}
		return stringBuilder.toString();
	}

	private static String joinCapitalized(char joiner, String[] words) {
		if (words.length == 0) {
			return "";
		}

		int totalLength = countTotalWordsLength(words) + words.length - 1;
		StringBuilder stringBuilder = new StringBuilder(totalLength);
		for (String word : words) {
			appendCapitalized(stringBuilder, word);
			if (stringBuilder.length() < totalLength) {
				stringBuilder.append(joiner);
			}
		}
		return stringBuilder.toString();
	}

	private static int countTotalWordsLength(String[] words) {
		if (words.length == 0) {
			return 0;
		}

		int totalLength = 0;
		for (String word : words) {
			totalLength += word.length();
		}
		return totalLength;
	}

	private static void appendAllLower(StringBuilder target, CharSequence input) {
		if (input.length() == 0) {
			return;
		}
		PrimitiveIterator.OfInt codePointIterator = input.codePoints().iterator();
		while (codePointIterator.hasNext()) {
			target.appendCodePoint(Character.toLowerCase(codePointIterator.nextInt()));
		}
	}

	private static void appendAllUpper(StringBuilder target, CharSequence input) {
		if (input.length() == 0) {
			return;
		}
		PrimitiveIterator.OfInt codePointIterator = input.codePoints().iterator();
		while (codePointIterator.hasNext()) {
			target.appendCodePoint(Character.toUpperCase(codePointIterator.nextInt()));
		}
	}

	private static void appendCapitalized(StringBuilder target, CharSequence input) {
		if (input.length() == 0) {
			return;
		}
		PrimitiveIterator.OfInt codePointIterator = input.codePoints().iterator();
		target.appendCodePoint(Character.toUpperCase(codePointIterator.nextInt()));
		while (codePointIterator.hasNext()) {
			target.appendCodePoint(Character.toLowerCase(codePointIterator.nextInt()));
		}
	}
}

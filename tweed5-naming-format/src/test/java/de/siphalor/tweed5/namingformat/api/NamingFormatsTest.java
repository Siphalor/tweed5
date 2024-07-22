package de.siphalor.tweed5.namingformat.api;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NamingFormatsTest {

	@ParameterizedTest
	@MethodSource("allNamingFormats")
	void splitIntoWordsEmpty(NamingFormat namingFormat) {
		assertArrayEquals(new String[0], namingFormat.splitIntoWords(""));
	}

	@ParameterizedTest
	@MethodSource("allNamingFormats")
	void joinToNameEmpty(NamingFormat namingFormat) {
		assertEquals("", namingFormat.joinToName(new String[0]));
	}

	@ParameterizedTest
	@MethodSource("allNamingFormats")
	void isFormatNameStableInOwnFormat(NamingFormat namingFormat) {
		assertEquals(namingFormat.name(), NamingFormats.convert(namingFormat.name(), namingFormat, namingFormat));
	}

	@ParameterizedTest
	@MethodSource("splitIntoWordsArgs")
	void splitIntoWords(NamingFormat namingFormat, String name) {
		String[] result = namingFormat.splitIntoWords(name);
		// Expecting "not a URL" in any casing
		assertEquals(3, result.length, "Words should be 3, but got: " + Arrays.toString(result));
		assertEquals("not", result[0].toLowerCase(Locale.ROOT));
		assertEquals("a", result[1].toLowerCase(Locale.ROOT));
		assertEquals("url", result[2].toLowerCase(Locale.ROOT));
	}

	private static Stream<Arguments> splitIntoWordsArgs() {
		return Stream.of(
				Arguments.of(NamingFormats.camelCase(), "notAUrl"),
				Arguments.of(NamingFormats.pascalCase(), "NotAUrl"),
				Arguments.of(NamingFormats.kebabCase(), "not-a-url"),
				Arguments.of(NamingFormats.upperKebabCase(), "NOT-A-URL"),
				Arguments.of(NamingFormats.snakeCase(), "not_a_url"),
				Arguments.of(NamingFormats.upperSnakeCase(), "NOT_A_URL"),
				Arguments.of(NamingFormats.spaceCase(), "not a url"),
				Arguments.of(NamingFormats.upperSpaceCase(), "NOT A URL"),
				Arguments.of(NamingFormats.titleCase(), "Not A Url")
		);
	}

	@ParameterizedTest
	@MethodSource("joinToNameArgs")
	void joinToName(NamingFormat namingFormat, String expectedName) {
		assertEquals(expectedName, namingFormat.joinToName(new String[]{ "an", "INTERESTING", "über", "name" }));
	}

	@ParameterizedTest
	@MethodSource("joinToNameArgs")
	void joinToNameLocaleIndependent(NamingFormat namingFormat, String expectedName) {
		Locale turkishLocale = Locale.of("tr", "TR");
		Locale.setDefault(turkishLocale);
		assertEquals("ı", "I".toLowerCase(), "Turkish locale works");

		assertEquals(expectedName, namingFormat.joinToName(new String[]{ "an", "INTERESTING", "über", "name" }));
	}

	private static Stream<Arguments> joinToNameArgs() {
		return Stream.of(
				Arguments.of(NamingFormats.camelCase(), "anInterestingÜberName"),
				Arguments.of(NamingFormats.pascalCase(), "AnInterestingÜberName"),
				Arguments.of(NamingFormats.kebabCase(), "an-interesting-über-name"),
				Arguments.of(NamingFormats.upperKebabCase(), "AN-INTERESTING-ÜBER-NAME"),
				Arguments.of(NamingFormats.snakeCase(), "an_interesting_über_name"),
				Arguments.of(NamingFormats.upperSnakeCase(), "AN_INTERESTING_ÜBER_NAME"),
				Arguments.of(NamingFormats.spaceCase(), "an interesting über name"),
				Arguments.of(NamingFormats.upperSpaceCase(), "AN INTERESTING ÜBER NAME"),
				Arguments.of(NamingFormats.titleCase(), "An Interesting Über Name")
		);
	}

	private static NamingFormat[] allNamingFormats() {
		return new NamingFormat[] {
				NamingFormats.camelCase(),
				NamingFormats.pascalCase(),
				NamingFormats.kebabCase(),
				NamingFormats.upperKebabCase(),
				NamingFormats.snakeCase(),
				NamingFormats.upperSnakeCase(),
				NamingFormats.spaceCase(),
				NamingFormats.upperSpaceCase(),
				NamingFormats.titleCase()
		};
	}
}
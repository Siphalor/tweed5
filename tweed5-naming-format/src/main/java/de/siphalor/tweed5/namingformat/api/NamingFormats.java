package de.siphalor.tweed5.namingformat.api;

import de.siphalor.tweed5.namingformat.impl.NamingFormatImpls;

public class NamingFormats {
	public static String convert(String text, NamingFormat sourceFormat, NamingFormat targetFormat) {
		return targetFormat.joinToName(sourceFormat.splitIntoWords(text));
	}

	public static NamingFormat camelCase() {
		return NamingFormatImpls.CAMEL_CASE;
	}

	public static NamingFormat pascalCase() {
		return NamingFormatImpls.PASCAL_CASE;
	}

	public static NamingFormat kebabCase() {
		return NamingFormatImpls.KEBAB_CASE;
	}

	public static NamingFormat upperKebabCase() {
		return NamingFormatImpls.UPPER_KEBAB_CASE;
	}

	public static NamingFormat snakeCase() {
		return NamingFormatImpls.SNAKE_CASE;
	}

	public static NamingFormat upperSnakeCase() {
		return NamingFormatImpls.UPPER_SNAKE_CASE;
	}

	public static NamingFormat spaceCase() {
		return NamingFormatImpls.SPACE_CASE;
	}

	public static NamingFormat upperSpaceCase() {
		return NamingFormatImpls.UPPER_SPACE_CASE;
	}

	public static NamingFormat titleCase() {
		return NamingFormatImpls.TITLE_CASE;
	}
}

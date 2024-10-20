package de.siphalor.tweed5.namingformat.api;

public interface NamingFormat {
	String[] splitIntoWords(String name);
	String joinToName(String[] words);

	String name();

	static String convert(String name, NamingFormat from, NamingFormat to) {
		if (from == to) {
			return name;
		}
		return to.joinToName(from.splitIntoWords(name));
	}
}

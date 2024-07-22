package de.siphalor.tweed5.namingformat.api;

public interface NamingFormat {
	String[] splitIntoWords(String name);
	String joinToName(String[] words);

	String name();
}

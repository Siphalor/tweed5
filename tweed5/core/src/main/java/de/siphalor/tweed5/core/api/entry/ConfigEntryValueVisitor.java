package de.siphalor.tweed5.core.api.entry;

public interface ConfigEntryValueVisitor {
	<T> void visitEntry(ConfigEntry<T> entry, T value);

	default <T> boolean enterStructuredEntry(ConfigEntry<T> entry, T value) {
		visitEntry(entry, value);
		return true;
	}

	default boolean enterStructuredSubEntry(String entryKey, String valueKey) {
		return true;
	}

	default void leaveStructuredSubEntry(String entryKey, String valueKey) {
	}

	default <T> void leaveStructuredEntry(ConfigEntry<T> entry, T value) {
	}
}

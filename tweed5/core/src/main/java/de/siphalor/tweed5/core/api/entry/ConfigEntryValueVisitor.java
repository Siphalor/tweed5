package de.siphalor.tweed5.core.api.entry;

public interface ConfigEntryValueVisitor {
	<T> void visitEntry(ConfigEntry<T> entry, T value);

	default <T> boolean enterStructuredEntry(StructuredConfigEntry<T> entry, T value) {
		visitEntry(entry, value);
		return true;
	}

	default boolean enterAddressableStructuredSubEntry(String entryKey, String valueKey, String dataKey) {
		return true;
	}

	default boolean enterStructuredSubEntry(String entryKey, String valueKey) {
		return true;
	}

	default void leaveAddressableStructuredSubEntry(String entryKey, String valueKey, String dataKey) {
	}

	default void leaveStructuredSubEntry(String entryKey, String valueKey) {
	}

	default <T> void leaveStructuredEntry(StructuredConfigEntry<T> entry, T value) {
	}
}

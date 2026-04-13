package de.siphalor.tweed5.core.api.entry;

public interface ConfigEntryValueVisitor {
	<T> void visitEntry(ConfigEntry<T> entry, T value);

	default <T> boolean enterStructuredEntry(StructuredConfigEntry<T> entry, T value) {
		visitEntry(entry, value);
		return true;
	}

	default boolean enterSubEntry(SubEntryKey subEntryKey) {
		return true;
	}

	default void leaveSubEntry(SubEntryKey subEntryKey) {
	}

	default <T> void leaveStructuredEntry(StructuredConfigEntry<T> entry, T value) {
	}
}

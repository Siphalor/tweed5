package de.siphalor.tweed5.core.api.entry;

public interface ConfigEntryValueVisitor {
	<T> void visitEntry(ConfigEntry<T> entry, T value);

	default <T> boolean enterCollectionEntry(ConfigEntry<T> entry, T value) {
		visitEntry(entry, value);
		return true;
	}

	default <T> void leaveCollectionEntry(ConfigEntry<T> entry, T value) {
	}

	default <T> boolean enterCompoundEntry(ConfigEntry<T> entry, T value) {
		visitEntry(entry, value);
		return true;
	}

	default boolean enterCompoundSubEntry(String key) {
		return true;
	}

	default void leaveCompoundSubEntry(String key) {
	}

	default <T> void leaveCompoundEntry(ConfigEntry<T> entry, T value) {
	}
}

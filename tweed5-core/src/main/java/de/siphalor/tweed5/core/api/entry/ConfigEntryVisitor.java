package de.siphalor.tweed5.core.api.entry;

public interface ConfigEntryVisitor {
	void visitEntry(ConfigEntry<?> entry);

	default boolean enterCollectionEntry(ConfigEntry<?> entry) {
		visitEntry(entry);
		return true;
	}

	default void leaveCollectionEntry(ConfigEntry<?> entry) {
	}

	default boolean enterCompoundEntry(ConfigEntry<?> entry) {
		visitEntry(entry);
		return true;
	}

	default boolean enterCompoundSubEntry(String key) {
		return true;
	}

	default void leaveCompoundSubEntry(String key) {
	}

	default void leaveCompoundEntry(ConfigEntry<?> entry) {
	}
}

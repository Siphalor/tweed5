package de.siphalor.tweed5.core.api.entry;

public interface ConfigEntryVisitor {
	void visitEntry(ConfigEntry<?> entry);

	default boolean enterStructuredEntry(ConfigEntry<?> entry) {
		visitEntry(entry);
		return true;
	}

	default boolean enterStructuredSubEntry(String key) {
		return true;
	}

	default void leaveStructuredSubEntry(String key) {
	}

	default void leaveStructuredEntry(ConfigEntry<?> entry) {
	}
}

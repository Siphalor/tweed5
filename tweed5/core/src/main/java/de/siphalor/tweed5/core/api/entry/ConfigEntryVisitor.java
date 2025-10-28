package de.siphalor.tweed5.core.api.entry;

import de.siphalor.tweed5.core.api.Arity;

public interface ConfigEntryVisitor {
	void visitEntry(ConfigEntry<?> entry);

	default boolean enterStructuredEntry(ConfigEntry<?> entry) {
		visitEntry(entry);
		return true;
	}

	default boolean enterStructuredSubEntry(String key, Arity arity) {
		return true;
	}

	default void leaveStructuredSubEntry(String key, Arity arity) {
	}

	default void leaveStructuredEntry(ConfigEntry<?> entry) {
	}
}

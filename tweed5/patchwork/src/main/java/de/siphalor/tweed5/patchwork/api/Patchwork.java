package de.siphalor.tweed5.patchwork.api;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface Patchwork {
	@Contract(pure = true)
	<T extends @Nullable Object> @Nullable T get(PatchworkPartAccess<T> access) throws InvalidPatchworkAccessException;
	@Contract(mutates = "this")
	<T extends @Nullable Object> void set(PatchworkPartAccess<T> access, T value) throws InvalidPatchworkAccessException;

	@Contract(pure = true)
	Patchwork copy();
}

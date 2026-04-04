package de.siphalor.tweed5.serde.extension.api.read.result;

import org.jspecify.annotations.Nullable;

public interface ThrowingFunction<T extends @Nullable Object, R extends @Nullable Object> {
	R apply(T value) throws Exception;
}

package de.siphalor.tweed5.serde.extension.api.read.result;

import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@FunctionalInterface
public interface ThrowingReadFunction<T extends @Nullable Object, R extends @Nullable Object> {
	static <T extends @Nullable Object, R extends @Nullable Object> ThrowingReadFunction<T, R> any(
			TweedReadContext readContext,
			ThrowingFunction<T, R>... functions
	) {
		//noinspection unchecked
		ThrowingReadFunction<T, R>[] wrappedFunctions = new ThrowingReadFunction[functions.length];
		for (int i = 0; i < functions.length; i++) {
			wrappedFunctions[i] = wrap(readContext, functions[i]);
		}
		return any(wrappedFunctions);
	}

	static <T extends @Nullable Object, R extends @Nullable Object> ThrowingReadFunction<T, R> any(
			ThrowingReadFunction<T, R>... functions
	) {
		if (functions.length == 0) {
			throw new IllegalArgumentException("At least one function is required");
		}
		return value -> {
			List<TweedReadIssue> issues = null;
			boolean foundEmpty = false;
			for (ThrowingReadFunction<T, R> function : functions) {
				TweedReadResult<R> result = function.apply(value);
				if (result.hasValue()) {
					return result;
				} else if (!result.isFailed()) {
					foundEmpty = true;
				}
				if (result.hasIssues()) {
					if (issues == null) {
						issues = new ArrayList<>(functions.length);
						issues.addAll(Arrays.asList(result.issues()));
					}
				}
			}
			if (foundEmpty) {
				return TweedReadResult.empty();
			}
			if (issues != null) {
				return TweedReadResult.failed(issues.toArray(new TweedReadIssue[0]));
			}
			throw new IllegalStateException("Unreachable");
		};
	}

	static <T extends @Nullable Object, R extends @Nullable Object> ThrowingReadFunction<T, R> wrap(
			TweedReadContext readContext,
			ThrowingFunction<T, R> function
	) {
		return value -> {
			try {
				return TweedReadResult.ok(function.apply(value));
			} catch (Exception e) {
				return TweedReadResult.failed(TweedReadIssue.error(e, readContext));
			}
		};
	}

	TweedReadResult<R> apply(T value) throws Exception;
}

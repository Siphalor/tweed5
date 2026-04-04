package de.siphalor.tweed5.serde.extension.api.read.result;

import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(doNotUseGetters = true)
public class TweedReadResult<T extends @Nullable Object> {
	private static final TweedReadResult<?> EMPTY = new TweedReadResult<>(null, false, null, Severity.OK);

	private final T value;
	private final boolean hasValue;
	private final TweedReadIssue @Nullable [] issues;
	private final Severity severity;

	public static <T extends @Nullable Object> TweedReadResult<T> ok(T value) {
		return new TweedReadResult<>(value, true, null, Severity.OK);
	}

	public static <T extends @Nullable Object> TweedReadResult<T> empty() {
		//noinspection unchecked
		return (TweedReadResult<T>) EMPTY;
	}

	public static <T extends @Nullable Object> TweedReadResult<T> withIssues(T value, TweedReadIssue... issues) {
		return new TweedReadResult<>(value, true, issues, Severity.OK);
	}

	public static <T extends @Nullable Object> TweedReadResult<T> error(TweedReadIssue... issues) {
		return new TweedReadResult<>(null, false, issues, Severity.ERROR);
	}

	public static <T extends @Nullable Object> TweedReadResult<T> failed(TweedReadIssue... issues) {
		if (issues.length == 0) {
			throw new IllegalArgumentException("At least one issue is required for a failed read result");
		}
		return new TweedReadResult<>(null, false, issues, Severity.FAILURE);
	}

	public T value() {
		if (hasValue) {
			return value;
		}
		throw new IllegalStateException("No value present");
	}

	public boolean hasValue() {
		return hasValue;
	}

	public boolean hasIssues() {
		return issues != null;
	}

	public TweedReadIssue [] issues() {
		if (issues == null) {
			return new TweedReadIssue[0];
		}
		return issues;
	}

	public boolean isError() {
		return severity == Severity.ERROR;
	}

	public boolean isFailed() {
		return severity == Severity.FAILURE;
	}

	public <R extends @Nullable Object> TweedReadResult<R> map(
			ThrowingFunction<T, R> function,
			TweedReadContext readContext
	) {
		return andThen(value -> ok(function.apply(value)), readContext);
	}

	public <R extends @Nullable Object> TweedReadResult<R> andThen(
			ThrowingReadFunction<T, R> function,
			TweedReadContext readContext
	) {
		if (severity != Severity.OK || !hasValue()) {
			//noinspection unchecked
			return (TweedReadResult<R>) this;
		}
		try {
			TweedReadResult<R> innerResult = function.apply(value());
			TweedReadIssue[] issues = combineIssues(issues(), innerResult.issues());
			if (issues.length == 0) {
				return innerResult;
			}
			if (innerResult.isFailed()) {
				return failed(issues);
			} else if (innerResult.isError()) {
				return error(issues);
			} else if (innerResult.hasValue()) {
				return withIssues(innerResult.value(), issues);
			} else {
				return empty();
			}
		} catch (Exception exception) {
			if (hasIssues()) {
				TweedReadIssue[] issues = new TweedReadIssue[issues().length + 1];
				System.arraycopy(issues(), 0, issues, 0, issues().length);
				issues[issues().length] = TweedReadIssue.error(exception, readContext);
				return failed(issues);
			}
			return failed(TweedReadIssue.error(exception, readContext));
		}
	}

	public TweedReadResult<T> catchError(
			ThrowingFunction<TweedReadIssue[], TweedReadResult<T>> errorHandler,
			TweedReadContext readContext
	) {
		if (severity != Severity.ERROR) {
			return this;
		}
		try {
			return errorHandler.apply(issues());
		} catch (Exception exception) {
			TweedReadIssue[] issues = new TweedReadIssue[issues().length + 1];
			System.arraycopy(issues(), 0, issues, 0, issues().length);
			issues[issues().length] = TweedReadIssue.error(exception, readContext);
			return error(issues);
		}
	}

	private static TweedReadIssue[] combineIssues(TweedReadIssue[] a, TweedReadIssue[] b) {
		if (a.length == 0) {
			return b;
		}
		if (b.length == 0) {
			return a;
		}
		TweedReadIssue[] result = new TweedReadIssue[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	@Override
	public String toString() {
		if (severity == Severity.FAILURE) {
			return "TweedReadResult#failed(issues=" + Arrays.toString(issues) + ')';
		} else if (severity == Severity.ERROR) {
			return "TweedReadResult#error(issues=" + Arrays.toString(issues) + ')';
		} else if (hasValue) {
			if (hasIssues()) {
				return "TweedReadResult#withIssues(value=" + value + ", issues=" + Arrays.toString(issues) + ')';
			} else {
				return "TweedReadResult#ok(value=" + value + ')';
			}
		} else {
			return "TweedReadResult#empty";
		}
	}

	private enum Severity {
		OK,
		ERROR,
		FAILURE,
	}
}

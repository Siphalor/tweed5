package de.siphalor.tweed5.defaultextensions.validation.api.result;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationResult<T> {
	private final T value;
	@NotNull
	private final Collection<ValidationIssue> issues;
	private final boolean hasError;

	public static <T> ValidationResult<T> ok(T value) {
		return new ValidationResult<>(value, Collections.emptyList(), false);
	}

	public static <T> ValidationResult<T> withIssues(T value, @NotNull Collection<ValidationIssue> issues) {
		return new ValidationResult<>(value, issues, issuesContainError(issues));
	}

	private static boolean issuesContainError(Collection<ValidationIssue> issues) {
		if (issues.isEmpty()) {
			return false;
		}
		for (ValidationIssue issue : issues) {
			if (issue.level() == ValidationIssueLevel.ERROR) {
				return true;
			}
		}
		return false;
	}

	public ValidationResult<T> andThen(Function<T, ValidationResult<T>> function) {
		if (hasError) {
			return this;
		}

		ValidationResult<T> functionResult = function.apply(value);

		if (functionResult.issues.isEmpty()) {
			if (functionResult.value == value) {
				return this;
			} else if (issues.isEmpty()) {
				return new ValidationResult<>(functionResult.value, Collections.emptyList(), false);
			}
		}

		ArrayList<ValidationIssue> combinedIssues = new ArrayList<>(issues.size() + functionResult.issues.size());
		combinedIssues.addAll(issues);
		combinedIssues.addAll(functionResult.issues);

		return new ValidationResult<>(functionResult.value, combinedIssues, functionResult.hasError);
	}
}

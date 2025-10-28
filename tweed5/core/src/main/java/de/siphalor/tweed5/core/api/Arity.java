package de.siphalor.tweed5.core.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Arity {
	SINGLE(1, 1),
	OPTIONAL(0, 1),
	MULTIPLE(1, Integer.MAX_VALUE),
	ANY(0, Integer.MAX_VALUE),
	;

	private final int min;
	private final int max;
}

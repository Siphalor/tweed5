package de.siphalor.tweed5.utils.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueSymbol {
	private final String displayName;

	@Override
	public String toString() {
		return "UniqueSymbol@" + System.identityHashCode(this) + "{" + displayName + "}";
	}
}

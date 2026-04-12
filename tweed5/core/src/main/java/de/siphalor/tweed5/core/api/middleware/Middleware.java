package de.siphalor.tweed5.core.api.middleware;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Set;

public interface Middleware<M, C> {
	String DEFAULT_START = "$default.start";
	String DEFAULT_END = "$default.end";

	String id();

	default Set<String> mustComeBefore() {
		return Collections.singleton(DEFAULT_END);
	}
	default Set<String> mustComeAfter() {
		return Collections.singleton(DEFAULT_START);
	}

	default M process(M inner, C context) {
		return process(inner);
	}

	@Deprecated
	@ApiStatus.OverrideOnly
	default M process(M inner) {
		return inner;
	}
}

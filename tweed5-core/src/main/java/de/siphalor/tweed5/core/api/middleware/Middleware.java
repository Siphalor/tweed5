package de.siphalor.tweed5.core.api.middleware;

import java.util.Collections;
import java.util.Set;

public interface Middleware<M> {
	String DEFAULT_START = "$default.start";
	String DEFAULT_END = "$default.end";

	String id();

	default Set<String> mustComeBefore() {
		return Collections.singleton(DEFAULT_END);
	}
	default Set<String> mustComeAfter() {
		return Collections.singleton(DEFAULT_START);
	}

	M process(M inner);
}

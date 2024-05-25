package de.siphalor.tweed5.core.api.middleware;

import java.util.Collections;
import java.util.Set;

public interface Middleware<M> {
	String id();

	default Set<String> mustComeBefore() {
		return Collections.emptySet();
	}
	default Set<String> mustComeAfter() {
		return Collections.emptySet();
	}

	M process(M inner);
}

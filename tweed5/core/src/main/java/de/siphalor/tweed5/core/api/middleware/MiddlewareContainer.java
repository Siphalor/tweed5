package de.siphalor.tweed5.core.api.middleware;

import java.util.Collection;

public interface MiddlewareContainer<M, C> extends Middleware<M, C> {
	default void registerAll(Collection<Middleware<M, C>> middlewares) {
		middlewares.forEach(this::register);
	}
	void register(Middleware<M, C> middleware);
	void seal();
	Collection<Middleware<M, C>> middlewares();
}

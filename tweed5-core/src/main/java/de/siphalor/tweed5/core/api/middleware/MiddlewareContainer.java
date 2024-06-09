package de.siphalor.tweed5.core.api.middleware;

import java.util.Collection;

public interface MiddlewareContainer<M> extends Middleware<M> {
	default void registerAll(Collection<Middleware<M>> middlewares) {
		middlewares.forEach(this::register);
	}
	void register(Middleware<M> middleware);
	void seal();
	Collection<Middleware<M>> middlewares();
}

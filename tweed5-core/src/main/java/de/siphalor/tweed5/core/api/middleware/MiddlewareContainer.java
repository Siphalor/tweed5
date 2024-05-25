package de.siphalor.tweed5.core.api.middleware;

public interface MiddlewareContainer<M> extends Middleware<M> {
	void register(Middleware<M> middleware);
	void seal();
}

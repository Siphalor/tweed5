package de.siphalor.tweed5.patchwork.api;

public interface Patchwork<S extends Patchwork<S>> {
	boolean isPatchworkPartDefined(Class<?> patchworkInterface);
	boolean isPatchworkPartSet(Class<?> patchworkInterface);

	S copy();
}

package de.siphalor.tweed5.patchwork.api;

import de.siphalor.tweed5.patchwork.impl.PatchworkFactoryImpl;

public interface PatchworkFactory {
	static Builder builder() {
		return new PatchworkFactoryImpl.Builder();
	}

	Patchwork create();

	interface Builder {
		<T> PatchworkPartAccess<T> registerPart(Class<T> partClass);
		PatchworkFactory build();
	}
}

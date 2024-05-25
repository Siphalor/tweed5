package de.siphalor.tweed5.core.api.extension;

import de.siphalor.tweed5.patchwork.api.Patchwork;

public interface RegisteredExtensionData<U extends Patchwork<U>, E> {
	void set(U patchwork, E extension);
}

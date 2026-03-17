package de.siphalor.tweed5.serde.extension.api;

import de.siphalor.tweed5.patchwork.api.Patchwork;

public interface TweedWriteContext {
	ReadWriteExtension readWriteExtension();
	Patchwork extensionsData();
}

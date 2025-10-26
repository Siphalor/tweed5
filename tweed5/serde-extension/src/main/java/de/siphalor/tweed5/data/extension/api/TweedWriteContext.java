package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.patchwork.api.Patchwork;

public interface TweedWriteContext {
	ReadWriteExtension readWriteExtension();
	Patchwork extensionsData();
}

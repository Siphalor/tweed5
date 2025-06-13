package de.siphalor.tweed5.data.extension.api;

import de.siphalor.tweed5.patchwork.api.Patchwork;

public interface TweedReadContext {
	ReadWriteExtension readWriteExtension();
	Patchwork extensionsData();
}

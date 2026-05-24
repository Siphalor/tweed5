package de.siphalor.tweed5.defaultextensions.pather.impl;

import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import de.siphalor.tweed5.serde.extension.api.TweedWriteContext;

public class PatherExtensionImpl implements PatherExtension {
	@Override
	public String getPath(TweedReadContext context) {
		return context.currentEntryPath().toString();
	}

	@Override
	public String getPath(TweedWriteContext context) {
		return context.currentEntryPath().toString();
	}
}

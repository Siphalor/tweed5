package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import de.siphalor.tweed5.serde.extension.api.TweedWriteContext;
import de.siphalor.tweed5.defaultextensions.pather.impl.PatherExtensionImpl;

public interface PatherExtension extends TweedExtension {
	Class<? extends PatherExtension> DEFAULT = PatherExtensionImpl.class;
	String EXTENSION_ID = "pather";

	@Override
	default String getId() {
		return EXTENSION_ID;
	}

	String getPath(TweedReadContext context);
	String getPath(TweedWriteContext context);
}

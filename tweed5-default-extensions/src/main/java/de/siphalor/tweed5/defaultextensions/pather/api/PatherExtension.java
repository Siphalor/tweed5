package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.pather.impl.PatherExtensionImpl;

public interface PatherExtension extends TweedExtension {
	Class<? extends PatherExtension> DEFAULT = PatherExtensionImpl.class;
}

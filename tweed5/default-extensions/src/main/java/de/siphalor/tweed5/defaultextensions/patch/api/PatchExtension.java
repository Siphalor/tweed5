package de.siphalor.tweed5.defaultextensions.patch.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.patch.impl.PatchExtensionImpl;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public interface PatchExtension extends TweedExtension {
	Class<? extends PatchExtension> DEFAULT = PatchExtensionImpl.class;
	String EXTENSION_ID = "patch";

	@Override
	default String getId() {
		return EXTENSION_ID;
	}

	PatchInfo collectPatchInfo(Patchwork readWriteContextExtensionsData);

	<T extends @Nullable Object> T patch(ConfigEntry<T> entry, T targetValue, T patchValue, PatchInfo patchInfo);
}

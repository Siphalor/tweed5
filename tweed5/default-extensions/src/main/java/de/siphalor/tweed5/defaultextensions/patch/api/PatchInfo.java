package de.siphalor.tweed5.defaultextensions.patch.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;

public interface PatchInfo {
	boolean containsEntry(ConfigEntry<?> entry);
}

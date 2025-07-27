package de.siphalor.tweed5.defaultextensions.patch.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchInfo;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public class PatchInfoImpl implements PatchInfo {
	private final Map<ConfigEntry<?>, @Nullable Void> subPatchInfos = new IdentityHashMap<>();

	@Override
	public boolean containsEntry(ConfigEntry<?> entry) {
		return subPatchInfos.containsKey(entry);
	}

	void addEntry(ConfigEntry<?> entry) {
		subPatchInfos.put(entry, null);
	}
}

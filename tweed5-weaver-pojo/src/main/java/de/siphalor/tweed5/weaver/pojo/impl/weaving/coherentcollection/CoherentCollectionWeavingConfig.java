package de.siphalor.tweed5.weaver.pojo.impl.weaving.coherentcollection;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCoherentCollectionConfigEntry;
import org.jetbrains.annotations.Nullable;

public interface CoherentCollectionWeavingConfig {
	@SuppressWarnings("rawtypes")
	@Nullable
	Class<? extends WeavableCoherentCollectionConfigEntry> coherentCollectionEntryClass();
}

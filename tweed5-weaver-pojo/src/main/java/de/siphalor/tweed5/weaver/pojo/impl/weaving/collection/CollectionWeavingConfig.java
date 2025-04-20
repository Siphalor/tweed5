package de.siphalor.tweed5.weaver.pojo.impl.weaving.collection;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCollectionConfigEntry;
import org.jetbrains.annotations.Nullable;

public interface CollectionWeavingConfig {
	@SuppressWarnings("rawtypes")
	@Nullable
	Class<? extends WeavableCollectionConfigEntry> collectionEntryClass();
}

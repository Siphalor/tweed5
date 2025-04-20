package de.siphalor.tweed5.weaver.pojo.impl.weaving.collection;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCollectionConfigEntry;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Builder
@Value
public class CollectionWeavingConfigImpl implements CollectionWeavingConfig {

	@SuppressWarnings("rawtypes")
	@Nullable
	Class<? extends WeavableCollectionConfigEntry> collectionEntryClass;

	public static CollectionWeavingConfigImpl withOverrides(CollectionWeavingConfig self, CollectionWeavingConfig overrides) {
		return CollectionWeavingConfigImpl.builder()
				.collectionEntryClass(overrides.collectionEntryClass() != null ? overrides.collectionEntryClass() : self.collectionEntryClass())
				.build();
	}
}

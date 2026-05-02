package de.siphalor.tweed5.weaver.pojo.impl.weaving.config;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCollectionConfigEntry;
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Builder
@Value
public class CollectionWeavingConfig {

	@SuppressWarnings("rawtypes")
	@Nullable Class<? extends WeavableCollectionConfigEntry> collectionEntryClass;

	public static CollectionWeavingConfig withOverrides(CollectionWeavingConfig self, CollectionWeavingConfig overrides) {
		return CollectionWeavingConfig.builder()
				.collectionEntryClass(overrides.collectionEntryClass() != null ? overrides.collectionEntryClass() : self.collectionEntryClass())
				.build();
	}
}

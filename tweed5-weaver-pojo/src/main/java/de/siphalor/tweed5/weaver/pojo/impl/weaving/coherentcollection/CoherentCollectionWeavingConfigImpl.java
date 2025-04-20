package de.siphalor.tweed5.weaver.pojo.impl.weaving.coherentcollection;

import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCoherentCollectionConfigEntry;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Builder
@Value
public class CoherentCollectionWeavingConfigImpl implements CoherentCollectionWeavingConfig {

	@SuppressWarnings("rawtypes")
	@Nullable
	Class<? extends WeavableCoherentCollectionConfigEntry> coherentCollectionEntryClass;

	public static CoherentCollectionWeavingConfigImpl withOverrides(CoherentCollectionWeavingConfig self, CoherentCollectionWeavingConfig overrides) {
		return CoherentCollectionWeavingConfigImpl.builder()
				.coherentCollectionEntryClass(overrides.coherentCollectionEntryClass() != null ? overrides.coherentCollectionEntryClass() : self.coherentCollectionEntryClass())
				.build();
	}
}

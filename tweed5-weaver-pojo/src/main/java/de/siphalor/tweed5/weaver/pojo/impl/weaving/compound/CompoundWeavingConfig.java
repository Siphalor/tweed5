package de.siphalor.tweed5.weaver.pojo.impl.weaving.compound;

import de.siphalor.tweed5.namingformat.api.NamingFormat;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import org.jspecify.annotations.Nullable;

public interface CompoundWeavingConfig {
	@Nullable NamingFormat compoundSourceNamingFormat();

	@Nullable NamingFormat compoundTargetNamingFormat();

	@SuppressWarnings("rawtypes")
	@Nullable Class<? extends WeavableCompoundConfigEntry> compoundEntryClass();
}

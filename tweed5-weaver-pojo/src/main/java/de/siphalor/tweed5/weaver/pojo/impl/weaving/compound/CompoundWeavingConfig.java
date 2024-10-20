package de.siphalor.tweed5.weaver.pojo.impl.weaving.compound;

import de.siphalor.tweed5.namingformat.api.NamingFormat;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import org.jetbrains.annotations.Nullable;

public interface CompoundWeavingConfig {
	NamingFormat compoundSourceNamingFormat();

	NamingFormat compoundTargetNamingFormat();

	@SuppressWarnings("rawtypes")
	@Nullable
	Class<? extends WeavableCompoundConfigEntry> compoundEntryClass();
}

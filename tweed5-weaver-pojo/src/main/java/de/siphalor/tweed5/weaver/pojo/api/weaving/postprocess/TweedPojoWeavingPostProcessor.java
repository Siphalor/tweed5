package de.siphalor.tweed5.weaver.pojo.api.weaving.postprocess;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;

public interface TweedPojoWeavingPostProcessor {
	void apply(ConfigEntry<?> configEntry, WeavingContext context);
}
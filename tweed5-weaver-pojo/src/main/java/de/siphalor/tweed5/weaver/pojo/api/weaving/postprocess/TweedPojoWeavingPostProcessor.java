package de.siphalor.tweed5.weaver.pojo.api.weaving.postprocess;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;

public interface TweedPojoWeavingPostProcessor {
	TweedConstructFactory<TweedPojoWeavingPostProcessor> FACTORY =
			TweedConstructFactory.builder(TweedPojoWeavingPostProcessor.class).build();

	void apply(ConfigEntry<?> configEntry, WeavingContext context);
}

package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import org.jetbrains.annotations.ApiStatus;

public interface TweedPojoWeaver extends TweedPojoWeavingFunction {
	TweedConstructFactory<TweedPojoWeaver> FACTORY = TweedConstructFactory.builder(TweedPojoWeaver.class).build();

	@ApiStatus.OverrideOnly
	void setup(SetupContext context);

	interface SetupContext {
		<E> PatchworkPartAccess<E> registerWeavingContextExtensionData(Class<E> dataClass);
	}
}

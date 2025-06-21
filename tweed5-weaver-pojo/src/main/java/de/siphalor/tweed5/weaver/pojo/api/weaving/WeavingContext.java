package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;

@Value
@Builder
public class WeavingContext {
	@Nullable WeavingContext parent;
	@Getter(AccessLevel.NONE)
	WeavingFn weavingFunction;
	ConfigContainer<?> configContainer;
	String[] path;
	Patchwork extensionsData;
	AnnotatedElement annotations;

	public <T> ConfigEntry<T> weaveEntry(ActualType<T> valueType, Patchwork extensionsData, ProtoWeavingContext context) {
		return weavingFunction.weaveEntry(valueType, extensionsData, context);
	}

	public interface WeavingFn {
		<T> ConfigEntry<T> weaveEntry(ActualType<T> valueType, Patchwork extensionsData, ProtoWeavingContext context);
	}
}

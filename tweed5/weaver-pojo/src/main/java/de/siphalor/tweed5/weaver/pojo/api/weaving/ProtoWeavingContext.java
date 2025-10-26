package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProtoWeavingContext {
	@Nullable WeavingContext parent;
	ConfigContainer<?> configContainer;
	String[] path;
	AnnotatedElement annotations;

	public static ProtoWeavingContext subContextFor(
			WeavingContext weavingContext,
			String subPathName,
			AnnotatedElement annotations
	) {
		String[] path = Arrays.copyOf(weavingContext.path(), weavingContext.path().length + 1, String[].class);
		path[path.length - 1] = subPathName;
		return new ProtoWeavingContext(
				weavingContext,
				weavingContext.configContainer(),
				path,
				annotations
		);
	}

	public static ProtoWeavingContext create(ConfigContainer<?> configContainer, AnnotatedElement annotations) {
		return new ProtoWeavingContext(
				null,
				configContainer,
				new String[0],
				annotations
		);
	}
}

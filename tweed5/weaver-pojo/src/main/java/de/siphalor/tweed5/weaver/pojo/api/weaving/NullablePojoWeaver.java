package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.entry.NullableConfigEntryImpl;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Locale;

public class NullablePojoWeaver implements TweedPojoWeavingExtension {
	private @Nullable PatchworkPartAccess<CustomData> customDataAccess;

	@Override
	public void setup(SetupContext context) {
		customDataAccess = context.registerWeavingContextExtensionData(CustomData.class);
	}

	@Override
	public <T> void beforeWeaveEntry(ActualType<T> valueType, Patchwork extensionsData, ProtoWeavingContext context) {
		assert customDataAccess != null;

		Boolean nullable = null;
		for (Annotation annotation : valueType.getAnnotations()) {
			String annotationName = annotation.annotationType().getSimpleName().toLowerCase(Locale.ROOT);
			switch (annotationName) {
				case "nullable":
					nullable = true;
					break;
				case "nonnull":
				case "notnull":
					nullable = false;
					break;
			}
		}

		extensionsData.set(customDataAccess, new CustomData(Boolean.TRUE.equals(nullable)));
	}

	@Override
	public @Nullable <T> ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context) {
		assert customDataAccess != null;

		CustomData customData = context.extensionsData().get(customDataAccess);
		if (customData == null || !customData.nullable) {
			return null;
		}

		customData.nullable(false);
		return new NullableConfigEntryImpl<>(
				context.configContainer(),
				valueType.declaredType(),
				context.weavePseudoEntry(context, "nonNull", context.extensionsData())
		);
	}

	@AllArgsConstructor
	@Setter
	private static class CustomData {
		private boolean nullable;
	}
}

package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.annotation.StringMapWeaving;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableStringMapConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.entry.StringMapConfigEntryImpl;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoWeavingException;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.config.StringMapWeavingConfig;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Supplier;

/**
 * A weaver that weaves classes with the {@link StringMapWeaving} annotation as string map entries.
 * The type must be a {@link Map} with {@link String} keys.
 */
public class StringMapPojoWeaver implements TweedPojoWeavingExtension {
	private static final StringMapWeavingConfig DEFAULT_WEAVING_CONFIG = StringMapWeavingConfig.builder()
			.stringMapEntryClass(StringMapConfigEntryImpl.class)
			.build();

	@Nullable
	private PatchworkPartAccess<StringMapWeavingConfig> weavingConfigAccess;

	@Override
	public void setup(SetupContext context) {
		this.weavingConfigAccess = context.registerWeavingContextExtensionData(StringMapWeavingConfig.class);
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public <T> @Nullable ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context) {
		assert weavingConfigAccess != null;

		List<ActualType<?>> mapTypeParams = valueType.getTypesOfSuperArguments(Map.class);
		if (mapTypeParams == null || mapTypeParams.get(0).declaredType() != String.class) {
			return null;
		}

		try {
			StringMapWeavingConfig weavingConfig = getOrCreateWeavingConfig(context);
			Patchwork newExtensionsData = context.extensionsData().copy();
			newExtensionsData.set(weavingConfigAccess, weavingConfig);

			ActualType<?> valueTypeParam = mapTypeParams.get(1);
			Supplier<Map<String, Object>> constructor = getMapConstructor(valueType);

			ConfigEntry<?> valueEntry = context.weaveEntry(
					valueTypeParam,
					newExtensionsData,
					ProtoWeavingContext.subContextFor(context, "value", valueTypeParam)
			);

			return WeavableStringMapConfigEntry.FACTORY
					.construct(Objects.requireNonNull(weavingConfig.stringMapEntryClass()))
					.typedArg(ConfigContainer.class, context.configContainer())
					.namedArg("mapClass", valueType.declaredType())
					.namedArg("mapConstructor", constructor)
					.namedArg("valueEntry", valueEntry)
					.finish();
		} catch (Exception e) {
			throw new PojoWeavingException("Exception occurred trying to weave string map for class " + valueType, e);
		}
	}

	private StringMapWeavingConfig getOrCreateWeavingConfig(WeavingContext context) {
		assert weavingConfigAccess != null;

		StringMapWeavingConfig parent = context.extensionsData().get(weavingConfigAccess);
		if (parent == null) {
			parent = DEFAULT_WEAVING_CONFIG;
		}

		StringMapWeavingConfig local = createWeavingConfigFromAnnotations(context.annotations());
		if (local == null) {
			return parent;
		}

		return StringMapWeavingConfig.withOverrides(parent, local);
	}

	private @Nullable StringMapWeavingConfig createWeavingConfigFromAnnotations(AnnotatedElement annotations) {
		StringMapWeaving annotation = annotations.getAnnotation(StringMapWeaving.class);
		if (annotation == null) {
			return null;
		}

		StringMapWeavingConfig.StringMapWeavingConfigBuilder builder = StringMapWeavingConfig.builder();
		if (annotation.entryClass() != WeavableStringMapConfigEntry.class) {
			builder.stringMapEntryClass(annotation.entryClass());
		}

		return builder.build();
	}

	private Supplier<Map<String, Object>> getMapConstructor(ActualType<?> type) {
		Class<?> declaredType = type.declaredType();
		if (declaredType == Map.class || declaredType == HashMap.class) {
			return HashMap::new;
		} else if (declaredType == LinkedHashMap.class) {
			return LinkedHashMap::new;
		} else if (declaredType == TreeMap.class) {
			return TreeMap::new;
		}
		try {
			return findCompatibleConstructor(type);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new PojoWeavingException("Could not find no-args constructor for " + type, e);
		}
	}

	@SuppressWarnings("unchecked")
	private Supplier<Map<String, Object>> findCompatibleConstructor(ActualType<?> type) throws
			NoSuchMethodException,
			IllegalAccessException {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		MethodHandle constructor = lookup.findConstructor(type.declaredType(), MethodType.methodType(void.class));
		return () -> {
			try {
				return (Map<String, Object>) constructor.invoke();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}
}

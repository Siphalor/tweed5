package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CollectionWeaving;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCollectionConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.entry.CollectionConfigEntryImpl;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoWeavingException;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.collection.CollectionWeavingConfig;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.collection.CollectionWeavingConfigImpl;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.IntFunction;

public class CollectionPojoWeaver implements TweedPojoWeaver {
	private static final CollectionWeavingConfig DEFAULT_WEAVING_CONFIG = CollectionWeavingConfigImpl.builder()
			.collectionEntryClass(CollectionConfigEntryImpl.class)
			.build();

	private RegisteredExtensionData<WeavingContext.ExtensionsData, CollectionWeavingConfig> weavingConfigAccess;

	@Override
	public void setup(SetupContext context) {
		this.weavingConfigAccess = context.registerWeavingContextExtensionData(CollectionWeavingConfig.class);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public <T> @Nullable ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context) {
		List<ActualType<?>> collectionTypeParams = valueType.getTypesOfSuperArguments(Collection.class);
		if (collectionTypeParams == null) {
			return null;
		}
		try {
			CollectionWeavingConfig weavingConfig = getOrCreateWeavingConfig(context);
			WeavingContext.ExtensionsData newExtensionsData = context.extensionsData().copy();
			weavingConfigAccess.set(newExtensionsData, weavingConfig);

			IntFunction<Collection<Object>> constructor = getCollectionConstructor(valueType);

			WeavableCollectionConfigEntry configEntry = WeavableCollectionConfigEntry.FACTORY
					.construct(Objects.requireNonNull(weavingConfig.collectionEntryClass()))
					.typedArg(valueType.declaredType())
					.typedArg(IntFunction.class, constructor)
					.finish();

			configEntry.elementEntry(context.weaveEntry(
					collectionTypeParams.get(0),
					context.subContextBuilder("element")
							.annotations(collectionTypeParams.get(0))
							.extensionsData(newExtensionsData)
							.build()
			));
			configEntry.seal(context.configContainer());

			return configEntry;
		} catch (Exception e) {
			throw new PojoWeavingException("Exception occurred trying to weave collection for class " + valueType, e);
		}
	}

	private CollectionWeavingConfig getOrCreateWeavingConfig(WeavingContext context) {
		CollectionWeavingConfig parent;
		if (context.extensionsData().isPatchworkPartSet(CollectionWeavingConfig.class)) {
			parent = (CollectionWeavingConfig) context.extensionsData();
		} else {
			parent = DEFAULT_WEAVING_CONFIG;
		}

		CollectionWeavingConfig local = createWeavingConfigFromAnnotations(context.annotations());
		if (local == null) {
			return parent;
		}

		return CollectionWeavingConfigImpl.withOverrides(parent, local);
	}

	private @Nullable CollectionWeavingConfig createWeavingConfigFromAnnotations(AnnotatedElement annotations) {
		CollectionWeaving annotation = annotations.getAnnotation(CollectionWeaving.class);
		if (annotation == null) {
			return null;
		}

		CollectionWeavingConfigImpl.CollectionWeavingConfigImplBuilder builder = CollectionWeavingConfigImpl.builder();
		if (annotation.entryClass() != null) {
			builder.collectionEntryClass(annotation.entryClass());
		}

		return builder.build();
	}

	public IntFunction<Collection<Object>> getCollectionConstructor(ActualType<?> type) {
		if (type.declaredType() == List.class) {
			return ArrayList::new;
		} else if (type.declaredType() == Set.class) {
			return capacity -> new HashSet<>((int) Math.ceil(capacity * 1.4), 0.75F);
		}
		try {
			return findCompatibleConstructor(type);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new PojoWeavingException("could not find no args constructor for " + type, e);
		}
	}

	public IntFunction<Collection<Object>> findCompatibleConstructor(ActualType<?> type) throws
			NoSuchMethodException,
			IllegalAccessException {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		MethodHandle constructor = lookup.findConstructor(type.declaredType(), MethodType.methodType(Void.class));
		return capacity -> {
			try {
				//noinspection unchecked
				return (Collection<Object>) constructor.invoke();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}
}

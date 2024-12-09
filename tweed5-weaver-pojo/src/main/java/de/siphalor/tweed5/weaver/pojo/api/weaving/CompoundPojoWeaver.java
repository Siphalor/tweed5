package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.namingformat.api.NamingFormat;
import de.siphalor.tweed5.namingformat.api.NamingFormatCollector;
import de.siphalor.tweed5.namingformat.api.NamingFormats;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.entry.StaticPojoCompoundConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoClassIntrospector;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoWeavingException;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.compound.CompoundWeavingConfig;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.compound.CompoundWeavingConfigImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * A weaver that weaves classes with the {@link CompoundWeaving} annotation as compound entries.
 */
public class CompoundPojoWeaver implements TweedPojoWeaver {
	private static final CompoundWeavingConfig DEFAULT_WEAVING_CONFIG = CompoundWeavingConfigImpl.builder()
			.compoundSourceNamingFormat(NamingFormats.camelCase())
			.compoundTargetNamingFormat(NamingFormats.camelCase())
			.compoundEntryClass(StaticPojoCompoundConfigEntry.class)
			.build();

	private final NamingFormatCollector namingFormatCollector = new NamingFormatCollector();
	private RegisteredExtensionData<WeavingContext.ExtensionsData, CompoundWeavingConfig> weavingConfigAccess;

	public void setup(SetupContext context) {
		namingFormatCollector.setupFormats();

		this.weavingConfigAccess = context.registerWeavingContextExtensionData(CompoundWeavingConfig.class);
	}

	@Override
	public @Nullable <T> ConfigEntry<T> weaveEntry(Class<T> valueClass, WeavingContext context) {
		if (context.annotations().getAnnotation(CompoundWeaving.class) == null) {
			return null;
		}
		try {
			CompoundWeavingConfig weavingConfig = getOrCreateWeavingConfig(context);
			WeavingContext.ExtensionsData newExtensionsData = context.extensionsData().copy();
			weavingConfigAccess.set(newExtensionsData, weavingConfig);

			PojoClassIntrospector introspector = PojoClassIntrospector.forClass(valueClass);

			WeavableCompoundConfigEntry<T> compoundEntry = instantiateCompoundEntry(introspector, weavingConfig);

			Map<String, PojoClassIntrospector.Property> properties = introspector.properties();
			properties.forEach((name, property) -> {
				if (shouldIncludeCompoundPropertyInWeaving(property)) {
					compoundEntry.registerSubEntry(weaveCompoundSubEntry(property, newExtensionsData, context));
				}
			});

			return compoundEntry;
		} catch (Exception e) {
			throw new PojoWeavingException("Exception occurred trying to weave compound for class " + valueClass.getName(), e);
		}
	}

	private CompoundWeavingConfig getOrCreateWeavingConfig(WeavingContext context) {
		CompoundWeavingConfig parent;
		if (context.extensionsData().isPatchworkPartSet(CompoundWeavingConfig.class)) {
			parent = (CompoundWeavingConfig) context.extensionsData();
		} else {
			parent = DEFAULT_WEAVING_CONFIG;
		}

		CompoundWeavingConfig local = createWeavingConfigFromAnnotations(context.annotations());
		if (local == null) {
			return parent;
		}

		return CompoundWeavingConfigImpl.withOverrides(parent, local);
	}

	private WeavingContext createSubContextForProperty(
			PojoClassIntrospector.Property property,
			String name,
			WeavingContext.ExtensionsData newExtensionsData,
			WeavingContext parentContext
	) {
		return parentContext.subContextBuilder(name)
				.annotations(collectAnnotationsForField(property.field()))
				.extensionsData(newExtensionsData)
				.build();
	}

	private Annotations collectAnnotationsForField(Field field) {
		Annotations annotations = new Annotations();
		annotations.addAnnotationsFrom(ElementType.TYPE, field.getType());
		annotations.addAnnotationsFrom(ElementType.FIELD, field);
		return annotations;
	}

	@Nullable
	private CompoundWeavingConfig createWeavingConfigFromAnnotations(Annotations annotations) {
		CompoundWeaving annotation = annotations.getAnnotation(CompoundWeaving.class);
		if (annotation == null) {
			return null;
		}

		CompoundWeavingConfigImpl.CompoundWeavingConfigImplBuilder builder = CompoundWeavingConfigImpl.builder();
		builder.compoundSourceNamingFormat(NamingFormats.camelCase());
		if (!annotation.namingFormat().isEmpty()) {
			builder.compoundTargetNamingFormat(getNamingFormatById(annotation.namingFormat()));
		}
		if (annotation.entryClass() != WeavableCompoundConfigEntry.class) {
			builder.compoundEntryClass(annotation.entryClass());
		}

		return builder.build();
	}


	@SuppressWarnings("unchecked")
	private <C> WeavableCompoundConfigEntry<C> instantiateCompoundEntry(
			PojoClassIntrospector classIntrospector,
			CompoundWeavingConfig weavingConfig
	) {
		MethodHandle valueConstructor = classIntrospector.noArgsConstructor();
		if (valueConstructor == null) {
			throw new PojoWeavingException("Class " + classIntrospector.type().getName() + " must have public no args constructor");
		}

		//noinspection rawtypes
		Class<? extends WeavableCompoundConfigEntry> annotationEntryClass = weavingConfig.compoundEntryClass();
		@NotNull
		Class<WeavableCompoundConfigEntry<C>> weavableEntryClass = (Class<WeavableCompoundConfigEntry<C>>) (
				annotationEntryClass != null
						? annotationEntryClass
						: StaticPojoCompoundConfigEntry.class
		);
		return WeavableCompoundConfigEntry.instantiate(
				weavableEntryClass,
				(Class<C>) classIntrospector.type(),
				valueConstructor
		);
	}

	private boolean shouldIncludeCompoundPropertyInWeaving(PojoClassIntrospector.Property property) {
		return property.getter() != null && (property.setter() != null || property.isFinal());
	}

	private @NotNull WeavableCompoundConfigEntry.SubEntry weaveCompoundSubEntry(
			PojoClassIntrospector.Property property,
			WeavingContext.ExtensionsData newExtensionsData,
			WeavingContext parentContext
	) {
		String name = convertName(property.field().getName(), (CompoundWeavingConfig) newExtensionsData);
		WeavingContext subContext = createSubContextForProperty(property, name, newExtensionsData, parentContext);

		ConfigEntry<?> subEntry;
		if (property.isFinal()) {
			// TODO
			throw new UnsupportedOperationException("Final config entries are not supported in weaving yet.");
		} else {
			subEntry = subContext.weaveEntry(property.field().getType(), subContext);
		}

		return new StaticPojoCompoundConfigEntry.SubEntry(
				name,
				subEntry,
				property.getter(),
				property.setter()
		);
	}

	private @NotNull String convertName(String name, CompoundWeavingConfig weavingConfig) {
		return NamingFormat.convert(
				name,
				weavingConfig.compoundSourceNamingFormat(),
				weavingConfig.compoundTargetNamingFormat()
		);
	}

	private @NotNull NamingFormat getNamingFormatById(String id) {
		NamingFormat namingFormat = namingFormatCollector.namingFormats().get(id);
		if (namingFormat == null) {
			throw new PojoWeavingException(
					"Naming format \"" + id + "\" is not recognized. Available formats are: " +
							namingFormatCollector.namingFormats().keySet()
			);
		}
		return namingFormat;
	}
}

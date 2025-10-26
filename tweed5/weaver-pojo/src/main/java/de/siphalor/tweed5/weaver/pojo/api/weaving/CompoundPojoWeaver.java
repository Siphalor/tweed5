package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.namingformat.api.NamingFormat;
import de.siphalor.tweed5.namingformat.api.NamingFormatCollector;
import de.siphalor.tweed5.namingformat.api.NamingFormats;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.typeutils.api.annotations.LayeredTypeAnnotations;
import de.siphalor.tweed5.typeutils.api.type.TypeAnnotationLayer;
import de.siphalor.tweed5.weaver.pojo.impl.entry.StaticPojoCompoundConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoClassIntrospector;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoWeavingException;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.compound.CompoundWeavingConfig;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.compound.CompoundWeavingConfigImpl;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A weaver that weaves classes with the {@link CompoundWeaving} annotation as compound entries.
 */
public class CompoundPojoWeaver implements TweedPojoWeavingExtension {
	private static final CompoundWeavingConfig DEFAULT_WEAVING_CONFIG = CompoundWeavingConfigImpl.builder()
			.compoundSourceNamingFormat(NamingFormats.camelCase())
			.compoundTargetNamingFormat(NamingFormats.camelCase())
			.compoundEntryClass(StaticPojoCompoundConfigEntry.class)
			.build();

	private final NamingFormatCollector namingFormatCollector = new NamingFormatCollector();
	@Nullable
	private PatchworkPartAccess<CompoundWeavingConfig> weavingConfigAccess;

	public void setup(SetupContext context) {
		namingFormatCollector.setupFormats();

		this.weavingConfigAccess = context.registerWeavingContextExtensionData(CompoundWeavingConfig.class);
	}

	@Override
	public <T> @Nullable ConfigEntry<T> weaveEntry(ActualType<T> valueType, WeavingContext context) {
		assert weavingConfigAccess != null;

		if (context.annotations().getAnnotation(CompoundWeaving.class) == null) {
			return null;
		}
		try {
			CompoundWeavingConfig weavingConfig = getOrCreateWeavingConfig(context);
			Patchwork newExtensionsData = context.extensionsData().copy();
			newExtensionsData.set(weavingConfigAccess, weavingConfig);

			PojoClassIntrospector introspector = PojoClassIntrospector.forClass(valueType.declaredType());

			List<WeavableCompoundConfigEntry.SubEntry> subEntries = introspector.properties().values().stream()
					.filter(this::shouldIncludeCompoundPropertyInWeaving)
					.map(property -> weaveCompoundSubEntry(property, newExtensionsData, context))
					.collect(Collectors.toList());

			return instantiateCompoundEntry(introspector, weavingConfig, subEntries, context);
		} catch (Exception e) {
			throw new PojoWeavingException("Exception occurred trying to weave compound for class " + valueType, e);
		}
	}

	private CompoundWeavingConfig getOrCreateWeavingConfig(WeavingContext context) {
		assert weavingConfigAccess != null;

		CompoundWeavingConfig parent = context.extensionsData().get(weavingConfigAccess);
		if (parent == null) {
			parent = DEFAULT_WEAVING_CONFIG;
		}

		CompoundWeavingConfig local = createWeavingConfigFromAnnotations(context.annotations());
		if (local == null) {
			return parent;
		}

		return CompoundWeavingConfigImpl.withOverrides(parent, local);
	}

	private @Nullable CompoundWeavingConfig createWeavingConfigFromAnnotations(AnnotatedElement annotations) {
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
			CompoundWeavingConfig weavingConfig,
			List<WeavableCompoundConfigEntry.SubEntry> subEntries,
			WeavingContext weavingContext
	) {
		MethodHandle valueConstructorHandle = classIntrospector.noArgsConstructor();
		if (valueConstructorHandle == null) {
			throw new PojoWeavingException("Class " + classIntrospector.type().getName() + " must have public no args constructor");
		}
		Supplier<?> valueConstructor = () -> {
			try {
				return valueConstructorHandle.invoke();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};

		//noinspection rawtypes
		Class<? extends WeavableCompoundConfigEntry> annotationEntryClass = weavingConfig.compoundEntryClass();
		Class<WeavableCompoundConfigEntry<C>> weavableEntryClass = (Class<WeavableCompoundConfigEntry<C>>) (
				annotationEntryClass != null
						? annotationEntryClass
						: StaticPojoCompoundConfigEntry.class
		);
		return WeavableCompoundConfigEntry.FACTORY.construct(weavableEntryClass)
				.typedArg(ConfigContainer.class, weavingContext.configContainer())
				.typedArg(classIntrospector.type())
				.typedArg(Supplier.class, valueConstructor)
				.namedArg("subEntries",  subEntries)
				.finish();
	}

	private boolean shouldIncludeCompoundPropertyInWeaving(PojoClassIntrospector.Property property) {
		return property.getter() != null && (property.setter() != null || property.isFinal());
	}

	private WeavableCompoundConfigEntry.SubEntry weaveCompoundSubEntry(
			PojoClassIntrospector.Property property,
			Patchwork newExtensionsData,
			WeavingContext context
	) {
		assert weavingConfigAccess != null;
		CompoundWeavingConfig weavingConfig = newExtensionsData.get(weavingConfigAccess);
		assert weavingConfig != null;

		String name = convertName(property.field().getName(), weavingConfig);

		ConfigEntry<?> subEntry;
		if (property.isFinal()) {
			// TODO
			throw new UnsupportedOperationException("Final config entries are not supported in weaving yet.");
		} else {
			subEntry = context.weaveEntry(
					ActualType.ofUsedType(property.field().getAnnotatedType()),
					newExtensionsData,
					ProtoWeavingContext.subContextFor(
							context,
							property.field().getName(),
							collectAnnotationsForField(property.field())
					)
			);
		}

		return new StaticPojoCompoundConfigEntry.SubEntry(
				name,
				subEntry,
				property.getter(),
				property.setter()
		);
	}

	private String convertName(String name, CompoundWeavingConfig weavingConfig) {
		// Always non-null at this point, since null values were already defaulted
		//noinspection DataFlowIssue
		return NamingFormat.convert(
				name,
				weavingConfig.compoundSourceNamingFormat(),
				weavingConfig.compoundTargetNamingFormat()
		);
	}

	private NamingFormat getNamingFormatById(String id) {
		NamingFormat namingFormat = namingFormatCollector.namingFormats().get(id);
		if (namingFormat == null) {
			throw new PojoWeavingException(
					"Naming format \"" + id + "\" is not recognized. Available formats are: " +
							namingFormatCollector.namingFormats().keySet()
			);
		}
		return namingFormat;
	}

	private AnnotatedElement collectAnnotationsForField(Field field) {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, field.getType());
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, field);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, field.getAnnotatedType());
		return annotations;
	}
}

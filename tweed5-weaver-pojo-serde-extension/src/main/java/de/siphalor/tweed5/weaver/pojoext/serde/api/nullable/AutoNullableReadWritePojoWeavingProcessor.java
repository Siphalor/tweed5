package de.siphalor.tweed5.weaver.pojoext.serde.api.nullable;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.weaving.ProtoWeavingContext;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import lombok.Value;
import lombok.var;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;

public class AutoNullableReadWritePojoWeavingProcessor implements TweedPojoWeavingExtension {
	private final ReadWriteExtension readWriteExtension;
	private @Nullable PatchworkPartAccess<CustomData> customDataAccess;

	@ApiStatus.Internal
	public AutoNullableReadWritePojoWeavingProcessor(ConfigContainer<?> configContainer) {
		readWriteExtension = configContainer.extension(ReadWriteExtension.class)
				.orElseThrow(() -> new IllegalStateException(
						"You must register a " + ReadWriteExtension.class.getSimpleName()
								+ " to use the " + getClass().getSimpleName()
				));
	}

	@Override
	public void setup(SetupContext context) {
		customDataAccess = context.registerWeavingContextExtensionData(CustomData.class);
	}

	@Override
	public <T> void beforeWeaveEntry(ActualType<T> valueType, Patchwork extensionsData, ProtoWeavingContext context) {
		assert customDataAccess != null;

		AutoReadWriteNullability innerNullability = null;

		var behavior = context.annotations().getAnnotation(AutoNullableReadWriteBehavior.class);
		if (behavior != null) {
			innerNullability = behavior.defaultNullability();
		}

		AutoReadWriteNullability currentNullability = null;
		CustomData customData = extensionsData.get(customDataAccess);
		if (customData != null) {
			if (customData.innerDefaultNullability() != null) {
				extensionsData.set(customDataAccess, new CustomData(
						customData.innerDefaultNullability(),
						innerNullability
				));
				return;
			}
			currentNullability = customData.defaultNullability();
		}

		if (innerNullability != null) {
			extensionsData.set(customDataAccess, new CustomData(currentNullability, innerNullability));
		}
	}

	@Override
	public <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {
		if (getNullability(valueType, context) == AutoReadWriteNullability.NULLABLE) {
			var definedEntryReader = readWriteExtension.getDefinedEntryReader(configEntry);
			if (definedEntryReader != null) {
				readWriteExtension.setEntryReader(
						configEntry,
						new TweedEntryReaderWriterImpls.NullableReader<>(definedEntryReader)
				);
			}
			var definedEntryWriter = readWriteExtension.getDefinedEntryWriter(configEntry);
			if (definedEntryWriter != null) {
				readWriteExtension.setEntryWriter(
						configEntry,
						new TweedEntryReaderWriterImpls.NullableWriter<>(definedEntryWriter)
				);
			}
		}
	}

	private <T> AutoReadWriteNullability getNullability(ActualType<T> valueType, WeavingContext context) {
		if (valueType.declaredType().isPrimitive()) {
			return AutoReadWriteNullability.NON_NULL;
		}

		Annotation[] annotations = context.annotations().getAnnotations();
		for (int i = annotations.length - 1; i >= 0; i--) {
			String typeName =  annotations[i].annotationType().getSimpleName();
			if ("nullable".equalsIgnoreCase(typeName)) {
				return AutoReadWriteNullability.NULLABLE;
			} else if ("nonnull".equalsIgnoreCase(typeName) || "notnull".equalsIgnoreCase(typeName)) {
				return AutoReadWriteNullability.NON_NULL;
			}
		}
		return getDefaultNullability(context.extensionsData());
	}

	private AutoReadWriteNullability getDefaultNullability(Patchwork extensionsData) {
		assert customDataAccess != null;

		CustomData customData = extensionsData.get(customDataAccess);
		if (customData != null && customData.defaultNullability() != null) {
			return customData.defaultNullability();
		}
		return AutoReadWriteNullability.NON_NULL;
	}

	@Value
	private static class CustomData {
		@Nullable AutoReadWriteNullability defaultNullability;
		@Nullable AutoReadWriteNullability innerDefaultNullability;
	}
}

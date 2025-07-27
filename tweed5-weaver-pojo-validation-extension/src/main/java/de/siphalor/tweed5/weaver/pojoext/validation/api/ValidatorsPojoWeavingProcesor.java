package de.siphalor.tweed5.weaver.pojoext.validation.api;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.ConfigEntryValidator;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import de.siphalor.tweed5.weaver.pojoext.validation.api.validators.WeavableConfigEntryValidator;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

public class ValidatorsPojoWeavingProcesor implements TweedPojoWeavingExtension {
	private final ValidationExtension validationExtension;

	@ApiStatus.Internal
	public ValidatorsPojoWeavingProcesor(ConfigContainer<?> configContainer) {
		validationExtension = configContainer.extension(ValidationExtension.class)
				.orElseThrow(() -> new IllegalStateException(
						"You must register a " + ValidationExtension.class.getSimpleName()
								+ " to use " + getClass().getSimpleName()
				));
	}

	@Override
	public void setup(SetupContext context) {

	}

	@Override
	public <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {
		Validator[] validatorAnnotations = context.annotations().getAnnotationsByType(Validator.class);
		ConfigEntryValidator[] validators = Arrays.stream(validatorAnnotations)
				.map(validatorAnnotation -> createValidatorFromAnnotation(configEntry, validatorAnnotation))
				.toArray(ConfigEntryValidator[]::new);
		validationExtension.addValidators(configEntry, validators);
	}

	private ConfigEntryValidator createValidatorFromAnnotation(
			ConfigEntry<?> configEntry,
			Validator validatorAnnotation
	) {
		return WeavableConfigEntryValidator.FACTORY.construct(validatorAnnotation.value())
				.typedArg(ConfigEntry.class, configEntry)
				.namedArg("config", validatorAnnotation.config())
				.finish();
	}
}

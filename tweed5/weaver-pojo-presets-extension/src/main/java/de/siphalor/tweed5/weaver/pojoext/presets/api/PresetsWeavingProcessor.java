package de.siphalor.tweed5.weaver.pojoext.presets.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import lombok.extern.apachecommons.CommonsLog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@CommonsLog
public class PresetsWeavingProcessor implements TweedPojoWeavingExtension {
	private static final int REQUIRED_FIELD_MODIFIERS = Modifier.STATIC | Modifier.FINAL;

	@Override
	public void setup(SetupContext context) {
	}

	@Override
	public <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {
		Map<String, T> presets = new HashMap<>();
		for (Field field : valueType.declaredType().getFields()) {
			Preset presetAnnotation = field.getAnnotation(Preset.class);
			if (presetAnnotation == null) {
				continue;
			}

			if ((field.getModifiers() & REQUIRED_FIELD_MODIFIERS) != REQUIRED_FIELD_MODIFIERS) {
				log.warn(
						"@Preset field " + field.getName() + " in class " + field.getDeclaringClass().getName()
								+ " is not static and final, skipping preset"
				);
				continue;
			}

			if (presets.containsKey(presetAnnotation.value())) {
				log.warn(
						"Duplicate preset name " + presetAnnotation.value() + " in class "
								+ field.getDeclaringClass().getName() + ", skipping preset"
				);
				presets.remove(presetAnnotation.value());
				continue;
			}

			if (!configEntry.valueClass().isAssignableFrom(field.getType())) {
				log.warn(
						"@Preset field " + field.getName() + " in class " + field.getDeclaringClass().getName()
								+ " has incompatible type, skipping preset"
				);
				continue;
			}

			try {
				//noinspection unchecked
				presets.put(presetAnnotation.value(), (T) field.get(null));
			} catch (IllegalAccessException e) {
				log.warn(
						"Failed to access preset field " + field.getName() + " in class "
								+ field.getDeclaringClass().getName(),
						e
				);
			}
		}

		if (!presets.isEmpty()) {
			PresetsExtension presetsExtension = configEntry.container().extension(PresetsExtension.class)
					.orElseThrow(() -> new IllegalStateException("PresetsExtension not declared in config container"));
			presets.forEach((name, value) -> presetsExtension.presetValue(configEntry, name, value));
		}
	}
}

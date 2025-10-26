package de.siphalor.tweed5.weaver.pojoext.attributes.api;

import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Function;

public class AttributesPojoWeavingProcessor implements TweedPojoWeavingExtension {
	AttributesExtension attributesExtension;

	@ApiStatus.Internal
	public AttributesPojoWeavingProcessor(ConfigContainer<?> configContainer) {
		attributesExtension = configContainer.extension(AttributesExtension.class)
				.orElseThrow(() -> new IllegalStateException(
						"You must register a " + AttributesExtension.class.getSimpleName()
								+ " to use the " + getClass().getSimpleName()
				));
	}

	@Override
	public void setup(SetupContext context) {

	}

	@Override
	public <T> void afterWeaveEntry(ActualType<T> valueType, ConfigEntry<T> configEntry, WeavingContext context) {
		val attributeAnnotations = context.annotations().getAnnotationsByType(Attribute.class);
		val attributes = collectAttributesFromAnnotations(attributeAnnotations, Attribute::key, Attribute::values);
		attributes.forEach((key, values) -> attributesExtension.setAttribute(configEntry, key, values));

		val attributeDefaultAnnotations = context.annotations().getAnnotationsByType(AttributeDefault.class);
		val attributeDefaults = collectAttributesFromAnnotations(
				attributeDefaultAnnotations,
				AttributeDefault::key,
				AttributeDefault::defaultValue
		);
		attributeDefaults.forEach((key, values) -> attributesExtension.setAttributeDefault(configEntry, key, values));
	}

	private <T> Map<String, List<String>> collectAttributesFromAnnotations(
			T[] annotations,
			Function<T, String> keyGetter,
			Function<T, String[]> valueGetter
	) {
		if (annotations.length == 0) {
			return Collections.emptyMap();
		}

		Map<String, List<String>> attributes;
		if (annotations.length == 1) {
			return Collections.singletonMap(
					keyGetter.apply(annotations[0]),
					Arrays.asList(valueGetter.apply(annotations[0]))
			);
		} else if (annotations.length <= 12) {
			attributes = new TreeMap<>();
		} else {
			attributes = new HashMap<>();
		}

		for (T annotation : annotations) {
			attributes.computeIfAbsent(keyGetter.apply(annotation), k -> new ArrayList<>())
					.addAll(Arrays.asList(valueGetter.apply(annotation)));
		}

		return attributes;
	}
}

package de.siphalor.tweed5.coat.bridge.api;

import de.siphalor.coat.util.EnumeratedMaterial;
import de.siphalor.tweed5.coat.bridge.api.mapping.TweedCoatMapper;
import de.siphalor.tweed5.coat.bridge.impl.TweedCoatMappersImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedCoatMappers {
	public static TweedCoatMapper<Byte> byteTextMapper() {
		return TweedCoatMappersImpl.BYTE_TEXT_MAPPER;
	}

	public static TweedCoatMapper<Short> shortTextMapper() {
		return TweedCoatMappersImpl.SHORT_TEXT_MAPPER;
	}

	public static TweedCoatMapper<Integer> integerTextMapper() {
		return TweedCoatMappersImpl.INTEGER_TEXT_MAPPER;
	}

	public static TweedCoatMapper<Long> longTextMapper() {
		return TweedCoatMappersImpl.LONG_TEXT_MAPPER;
	}

	public static TweedCoatMapper<Float> floatTextMapper() {
		return TweedCoatMappersImpl.FLOAT_TEXT_MAPPER;
	}

	public static TweedCoatMapper<Double> doubleTextMapper() {
		return TweedCoatMappersImpl.DOUBLE_TEXT_MAPPER;
	}

	public static TweedCoatMapper<Boolean> booleanCheckboxMapper() {
		return TweedCoatMappersImpl.BOOLEAN_CHECKBOX_MAPPER;
	}

	public static TweedCoatMapper<String> stringTextMapper() {
		return TweedCoatMappersImpl.STRING_TEXT_MAPPER;
	}

	public static <T extends Enum<T>> TweedCoatMapper<T> enumCycleButtonMapper() {
		//noinspection unchecked
		return (TweedCoatMapper<T>) TweedCoatMappersImpl.ENUM_CYCLE_BUTTON_MAPPER;
	}

	public static <T> TweedCoatMapper<T> enumeratedMaterialCycleButtonMapper(
			Class<T> valueClass,
			EnumeratedMaterial<T> material
	) {
		return new TweedCoatMappersImpl.EnumeratedMaterialCycleButtonMapper<>(valueClass, material);
	}

	public static TweedCoatMapper<Object> compoundCategoryMapper() {
		return TweedCoatMappersImpl.COMPOUND_CATEGORY_MAPPER;
	}

	public static <T> TweedCoatMapper<T> convertingTextMapper(
			Class<T> valueClass,
			Function<T, String> textMapper,
			Function<String, T> textParser
	) {
		//noinspection unchecked
		return TweedCoatMappersImpl.convertingTextMapper(new Class[]{valueClass}, textMapper, textParser);
	}
}

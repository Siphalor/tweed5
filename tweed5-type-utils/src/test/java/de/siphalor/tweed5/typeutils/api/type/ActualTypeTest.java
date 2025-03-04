package de.siphalor.tweed5.typeutils.api.type;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ActualTypeTest {

	@Test
	void ofClass() {
		assertThat(ActualType.ofClass(TypeTestClass.class))
				.isNotNull()
				.satisfies(
						type -> assertThat(type.declaredType()).isEqualTo(TypeTestClass.class),
						type -> assertThat(type.getAnnotation(TestAnnotation.class)).isNotNull()
								.extracting(TestAnnotation::value).isEqualTo("classy"),
						type -> assertThat(type.usedType()).isNull()
				);
	}

	@SneakyThrows
	@Test
	void ofUsedTypeSimpleAnnotated() {
		ActualType<?> actualType = ActualType.ofUsedType(
				TypeTestClass.class.getField("missingParamMap").getAnnotatedType()
		);

		assertThat(actualType.declaredType()).isEqualTo(Map.class);
	}

	@SneakyThrows
	@Test
	void ofUsedTypeParameterized() {
		ActualType<?> actualType = ActualType.ofUsedType(
				TypeTestClass.class.getField("wildcardParamMap").getAnnotatedType()
		);

		assertThat(actualType.declaredType()).isEqualTo(Map.class);
	}

	@SneakyThrows
	@Test
	void parameters() {
		ActualType<?> stringListType = ActualType.ofUsedType(
				TypeTestClass.class.getField("stringList").getAnnotatedType()
		);

		assertThat(stringListType.parameters())
				.singleElement()
				.satisfies(
						type -> assertThat(type.declaredType()).isEqualTo(String.class),
						type -> assertThat(type.getAnnotation(TestAnnotation.class))
								.isNotNull()
								.extracting(TestAnnotation::value)
								.isEqualTo("hi")
				);
	}

	@ParameterizedTest
	@ValueSource(classes = {List.class, Map.class, Integer.class})
	void getTypesOfSuperArgumentsNotInherited(Class<?> targetType) {
		ActualType<String> actualType = ActualType.ofClass(String.class);

		assertThat(actualType.getTypesOfSuperArguments(targetType)).isNull();
	}

	@Test
	void getTypesOfSuperArgumentsInheritedWithoutParameters() {
		ActualType<String> actualType = ActualType.ofClass(String.class);

		assertThat(actualType.getTypesOfSuperArguments(CharSequence.class)).isNotNull().isEmpty();
	}

	@SneakyThrows
	@ParameterizedTest
	@ValueSource(strings = {"missingParamMap", "wildcardParamMap"})
	void getTypesOfSuperArgumentsMissingParameters(String field) {
		ActualType<?> actualType = ActualType.ofUsedType(
				TypeTestClass.class.getField(field).getAnnotatedType()
		);

		assertThat(actualType.getTypesOfSuperArguments(Map.class))
				.satisfiesExactly(
						t -> assertThat(t.declaredType()).isEqualTo(Object.class),
						t -> assertThat(t.declaredType()).isEqualTo(Object.class)
				);
	}

	@SneakyThrows
	@ParameterizedTest
	@ValueSource(classes = {List.class, AbstractCollection.class, Collection.class, Iterable.class})
	void getTypesOfSuperArgumentSimpleList(Class<?> targetType) {
		ActualType<?> stringListType = ActualType.ofUsedType(
				TypeTestClass.class.getField("stringList").getAnnotatedType()
		);

		assertThat(stringListType.getTypesOfSuperArguments(targetType)).singleElement().satisfies(
				type -> assertThat(type.getAnnotation(TestAnnotation.class))
						.asInstanceOf(type(TestAnnotation.class))
						.extracting(TestAnnotation::value)
						.isEqualTo("hi"),
				type -> assertThat(type.declaredType()).isEqualTo(String.class)
		);
	}

	@SneakyThrows
	@ParameterizedTest
	@CsvSource({
			"useMap, elemUse",
			"declMap, elemDecl",
			"subValueMap, subType",
			"valueMap, baseType",
	})
	void getTypesOfSuperArgumentMapOverride(String field, String expectedAnnoValue) {
		ActualType<?> actualType = ActualType.ofUsedType(
				TypeTestClass.class.getField(field).getAnnotatedType()
		);

		assertThat(actualType.getTypesOfSuperArguments(Map.class)).satisfiesExactly(
				type -> assertThat(type.declaredType()).isEqualTo(String.class),
				type -> assertThat(type).satisfies(
						t -> assertThat(t.getAnnotation(TestAnnotation.class))
								.asInstanceOf(type(TestAnnotation.class))
								.extracting(TestAnnotation::value)
								.isEqualTo("list"),
						t -> assertThat(t.declaredType()).isEqualTo(List.class),
						t -> assertThat(t.parameters())
								.singleElement()
								.extracting(v -> v.getAnnotation(TestAnnotation.class))
								.as("List element type should have correct annotation")
								.asInstanceOf(type(TestAnnotation.class))
								.extracting(TestAnnotation::value)
								.isEqualTo(expectedAnnoValue)
				)
		);
	}

	@SneakyThrows
	@Test
	void getTypesOfSuperArgumentMapWildcardBounded() {
		ActualType<?> actualType = ActualType.ofUsedType(
				TypeTestClass.class.getField("wildcardBoundedParamMap").getAnnotatedType()
		);

		assertThat(actualType.getTypesOfSuperArguments(Map.class)).satisfiesExactly(
				type -> assertThat(type.declaredType()).isEqualTo(CharSequence.class),
				type -> assertThat(type.declaredType()).isEqualTo(Number.class)
		);
	}

	@TestAnnotation("classy")
	@SuppressWarnings("unused")
	static class TypeTestClass {
		public ArrayList<@TestAnnotation("hi") String> stringList;
		public String2ValueMultimap<@TestAnnotation("elemUse") SubValue> useMap;
		public String2ValueMultimap<SubValue> declMap;
		public Map<String, @TestAnnotation("list") List<SubValue>> subValueMap;
		public Map<String, @TestAnnotation("list") List<Value>> valueMap;
		@SuppressWarnings("rawtypes")
		public Map missingParamMap;
		public Map<?, ?> wildcardParamMap;
		public Map<? extends CharSequence, ? extends Number> wildcardBoundedParamMap;
	}

	interface String2ValueMultimap<V extends Value>
			extends Map<String, @TestAnnotation("list") List<@TestAnnotation("elemDecl") V>> {
	}

	@TestAnnotation("subType")
	interface SubValue extends Value {}

	@TestAnnotation("baseType")
	interface Value {}
}
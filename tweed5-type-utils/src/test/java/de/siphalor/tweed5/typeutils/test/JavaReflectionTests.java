package de.siphalor.tweed5.typeutils.test;

import de.siphalor.tweed5.typeutils.api.type.TestAnnotation;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

/**
 * Various test to demonstrate the workings of Java's reflection type system
 */
public class JavaReflectionTests {
	@SneakyThrows
	@Test
	void parameterizedType() {
		Field intsField = TestClass.class.getField("ints");
		assertThat(intsField.getGenericType())
				.asInstanceOf(type(ParameterizedType.class))
				.extracting(ParameterizedType::getRawType)
				.isInstanceOf(Class.class)
				.isEqualTo(List.class);
		assertThat(intsField.getAnnotatedType())
				.asInstanceOf(type(AnnotatedParameterizedType.class))
				.satisfies(
						type -> Assertions.assertThat(type.getAnnotation(TestAnnotation.class)).isNotNull(),
						type -> assertThat(type.getAnnotatedActualTypeArguments())
								.singleElement()
								.isInstanceOf(AnnotatedParameterizedType.class)
								.satisfies(arg -> Assertions.assertThat(arg.getAnnotation(TestAnnotation.class)).isNull())
				);
		assertThat(TestClass.class.getField("string").getGenericType()).isInstanceOf(Class.class)
				.isEqualTo(String.class);
	}

	@Test
	void repeatableAnnotation() {
		assertThat(TestClass.class.getAnnotationsByType(TestAnnotation.class)).hasSize(3);
		assertThat(TestClass.class.getAnnotations())
				.doesNotHaveAnyElementsOfTypes(TestAnnotation.class);
		assertThat(TestClass.class.getAnnotation(TestAnnotation.class)).isNull();
	}

	@TestAnnotation("a")
	@TestAnnotation("b")
	@TestAnnotation("c")
	static class TestClass {
		public String string;
		@TestAnnotation("x")
		public List<List<@TestAnnotation("y") Integer>> ints;
		public TestCollection<Long, String> collection;
	}

	interface TestCollection<A, B> extends Collection<Map<A, B>> {}
}

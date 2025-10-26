package de.siphalor.tweed5.annotationinheritance.api;

import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.array;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class AnnotationInheritanceAwareAnnotatedElementTest {
	@Test
	void getAnnotationAForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotation(A.class))
				.isNotNull()
				.extracting(A::value)
				.isEqualTo(1);
	}

	@Test
	void getAnnotationsByTypeAForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotationsByType(A.class))
				.singleElement()
				.extracting(A::value)
				.isEqualTo(1);
	}

	@Test
	void getAnnotationBForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotation(B.class))
				.isNotNull()
				.extracting(B::value)
				.isEqualTo(2);
	}

	@Test
	void getAnnotationsByTypeBForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotationsByType(B.class))
				.singleElement()
				.extracting(B::value)
				.isEqualTo(2);
	}

	@Test
	void getAnnotationCForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotation(C.class))
				.isNotNull()
				.extracting(C::value)
				.isEqualTo(10);
	}

	@Test
	void getAnnotationsByTypeCForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotationsByType(C.class))
				.singleElement()
				.extracting(C::value)
				.isEqualTo(10);
	}

	@Test
	void getAnnotationRForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotation(R.class)).isNull();
	}

	@Test
	void getAnnotationsByTypeRForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotationsByType(R.class))
				.satisfiesExactly(
						r -> assertThat(r.value()).isEqualTo(4),
						r -> assertThat(r.value()).isEqualTo(2),
						r -> assertThat(r.value()).isEqualTo(3),
						r -> assertThat(r.value()).isEqualTo(10)
				);
	}

	@Test
	void getAnnotationRsForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		Rs rs = element.getAnnotation(Rs.class);
		assertThat(rs)
				.isNotNull()
				.extracting(Rs::value)
				.asInstanceOf(array(R[].class))
				.satisfiesExactly(
						r -> assertThat(r.value()).isEqualTo(4),
						r -> assertThat(r.value()).isEqualTo(2),
						r -> assertThat(r.value()).isEqualTo(3),
						r -> assertThat(r.value()).isEqualTo(10)
				);
	}

	@Test
	void getAnnotationsByTypeRsForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotationsByType(Rs.class))
				.singleElement()
				.extracting(Rs::value)
				.asInstanceOf(array(R[].class))
				.satisfiesExactly(
						r -> assertThat(r.value()).isEqualTo(4),
						r -> assertThat(r.value()).isEqualTo(2),
						r -> assertThat(r.value()).isEqualTo(3),
						r -> assertThat(r.value()).isEqualTo(10)
				);
	}

	@Test
	void getAnnotationsForTarget1() {
		var element = new AnnotationInheritanceAwareAnnotatedElement(Target1.class);
		assertThat(element.getAnnotations())
				.satisfiesExactlyInAnyOrder(
						a -> assertThat(a).isInstanceOf(BequeatherThree.class),
						a -> assertThat(a).isInstanceOf(BequeatherTwo.class),
						a -> assertThat(a).isInstanceOf(BequeatherOne.class),
						a -> assertThat(a).asInstanceOf(type(A.class)).extracting(A::value).isEqualTo(1),
						a -> assertThat(a).asInstanceOf(type(B.class)).extracting(B::value).isEqualTo(2),
						a -> assertThat(a).asInstanceOf(type(C.class)).extracting(C::value).isEqualTo(10),
						a -> assertThat(a).asInstanceOf(type(Rs.class))
								.extracting(Rs::value)
								.asInstanceOf(array(R[].class))
								.satisfiesExactly(
										r -> assertThat(r).extracting(R::value).isEqualTo(4),
										r -> assertThat(r).extracting(R::value).isEqualTo(2),
										r -> assertThat(r).extracting(R::value).isEqualTo(3),
										r -> assertThat(r).extracting(R::value).isEqualTo(10)
								)
				);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
	public @interface A {
		int value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
	public @interface B {
		int value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
	public @interface C {
		int value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
	@Repeatable(Rs.class)
	public @interface R {
		int value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
	public @interface Rs {
		R[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
	@A(1)
	@B(1)
	@C(1)
	@R(1)
	@R(2)
	@AnnotationInheritance(passOn = {A.class, B.class, C.class, R.class})
	public @interface BequeatherOne {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@BequeatherOne
	@B(2)
	@R(2)
	@R(3)
	@AnnotationInheritance(passOn = {BequeatherOne.class, B.class, R.class}, override = R.class)
	public @interface BequeatherTwo {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@R(4)
	@AnnotationInheritance(passOn = {R.class, A.class})
	public @interface BequeatherThree {}

	@BequeatherThree
	@BequeatherTwo
	@C(10)
	@R(10)
	public static class Target1 {}
}

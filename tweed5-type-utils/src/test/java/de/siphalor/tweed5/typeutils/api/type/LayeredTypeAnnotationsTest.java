package de.siphalor.tweed5.typeutils.api.type;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.array;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class LayeredTypeAnnotationsTest {

	@Test
	void appendInSameLayer() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, A.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, B.class);

		assertThat(annotations.getAnnotation(TestAnnotation.class))
				.isNotNull()
				.extracting(TestAnnotation::value)
				.isEqualTo("a");
	}

	@Test
	void appendInDifferentLayer() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, B.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, A.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, B.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, B.class);

		assertThat(annotations.getAnnotation(TestAnnotation.class))
				.isNotNull()
				.extracting(TestAnnotation::value)
				.isEqualTo("a");
	}

	@Test
	void prependInSameLayer() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.prependLayerFrom(TypeAnnotationLayer.TYPE_USE, A.class);
		annotations.prependLayerFrom(TypeAnnotationLayer.TYPE_USE, B.class);

		assertThat(annotations.getAnnotation(TestAnnotation.class))
				.isNotNull()
				.extracting(TestAnnotation::value)
				.isEqualTo("b");
	}

	@Test
	void prependInDifferentLayer() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.prependLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, B.class);
		annotations.prependLayerFrom(TypeAnnotationLayer.TYPE_USE, B.class);
		annotations.prependLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, B.class);
		annotations.prependLayerFrom(TypeAnnotationLayer.TYPE_USE, A.class);

		assertThat(annotations.getAnnotation(TestAnnotation.class))
				.isNotNull()
				.extracting(TestAnnotation::value)
				.isEqualTo("a");
	}

	@Test
	void getAnnotationOverrideRepeatableWins() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, Repeated.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, B.class);

		assertThat(annotations.getAnnotation(TestAnnotation.class))
				.isNotNull()
				.extracting(TestAnnotation::value)
				.isEqualTo("b");
		assertThat(annotations.getAnnotation(TestAnnotations.class)).isNull();
	}

	@Test
	void getAnnotationOverrideRepeatableContainerWins() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, B.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, Repeated.class);

		assertThat(annotations.getAnnotation(TestAnnotation.class)).isNull();
		assertThat(annotations.getAnnotation(TestAnnotations.class))
				.isNotNull()
				.extracting(TestAnnotations::value)
				.asInstanceOf(array(TestAnnotation[].class))
				.satisfiesExactly(
						a -> assertThat(a.value()).isEqualTo("r1"),
						a -> assertThat(a.value()).isEqualTo("r2")
				);
	}

	@Test
	void getAnnotationsOverrideRepeatableWins() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, Repeated.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, B.class);

		assertThat(annotations.getAnnotations()).singleElement()
				.asInstanceOf(type(TestAnnotation.class))
				.extracting(TestAnnotation::value)
				.isEqualTo("b");
	}

	@Test
	void getAnnotationsOverrideRepeatableContainerWins() {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, B.class);
		annotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, Repeated.class);

		assertThat(annotations.getAnnotations()).singleElement()
				.asInstanceOf(type(TestAnnotations.class))
				.extracting(TestAnnotations::value)
				.asInstanceOf(array(TestAnnotation[].class))
				.satisfiesExactly(
						a -> assertThat(a.value()).isEqualTo("r1"),
						a -> assertThat(a.value()).isEqualTo("r2")
				);
	}

	@TestAnnotation("r1")
	@TestAnnotation("r2")
	private static class Repeated {
	}

	@TestAnnotation("a")
	private static class A {
	}

	@TestAnnotation("b")
	private static class B {
	}
}
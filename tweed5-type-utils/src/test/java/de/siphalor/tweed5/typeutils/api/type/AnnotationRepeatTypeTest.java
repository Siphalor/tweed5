package de.siphalor.tweed5.typeutils.api.type;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.ValueSources;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationRepeatTypeTest {

	@ParameterizedTest
	@ValueSource(classes = {Override.class, ParameterizedTest.class})
	void getTypeSimple(Class<? extends Annotation> annotationClass) {
		AnnotationRepeatType type = AnnotationRepeatType.getType(annotationClass);
		assertThat(type).isInstanceOf(AnnotationRepeatType.NonRepeatable.class);
	}

	@ParameterizedTest
	@ValueSource(classes = {ValueSources.class, Rs.class})
	void getTypeRepeatableContainer(Class<? extends Annotation> annotationClass) {
		AnnotationRepeatType type = AnnotationRepeatType.getType(annotationClass);
		assertThat(type).isInstanceOf(AnnotationRepeatType.RepeatableContainer.class);
	}

	@ParameterizedTest
	@ValueSource(classes = {ValueSource.class, R.class})
	void getTypeRepeatableValue(Class<? extends Annotation> annotationClass) {
		AnnotationRepeatType type = AnnotationRepeatType.getType(annotationClass);
		assertThat(type).isInstanceOf(AnnotationRepeatType.Repeatable.class);
	}

	@Repeatable(Rs.class)
	@Retention(RetentionPolicy.RUNTIME)
	@interface R { }

	@Retention(RetentionPolicy.RUNTIME)
	@interface Rs {
		R[] value();
	}
}
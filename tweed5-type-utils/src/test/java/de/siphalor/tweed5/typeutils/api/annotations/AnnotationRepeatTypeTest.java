package de.siphalor.tweed5.typeutils.api.annotations;

import org.junit.jupiter.api.Test;
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
		assertThat(type).isEqualTo(new AnnotationRepeatType.NonRepeatable(annotationClass));
	}

	@Test
	void getTypeRepeatableContainer() {
		AnnotationRepeatType type = AnnotationRepeatType.getType(Rs.class);
		assertThat(type).isEqualTo(new AnnotationRepeatType.RepeatableContainer(Rs.class, R.class));
	}

	@Test
	void getTypeRepeatableValue() {
		AnnotationRepeatType type = AnnotationRepeatType.getType(R.class);
		assertThat(type).isEqualTo(new AnnotationRepeatType.Repeatable(R.class, Rs.class));
	}

	@Repeatable(Rs.class)
	@Retention(RetentionPolicy.RUNTIME)
	@interface R { }

	@Retention(RetentionPolicy.RUNTIME)
	@interface Rs {
		R[] value();
	}
}

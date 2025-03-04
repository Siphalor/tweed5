package de.siphalor.tweed5.typeutils.api.type;

import de.siphalor.tweed5.typeutils.impl.type.AnnotationRepeatTypeResolver;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public interface AnnotationRepeatType {
	static AnnotationRepeatType getType(@NotNull Class<? extends Annotation> annotationClass) {
		return AnnotationRepeatTypeResolver.getType(annotationClass);
	}

	class NonRepeatable implements AnnotationRepeatType {
		private static final NonRepeatable INSTANCE = new NonRepeatable();

		public static NonRepeatable instance() {
			return INSTANCE;
		}

		private NonRepeatable() {}
	}

	@Value
	class Repeatable implements AnnotationRepeatType {
		Class<? extends Annotation> containerAnnotation;
	}

	@Value
	class RepeatableContainer implements AnnotationRepeatType {
		Class<? extends Annotation> componentAnnotation;
	}
}

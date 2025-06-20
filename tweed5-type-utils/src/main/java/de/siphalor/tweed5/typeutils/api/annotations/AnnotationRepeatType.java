package de.siphalor.tweed5.typeutils.api.annotations;

import de.siphalor.tweed5.typeutils.impl.annotations.AnnotationRepeatTypeResolver;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public abstract class AnnotationRepeatType {
	public static AnnotationRepeatType getType(Class<? extends Annotation> annotationClass) {
		return AnnotationRepeatTypeResolver.getType(annotationClass);
	}

	private final Class<? extends Annotation> annotationClass;

	public abstract @Nullable Class<? extends Annotation> alternativeAnnotationClass();

	public static class NonRepeatable extends AnnotationRepeatType {
		public NonRepeatable(Class<? extends Annotation> annotationClass) {
			super(annotationClass);
		}

		@Override
		public @Nullable Class<? extends Annotation> alternativeAnnotationClass() {
			return null;
		}
	}

	@Getter
	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class Repeatable extends AnnotationRepeatType {
		private final Class<? extends Annotation> containerAnnotationClass;

		public Repeatable(
				Class<? extends Annotation> annotationClass,
				Class<? extends Annotation> containerAnnotationClass
		) {
			super(annotationClass);
			this.containerAnnotationClass = containerAnnotationClass;
		}

		public AnnotationRepeatType.RepeatableContainer containerRepeatType() {
			return new RepeatableContainer(containerAnnotationClass, annotationClass());
		}

		@Override
		public @Nullable Class<? extends Annotation> alternativeAnnotationClass() {
			return containerAnnotationClass;
		}
	}

	@EqualsAndHashCode(callSuper = true)
	@ToString(callSuper = true)
	public static class RepeatableContainer extends AnnotationRepeatType {
		@Getter
		private final Class<? extends Annotation> elementAnnotationClass;
		@EqualsAndHashCode.Exclude
		@ToString.Exclude
		@Nullable
		private MethodHandle valueHandle;

		public RepeatableContainer(
				Class<? extends Annotation> annotationClass,
				Class<? extends Annotation> elementAnnotationClass
		) {
			super(annotationClass);
			this.elementAnnotationClass = elementAnnotationClass;
		}

		public AnnotationRepeatType.Repeatable elementRepeatType() {
			return new Repeatable(elementAnnotationClass, annotationClass());
		}

		@Override
		public @Nullable Class<? extends Annotation> alternativeAnnotationClass() {
			return elementAnnotationClass;
		}

		public <C extends Annotation, E extends Annotation> E[] elements(C containerAnnotation) {
			if (valueHandle == null) {
				try {
					valueHandle = MethodHandles.lookup().findVirtual(
							annotationClass(),
							"value",
							MethodType.methodType(elementAnnotationClass.arrayType())
					);
				} catch (Exception e) {
					throw new IllegalStateException(
							"Failed to resolve value method of container annotation: " + containerAnnotation
								+ " (" + containerAnnotation.getClass().getName() + ")",
							e
					);
				}
			}

			try {
				//noinspection unchecked
				return (E[]) valueHandle.invoke(containerAnnotation);
			} catch (Throwable e) {
				throw new RuntimeException(
						"Unexpected exception when calling value method of container annotation: "
								+ containerAnnotation + "( " + containerAnnotation.getClass().getName() + ")",
						e
				);
			}
		}
	}
}

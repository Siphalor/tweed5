package de.siphalor.tweed5.annotationinheritance.api;

import de.siphalor.tweed5.annotationinheritance.impl.AnnotationInheritanceResolver;
import de.siphalor.tweed5.typeutils.api.annotations.AnnotationRepeatType;
import de.siphalor.tweed5.utils.api.collection.ClassToInstanceMap;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

@RequiredArgsConstructor
public class AnnotationInheritanceAwareAnnotatedElement implements AnnotatedElement {
	private final AnnotatedElement inner;
	private @Nullable ClassToInstanceMap<Annotation> resolvedAnnotations;

	@Override
	public @Nullable <T extends Annotation> T getAnnotation(@NonNull Class<T> annotationClass) {
		if (resolvedAnnotations != null) {
			return resolvedAnnotations.get(annotationClass);
		}

		Annotation[] annotations = inner.getAnnotations();
		boolean metaEncountered = false;
		T foundAnnotation = null;

		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationClass)) {
				//noinspection unchecked
				foundAnnotation = (T) annotation;
			} else if (!metaEncountered) {
				metaEncountered = annotation.annotationType().isAnnotationPresent(AnnotationInheritance.class);
			}
		}

		if (foundAnnotation != null) {
			AnnotationRepeatType repeatType = AnnotationRepeatType.getType(annotationClass);
			if (repeatType instanceof AnnotationRepeatType.NonRepeatable) {
				return foundAnnotation;
			}
		}
		if (!metaEncountered) {
			return foundAnnotation;
		}

		return getOrResolveAnnotations().get(annotationClass);
	}

	@Override
	public @NotNull Annotation[] getAnnotations() {
		return getOrResolveAnnotations().values().toArray(new Annotation[0]);
	}

	@Override
	public @NotNull Annotation[] getDeclaredAnnotations() {
		return inner.getDeclaredAnnotations();
	}

	private ClassToInstanceMap<Annotation> getOrResolveAnnotations() {
		if (resolvedAnnotations == null) {
			resolvedAnnotations = new AnnotationInheritanceResolver(inner).resolve();
		}
		return resolvedAnnotations;
	}
}

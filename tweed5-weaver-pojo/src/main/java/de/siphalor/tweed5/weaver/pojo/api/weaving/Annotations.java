package de.siphalor.tweed5.weaver.pojo.api.weaving;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents multi-level annotations across multiple Java elements.
 * E.g. annotations on a field overriding annotations declared on the field type.
 */
public class Annotations {
	private static final List<ElementType> ELEMENT_TYPE_ORDER = Arrays.asList(
			ElementType.TYPE_USE,
			ElementType.FIELD,
			ElementType.CONSTRUCTOR,
			ElementType.METHOD,
			ElementType.LOCAL_VARIABLE,
			ElementType.TYPE_PARAMETER,
			ElementType.TYPE,
			ElementType.ANNOTATION_TYPE,
			ElementType.PACKAGE
	);
	private final Map<ElementType, AnnotatedElement> elements = new EnumMap<>(ElementType.class);

	public void addAnnotationsFrom(ElementType elementType, AnnotatedElement element) {
		elements.put(elementType, element);
	}

	@Nullable
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for (ElementType elementType : ELEMENT_TYPE_ORDER) {
			AnnotatedElement annotatedElement = elements.get(elementType);
			if (annotatedElement != null) {
				T annotation = annotatedElement.getAnnotation(annotationClass);
				if (annotation != null) {
					return annotation;
				}
			}
		}
		return null;
	}

	@Nullable
	public <T extends Annotation> T getAnnotation(ElementType elementType, Class<T> annotationType) {
		AnnotatedElement annotatedElement = elements.get(elementType);
		if (annotatedElement == null) {
			return null;
		}
		return annotatedElement.getAnnotation(annotationType);
	}

	@NotNull
	public <T extends Annotation> T[] getAnnotationHierarchy(Class<T> annotationClass) {
		List<T> hierarchy = new ArrayList<>(elements.size());
		for (ElementType elementType : ELEMENT_TYPE_ORDER) {
			AnnotatedElement element = elements.get(elementType);
			if (element != null) {
				T annotation = element.getAnnotation(annotationClass);
				if (annotation != null) {
					hierarchy.add(annotation);
				}
			}
		}
		//noinspection unchecked
		return hierarchy.toArray((T[]) Array.newInstance(annotationClass, hierarchy.size()));
	}

	@NotNull
	public <T extends Annotation> T[] getAnnotations(Class<T> annotationClass) {
		for (ElementType elementType : ELEMENT_TYPE_ORDER) {
			AnnotatedElement annotatedElement = elements.get(elementType);
			if (annotatedElement != null) {
				T[] annotations = annotatedElement.getAnnotationsByType(annotationClass);
				if (annotations.length != 0) {
					return annotations;
				}
			}
		}
		//noinspection unchecked
		return (T[]) Array.newInstance(annotationClass, 0);
	}

	@NotNull
	public <T extends Annotation> T[] getAnnotations(ElementType elementType, Class<T> annotationType) {
		AnnotatedElement annotatedElement = elements.get(elementType);
		if (annotatedElement == null) {
			//noinspection unchecked
			return (T[]) Array.newInstance(annotationType, 0);
		}
		return annotatedElement.getAnnotationsByType(annotationType);
	}

	@NotNull
	public <T extends Annotation> T[][] getAnnotationsHierachy(Class<T> annotationClass) {
		List<T[]> hierarchy = new ArrayList<>(ELEMENT_TYPE_ORDER.size());
		for (ElementType elementType : ELEMENT_TYPE_ORDER) {
			AnnotatedElement annotatedElement = elements.get(elementType);
			if (annotatedElement != null) {
				T[] annotations = annotatedElement.getAnnotationsByType(annotationClass);
				if (annotations.length != 0) {
					hierarchy.add(annotations);
				}
			}
		}
		//noinspection unchecked
		return hierarchy.toArray((T[][]) Array.newInstance(annotationClass, 0, 0));
	}

	@NotNull
	public Annotation[] getAllAnnotations() {
		Map<Class<? extends Annotation>, Annotation[]> annotations = new HashMap<>();
		for (ElementType elementType : ELEMENT_TYPE_ORDER) {
			AnnotatedElement annotatedElement = elements.get(elementType);
			if (annotatedElement != null) {
				for (Annotation annotation : annotatedElement.getAnnotations()) {
					annotations.putIfAbsent(annotation.annotationType(), new Annotation[]{annotation});

					Repeatable repeatable = annotation.annotationType().getAnnotation(Repeatable.class);
					if (repeatable != null) {
						annotations.put(repeatable.value(), annotatedElement.getAnnotationsByType(repeatable.value()));
					}
				}
			}
		}

		if (annotations.isEmpty()) {
			return new Annotation[0];
		} else if (annotations.size() == 1) {
			return annotations.values().iterator().next();
		} else {
			return annotations.values().stream().flatMap(Arrays::stream).toArray(Annotation[]::new);
		}
	}
}

package de.siphalor.tweed5.annotationinheritance.impl;

import de.siphalor.tweed5.annotationinheritance.api.AnnotationInheritance;
import de.siphalor.tweed5.typeutils.api.annotations.AnnotationRepeatType;
import de.siphalor.tweed5.utils.api.collection.ClassToInstanceMap;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public class AnnotationInheritanceResolver {
	private final AnnotatedElement main;
	private final Map<Class<? extends Annotation>, Aggregator> aggregators = new LinkedHashMap<>();
	private static final Set<Class<? extends Annotation>> IGNORED_META_ANNOTATIONS = new CopyOnWriteArraySet<>(Arrays.asList(
			Target.class,
			Retention.class,
			AnnotationInheritance.class
	));

	public ClassToInstanceMap<Annotation> resolve() {
		resolve(main, Collections.emptySet());

		ClassToInstanceMap<Annotation> resolvedAnnotations = ClassToInstanceMap.backedBy(new LinkedHashMap<>());

		List<Aggregator> aggregatorList = new ArrayList<>(aggregators.values());
		for (int i = aggregatorList.size() - 1; i >= 0; i--) {
			Aggregator aggregator = aggregatorList.get(i);
			if (aggregator.annotations.size() == 1) {
				//noinspection unchecked
				resolvedAnnotations.put(
						(Class<Annotation>) aggregator.repeatType.annotationClass(),
						aggregator.annotations.iterator().next()
				);
			} else if (!aggregator.annotations.isEmpty()) {
				Annotation[] annotations = (Annotation[]) Array.newInstance(
						aggregator.repeatType.annotationClass(),
						aggregator.annotations.size()
				);
				int j = aggregator.annotations.size() - 1;
				for (Annotation annotation : aggregator.annotations) {
					annotations[j--] = annotation;
				}

				Annotation containerAnnotation = RepeatableAnnotationContainerHelper.createContainer(annotations);
				//noinspection unchecked
				resolvedAnnotations.put((Class<Annotation>) containerAnnotation.annotationType(), containerAnnotation);
			}
		}

		return resolvedAnnotations;
	}

	private void resolve(AnnotatedElement annotatedElement, Set<Class<? extends Annotation>> overriden) {
		AnnotationInheritance inheritanceConfig = annotatedElement.getAnnotation(AnnotationInheritance.class);

		Set<Class<? extends Annotation>> passOnAnnotations = null;
		Set<Class<? extends Annotation>> overridenOnwards = new HashSet<>(overriden);
		if (annotatedElement != main) {
			if (inheritanceConfig == null || inheritanceConfig.passOn().length == 0) {
				return;
			}
			passOnAnnotations = new HashSet<>(inheritanceConfig.passOn().length + 5);
			for (Class<? extends Annotation> passOn : inheritanceConfig.passOn()) {
				passOnAnnotations.add(passOn);
				AnnotationRepeatType repeatType = AnnotationRepeatType.getType(passOn);
				if (repeatType instanceof AnnotationRepeatType.Repeatable) {
					passOnAnnotations.add(((AnnotationRepeatType.Repeatable) repeatType).containerAnnotationClass());
				}
			}
		}
		if (inheritanceConfig != null) {
			for (Class<? extends Annotation> override : inheritanceConfig.override()) {
				overridenOnwards.add(override);
				AnnotationRepeatType repeatType = AnnotationRepeatType.getType(override);
				if (repeatType instanceof AnnotationRepeatType.Repeatable) {
					overridenOnwards.add(((AnnotationRepeatType.Repeatable) repeatType).containerAnnotationClass());
				}
			}
		}

		Annotation[] annotations = annotatedElement.getAnnotations();
		for (int i = annotations.length - 1; i >= 0; i--) {
			Annotation annotation = annotations[i];
			if ((passOnAnnotations != null && !passOnAnnotations.contains(annotation.annotationType()))
					|| IGNORED_META_ANNOTATIONS.contains(annotation.annotationType())
					|| overriden.contains(annotation.annotationType())) {
				continue;
			}

			Aggregator aggregator = aggregators.get(annotation.annotationType());
			AnnotationRepeatType repeatType;
			if (aggregator != null) {
				repeatType = aggregator.repeatType;
				if (repeatType instanceof AnnotationRepeatType.Repeatable) {
					aggregator.annotations.add(annotation);
				}
			} else {
				repeatType = AnnotationRepeatType.getType(annotation.annotationType());
				if (repeatType instanceof AnnotationRepeatType.NonRepeatable) {
					aggregator = new Aggregator(repeatType, Collections.singleton(annotation));
					aggregators.put(annotation.annotationType(), aggregator);
					overridenOnwards.add(annotation.annotationType());
				} else if (repeatType instanceof AnnotationRepeatType.Repeatable) {
					ArrayList<Annotation> repeatableAnnotations = new ArrayList<>();
					repeatableAnnotations.add(annotation);
					aggregator = new Aggregator(repeatType, repeatableAnnotations);
					aggregators.put(annotation.annotationType(), aggregator);
				} else if (repeatType instanceof AnnotationRepeatType.RepeatableContainer) {
					AnnotationRepeatType.RepeatableContainer containerRepeatType
							= (AnnotationRepeatType.RepeatableContainer) repeatType;
					Class<? extends Annotation> elementAnnotationType = containerRepeatType.elementAnnotationClass();

					Annotation[] elements = containerRepeatType.elements(annotation);

					aggregator = aggregators.get(elementAnnotationType);
					if (aggregator != null) {
						for (int j = elements.length - 1; j >= 0; j--) {
							aggregator.annotations.add(elements[j]);
						}
					} else {
						List<Annotation> repeatedAnnotations = new ArrayList<>(elements.length);
						for (int e = elements.length - 1; e >= 0; e--) {
							repeatedAnnotations.add(elements[e]);
						}
						aggregators.put(
								containerRepeatType.elementAnnotationClass(),
								new Aggregator(containerRepeatType.elementRepeatType(), repeatedAnnotations)
						);
					}
				}
			}

			if (repeatType instanceof AnnotationRepeatType.NonRepeatable && annotation.annotationType()
					.isAnnotationPresent(AnnotationInheritance.class)) {
				resolve(annotation.annotationType(), overridenOnwards);
			}
		}
	}

	@Value
	private static class Aggregator {
		AnnotationRepeatType repeatType;
		Collection<Annotation> annotations;
	}
}

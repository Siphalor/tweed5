package de.siphalor.tweed5.typeutils.api.annotations;

import de.siphalor.tweed5.typeutils.api.type.TypeAnnotationLayer;
import lombok.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

public class LayeredTypeAnnotations implements AnnotatedElement {
	private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

	public static LayeredTypeAnnotations of(TypeAnnotationLayer layer, AnnotatedElement annotatedElement) {
		LayeredTypeAnnotations annotations = new LayeredTypeAnnotations();
		annotations.layers.add(new Layer(layer, annotatedElement));
		return annotations;
	}

	private final List<Layer> layers = new ArrayList<>();

	public void appendLayerFrom(TypeAnnotationLayer layer, AnnotatedElement annotatedElement) {
		int i;
		for (i = 0; i < layers.size(); i++) {
			if (layer.compareTo(layers.get(i).layer()) > 0) {
				break;
			}
		}
		layers.add(i, new Layer(layer, annotatedElement));
	}

	public void prependLayerFrom(TypeAnnotationLayer layer, AnnotatedElement annotatedElement) {
		int i;
		for (i = 0; i < layers.size(); i++) {
			if (layer.compareTo(layers.get(i).layer()) >= 0) {
				break;
			}
		}
		layers.add(i, new Layer(layer, annotatedElement));
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		if (layers.isEmpty()) {
			return null;
		} else if (layers.size() == 1) {
			return layers.get(0).annotatedElement.getAnnotation(annotationClass);
		}

		Class<? extends Annotation> altAnnotationClass = AnnotationRepeatType.getType(annotationClass)
				.alternativeAnnotationClass();

		for (Layer layer : layers) {
			T annotation = layer.annotatedElement.getAnnotation(annotationClass);
			if (annotation != null) {
				return annotation;
			}
			if (altAnnotationClass != null && layer.annotatedElement.isAnnotationPresent(altAnnotationClass)) {
				return null;
			}
		}
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		if (layers.isEmpty()) {
			return EMPTY_ANNOTATIONS;
		} else if (layers.size() == 1) {
			return layers.get(0).annotatedElement.getAnnotations();
		}

		Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();
		for (Layer layer : layers) {
			for (Annotation layerAnnotation : layer.annotatedElement.getAnnotations()) {
				Class<? extends Annotation> layerAnnotationClass = layerAnnotation.annotationType();
				if (annotations.containsKey(layerAnnotationClass)) {
					continue;
				}
				Class<? extends Annotation> layerAltClass = AnnotationRepeatType.getType(layerAnnotationClass)
						.alternativeAnnotationClass();
				if (annotations.containsKey(layerAltClass)) {
					continue;
				}
				annotations.put(layerAnnotationClass, layerAnnotation);
			}
		}
		return annotations.values().toArray(new Annotation[0]);
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		if (layers.isEmpty()) {
			return EMPTY_ANNOTATIONS;
		} else if (layers.size() == 1) {
			return layers.get(0).annotatedElement.getDeclaredAnnotations();
		}

		Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();
		for (Layer layer : layers) {
			for (Annotation layerAnnotation : layer.annotatedElement.getDeclaredAnnotations()) {
				Class<? extends Annotation> layerAnnotationClass = layerAnnotation.annotationType();
				if (annotations.containsKey(layerAnnotationClass)) {
					continue;
				}
				Class<? extends Annotation> layerAltClass = AnnotationRepeatType.getType(layerAnnotationClass)
						.alternativeAnnotationClass();
				if (annotations.containsKey(layerAltClass)) {
					continue;
				}
				annotations.put(layerAnnotationClass, layerAnnotation);
			}
		}
		return annotations.values().toArray(new Annotation[0]);
	}

	@Value
	private static class Layer {
		TypeAnnotationLayer layer;
		AnnotatedElement annotatedElement;
	}
}

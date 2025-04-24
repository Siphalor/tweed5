package de.siphalor.tweed5.typeutils.impl.type;

import de.siphalor.tweed5.typeutils.api.type.AnnotationRepeatType;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AnnotationRepeatTypeResolver {
	private static final Map<Class<? extends Annotation>, AnnotationRepeatType> CACHE = new HashMap<>();
	private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();

	public static AnnotationRepeatType getType(Class<? extends Annotation> annotationClass) {
		CACHE_LOCK.readLock().lock();
		try {
			AnnotationRepeatType cachedValue = CACHE.get(annotationClass);
			if (cachedValue != null) {
				return cachedValue;
			}
		} finally {
			CACHE_LOCK.readLock().unlock();
		}
		return determineType(annotationClass);
	}

	private static AnnotationRepeatType determineType(Class<? extends Annotation> annotationClass) {
		Class<? extends Annotation> container = getRepeatableContainerFromComponentAnnotation(annotationClass);
		if (container != null) {
			CACHE_LOCK.writeLock().lock();
			try {
				AnnotationRepeatType type = new AnnotationRepeatType.Repeatable(container);
				CACHE.put(annotationClass, type);
				CACHE.put(container, new AnnotationRepeatType.RepeatableContainer(annotationClass));
				return type;
			} finally {
				CACHE_LOCK.writeLock().unlock();
			}
		}
		Class<? extends Annotation> component = getRepeatableComponentFromContainerAnnotation(annotationClass);
		if (component != null) {
			CACHE_LOCK.writeLock().lock();
			try {
				AnnotationRepeatType type = new AnnotationRepeatType.RepeatableContainer(component);
				CACHE.put(annotationClass, type);
				CACHE.put(component, new AnnotationRepeatType.Repeatable(component));
				return type;
			} finally {
				CACHE_LOCK.writeLock().unlock();
			}
		}

		CACHE_LOCK.writeLock().lock();
		try {
			CACHE.put(annotationClass, AnnotationRepeatType.NonRepeatable.instance());
		} finally {
			CACHE_LOCK.writeLock().unlock();
		}
		return AnnotationRepeatType.NonRepeatable.instance();
	}

	private static @Nullable Class<? extends Annotation> getRepeatableContainerFromComponentAnnotation(
			Class<? extends Annotation> annotationClass
	) {
		Repeatable repeatableDeclaration = annotationClass.getAnnotation(Repeatable.class);
		if (repeatableDeclaration == null) {
			return null;
		}
		return repeatableDeclaration.value();
	}

	private static @Nullable Class<? extends Annotation> getRepeatableComponentFromContainerAnnotation(
			Class<? extends Annotation> annotationClass
	) {
		try {
			Method method = annotationClass.getMethod("value");
			Class<?> returnType = method.getReturnType();
			if (!returnType.isArray()) {
				return null;
			}
			Class<?> componentType = returnType.getComponentType();
			if (!componentType.isAnnotation()) {
				return null;
			}
			Repeatable repeatableDeclaration = componentType.getAnnotation(Repeatable.class);
			if (repeatableDeclaration == null) {
				return null;
			}
			if (repeatableDeclaration.value() != annotationClass) {
				return null;
			}

			//noinspection unchecked
			return (Class<? extends Annotation>) componentType;
		} catch (NoSuchMethodException ignored) {
			return null;
		}
	}
}

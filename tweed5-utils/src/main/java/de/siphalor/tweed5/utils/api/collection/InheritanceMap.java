package de.siphalor.tweed5.utils.api.collection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InheritanceMap<T extends @NonNull Object> {
	private static final InheritanceMap<Object> EMPTY = unmodifiable(new InheritanceMap<>(Object.class));

	private final Class<T> baseClass;
	private final Map<T, Collection<Class<? extends T>>> instanceToClasses;
	private final Map<Class<? extends T>, Collection<T>> classToInstances;

	public static <T extends @NonNull Object> InheritanceMap<T> empty() {
		return (InheritanceMap<T>) EMPTY;
	}
	public static <T extends @NonNull Object> InheritanceMap<T> unmodifiable(InheritanceMap<T> map) {
		return new Unmodifiable<>(map);
	}

	public InheritanceMap(Class<T> baseClass) {
		this(baseClass, new IdentityHashMap<>(), new HashMap<>());
	}

	public int size() {
		return instanceToClasses.size();
	}

	public boolean isEmpty() {
		return instanceToClasses.isEmpty();
	}

	public boolean containsAnyInstanceForClass(Class<? extends T> clazz) {
		return !classToInstances.getOrDefault(clazz, Collections.emptyList()).isEmpty();
	}

	public boolean containsSingleInstanceForClass(Class<? extends T> clazz) {
		return classToInstances.getOrDefault(clazz, Collections.emptyList()).size() == 1;
	}

	public boolean containsInstance(T instance) {
		return instanceToClasses.containsKey(instance);
	}

	public <V extends T> Collection<V> getAllInstances(Class<V> clazz) {
		return (Collection<V>) classToInstances.getOrDefault(clazz, Collections.emptyList());
	}

	public <V extends T> @Nullable V getSingleInstance(Class<V> clazz) throws NonUniqueResultException {
		Collection<T> instances = classToInstances.getOrDefault(clazz, Collections.emptyList());
		if (instances.isEmpty()) {
			return null;
		} else if (instances.size() == 1) {
			return (V) instances.iterator().next();
		} else {
			throw new NonUniqueResultException("Multiple instances for class " + clazz.getName() + " exist.");
		}
	}

	public boolean putAll(T... instances) {
		boolean changed = false;
		for (T instance : instances) {
			changed = put(instance) || changed;
		}
		return changed;
	}

	public boolean put(T instance) {
		if (instanceToClasses.containsKey(instance)) {
			return false;
		}

		putInternal(instance);
		return true;
	}
	
	public boolean putIfAbsent(T instance) {
		Collection<T> existingInstances = classToInstances.getOrDefault(instance.getClass(), Collections.emptyList());
		if (existingInstances.isEmpty()) {
			putInternal(instance);
			return true;
		} else {
			return false;
		}
	}
	
	public <V extends T> @Nullable V removeInstance(V instance) {
		if (!instanceToClasses.containsKey(instance)) {
			return null;
		}
		Collection<Class<? extends T>> classes = instanceToClasses.getOrDefault(instance, Collections.emptyList());
		for (Class<? extends T> implemented : classes) {
			classToInstances.getOrDefault(implemented, Collections.emptyList()).remove(instance);
		}
		instanceToClasses.remove(instance);
		return instance;
	}
	
	public void clear() {
		instanceToClasses.clear();
		classToInstances.clear();
	}
	
	public Set<T> values() {
		return instanceToClasses.keySet();
	}
	
	private void putInternal(T instance) {
		Collection<Class<? extends T>> classes = findClasses((Class<? extends T>) instance.getClass());

		instanceToClasses.put(instance, classes);
		for (Class<? extends T> implementedClass : classes) {
			classToInstances.computeIfAbsent(implementedClass, c -> new ArrayList<>()).add(instance);
		}
	}
	
	private Collection<Class<? extends T>> findClasses(Class<? extends T> clazz) {
		List<Class<? extends T>> classes = new ArrayList<>();
		
		Class<?> superClass = clazz;
		while (superClass != Object.class && superClass != baseClass && baseClass.isAssignableFrom(superClass)) {
			classes.add((Class<? extends T>) superClass);

			if (baseClass == Object.class || baseClass.isInterface()) {
				classes.addAll(findOnlyInterfaces((Class<? extends T>) superClass));
			}

			superClass = superClass.getSuperclass();
		}
		
		return classes;
	}
	
	private Collection<Class<? extends T>> findOnlyInterfaces(Class<? extends T> clazz) {
		List<Class<? extends T>> classes = new ArrayList<>();

		for (Class<?> implemented : clazz.getInterfaces()) {
			if (baseClass != implemented && baseClass.isAssignableFrom(implemented)) {
				classes.add((Class<? extends T>) implemented);
				classes.addAll(findOnlyInterfaces((Class<? extends T>) implemented));
			}
		}
		
		return classes;
	}

	public static class NonUniqueResultException extends Exception {
		public NonUniqueResultException(String message) {
			super(message);
		}
	}

	private static class Unmodifiable<T extends @NonNull Object> extends InheritanceMap<T> {
		public Unmodifiable(InheritanceMap<T> delegate) {
			super(delegate.baseClass, delegate.instanceToClasses, delegate.classToInstances);
		}

		@Override
		public boolean put(T instance) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean putIfAbsent(T instance) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <V extends T> V removeInstance(V instance) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}
	}
}

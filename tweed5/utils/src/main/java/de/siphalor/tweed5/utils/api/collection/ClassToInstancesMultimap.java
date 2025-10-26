package de.siphalor.tweed5.utils.api.collection;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class ClassToInstancesMultimap<T> implements Collection<T> {
	private static final ClassToInstancesMultimap<Object> EMPTY = unmodifiable(new ClassToInstancesMultimap<>(Collections.emptyMap(), ArrayList::new));

	protected final Map<Class<? extends T>, Collection<T>> delegate;
	protected final Supplier<Collection<T>> collectionSupplier;

	public static <T> ClassToInstancesMultimap<T> unmodifiable(ClassToInstancesMultimap<T> map) {
		return new Unmodifiable<>(map.delegate, map.collectionSupplier);
	}

	public static <T> ClassToInstancesMultimap<T> empty() {
		return (ClassToInstancesMultimap<T>) EMPTY;
	}

	public int size() {
		return (int) delegate.values().stream().mapToLong(Collection::size).sum();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.getOrDefault(o.getClass(), Collections.emptyList()).contains(o);
	}

	public Set<Class<? extends T>> classes() {
		return delegate.keySet();
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private final Iterator<Map.Entry<Class<? extends T>, Collection<T>>> classIterator = delegate.entrySet().iterator();
			private @Nullable Iterator<? extends T> listIterator;
			private boolean keptElement;
			private boolean keptAnyElementInList;

			@Override
			public boolean hasNext() {
				return classIterator.hasNext() || (listIterator != null && listIterator.hasNext());
			}

			@Override
			public T next() {
				if (keptElement) {
					keptAnyElementInList = true;
				}
				if (listIterator == null || !listIterator.hasNext()) {
					if (listIterator != null && !keptAnyElementInList) {
						classIterator.remove();
					}
					listIterator = classIterator.next().getValue().iterator();
					keptAnyElementInList = false;
				}
				keptElement = true;
				return listIterator.next();
			}

			@Override
			public void remove() {
				if (listIterator == null) {
					throw new IllegalStateException("Iterator has not been called");
				}
				keptElement = false;
				listIterator.remove();
			}
		};
	}

	@Override
	public Object[] toArray() {
		return delegate.values().stream().flatMap(Collection::stream).toArray();
	}

	@Override
	public <S> S[] toArray(S[] array) {
		Class<?> clazz = array.getClass().getComponentType();
		return delegate.values().stream()
				.flatMap(Collection::stream)
				.toArray(size -> (S[]) Array.newInstance(clazz, size));
	}

	@Override
	public boolean add(T value) {
		return delegate.computeIfAbsent(((Class<T>) value.getClass()), clazz -> collectionSupplier.get()).add(value);
	}

	@Override
	public boolean remove(Object value) {
		Collection<T> values = delegate.get(value.getClass());
		if (values == null) {
			return false;
		}
		if (values.remove(value)) {
			if (values.isEmpty()) {
				delegate.remove(value.getClass());
			}
			return true;
		}
		return false;
	}

	public <U extends T> Collection<U> getAll(Class<U> clazz) {
		return (Collection<U>) Collections.unmodifiableCollection(delegate.getOrDefault(clazz, Collections.emptyList()));
	}

	public Collection<T> removeAll(Class<? extends T> clazz) {
		Collection<T> removed = delegate.remove(clazz);
		return removed == null ? Collections.emptyList() : Collections.unmodifiableCollection(removed);
	}

	@Override
	public boolean containsAll(Collection<?> values) {
		for (Object value : values) {
			if (!contains(value)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> values) {
		boolean changed = false;
		for (T value : values) {
			changed = add(value) || changed;
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> values) {
		boolean changed = false;
		for (Object value : values) {
			changed = remove(value) || changed;
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> values) {
		Map<Class<?>, ? extends List<?>> valuesByClass = values.stream()
				.collect(Collectors.groupingBy(Object::getClass));
		delegate.putAll((Map<Class<? extends T>, List<T>>)(Object) valuesByClass);
		delegate.keySet().removeIf(key -> !valuesByClass.containsKey(key));
		return true;
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	protected static class Unmodifiable<T> extends ClassToInstancesMultimap<T> {
		public Unmodifiable(
				Map<Class<? extends T>, Collection<T>> delegate,
				Supplier<Collection<T>> collectionSupplier
		) {
			super(delegate, collectionSupplier);
		}

		@Override
		public Iterator<T> iterator() {
			return delegate.values().stream().flatMap(Collection::stream).iterator();
		}

		@Override
		public boolean add(T value) {
			throw createUnsupportedOperationException();
		}

		@Override
		public boolean remove(Object value) {
			throw createUnsupportedOperationException();
		}

		@Override
		public Collection<T> removeAll(Class<? extends T> clazz) {
			throw createUnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends T> values) {
			throw createUnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> values) {
			throw createUnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> values) {
			throw createUnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw createUnsupportedOperationException();
		}

		protected UnsupportedOperationException createUnsupportedOperationException() {
			return new UnsupportedOperationException("Map is unmodifiable");
		}
	}
}

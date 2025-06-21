package de.siphalor.tweed5.utils.api.collection;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

@SuppressWarnings("unchecked")
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassToInstanceMap<T extends @NonNull Object> implements Iterable<T> {
	private final Map<Class<? extends T>, T> delegate;

	public static <T extends @NonNull Object> ClassToInstanceMap<T> backedBy(Map<Class<? extends T>, T> delegate) {
		return new ClassToInstanceMap<>(delegate);
	}

	public ClassToInstanceMap() {
		this(new HashMap<>());
	}

	public int size() {
		return delegate.size();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public boolean containsClass(Class<? extends T> key) {
		return delegate.containsKey(key);
	}

	public boolean containsValue(T value) {
		return delegate.containsValue(value);
	}

	public <V extends T> @Nullable V get(Class<V> key) {
		return (V) delegate.get(key);
	}

	public <V extends T> @Nullable V put(V value) {
		return put((Class<V>) value.getClass(), value);
	}

	public <V extends T, U extends V> @Nullable V put(Class<V> key, U value) {
		return (V) delegate.put(key, value);
	}

	public <V extends T> @Nullable V remove(Class<V> key) {
		return (V) delegate.remove(key);
	}

	public void clear() {
		delegate.clear();
	}

	public Set<Class<? extends T>> classes() {
		return delegate.keySet();
	}

	public Set<T> values() {
		return new AbstractSet<T>() {
			@Override
			public Iterator<T> iterator() {
				Iterator<Map.Entry<Class<? extends T>, T>> entryIterator = delegate.entrySet().iterator();
				return new Iterator<T>() {
					@Override
					public boolean hasNext() {
						return entryIterator.hasNext();
					}

					@Override
					public T next() {
						return entryIterator.next().getValue();
					}

					@Override
					public void remove() {
						entryIterator.remove();
					}
				};
			}

			@Override
			public int size() {
				return delegate.size();
			}
		};
	}

	@Override
	public Iterator<T> iterator() {
		return values().iterator();
	}
}

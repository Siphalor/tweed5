package de.siphalor.tweed5.utils.api.collection;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassToInstanceMap<T> implements Iterable<T> {
	private final Map<Class<? extends T>, T> delegate;

	public static <T> ClassToInstanceMap<T> backedBy(Map<Class<? extends T>, T> delegate) {
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

	public <V extends T> V get(Class<V> key) {
		return (V) delegate.get(key);
	}

	public <V extends T> V put(@NotNull V value) {
		return (V) delegate.put((Class<? extends T>) value.getClass(), value);
	}

	public <V extends T> V remove(Class<V> key) {
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
			public @NotNull Iterator<T> iterator() {
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
	public @NotNull Iterator<T> iterator() {
		return values().iterator();
	}
}

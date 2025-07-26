package de.siphalor.tweed5.utils.api.collection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Array;
import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ImmutableArrayBackedSet<T> implements SortedSet<T> {
	private final T[] values;

	public static <T extends Comparable<T>> SortedSet<T> of(Collection<T> collection) {
		if (collection.isEmpty()) {
			return Collections.emptySortedSet();
		}
		T first = collection.iterator().next();

		//noinspection unchecked
		return new ImmutableArrayBackedSet<>(
				collection.stream().sorted().toArray(length -> (T[]) Array.newInstance(first.getClass(), length))
		);
	}

	@SafeVarargs
	public static <T extends Comparable<T>> SortedSet<T> of(T... values) {
		if (values.length == 0) {
			return Collections.emptySortedSet();
		}
		Arrays.sort(values);
		return new ImmutableArrayBackedSet<>(values);
	}

	@Override
	public @Nullable Comparator<? super T> comparator() {
		return null;
	}

	@Override
	public @NonNull SortedSet<T> subSet(T fromElement, T toElement) {
		int from = Arrays.binarySearch(values, fromElement);
		if (from < 0) {
			from = -from - 1;
		}
		if (from == 0) {
			return headSet(toElement);
		}
		int to = Arrays.binarySearch(values, toElement);
		if (to < 0) {
			to = -to - 1;
		}
		if (to == values.length) {
			return this;
		}
		return new  ImmutableArrayBackedSet<>(Arrays.copyOfRange(values, from, to));
	}

	@Override
	public @NonNull SortedSet<T> headSet(T toElement) {
		int to = Arrays.binarySearch(values, toElement);
		if (to < 0) {
			to = -to - 1;
		}
		if (to == values.length) {
			return this;
		}
		return new ImmutableArrayBackedSet<>(Arrays.copyOfRange(values, 0, to));
	}

	@Override
	public @NonNull SortedSet<T> tailSet(T fromElement) {
		int from = Arrays.binarySearch(values, fromElement);
		if (from < 0) {
			from = -from - 1;
		}
		if (from == 0) {
			return this;
		}
		return new ImmutableArrayBackedSet<>(Arrays.copyOfRange(values, from, values.length));
	}

	@Override
	public T first() {
		return values[0];
	}

	@Override
	public T last() {
		return values[values.length - 1];
	}

	@Override
	public int size() {
		return values.length;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean contains(Object o) {
		return Arrays.binarySearch(values, o) >= 0;
	}

	@Override
	public @NonNull Iterator<T> iterator() {
		return Arrays.stream(values).iterator();
	}

	@Override
	public @NonNull Object[] toArray() {
		return Arrays.copyOf(values, values.length);
	}

	@Override
	public @NonNull <T1> T1[] toArray(@NonNull T1[] a) {
		// basically copied from ArrayList#toArray(Object[])
		if (a.length < values.length) {
			//noinspection unchecked
			return (T1[]) toArray();
		}
		//noinspection SuspiciousSystemArraycopy
		System.arraycopy(values, 0, a, 0, values.length);
		if (a.length > values.length) {
			//noinspection DataFlowIssue
			a[values.length] = null;
		}
		return a;
	}

	@Override
	public boolean add(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(@NonNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(@NonNull Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(@NonNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(@NonNull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}

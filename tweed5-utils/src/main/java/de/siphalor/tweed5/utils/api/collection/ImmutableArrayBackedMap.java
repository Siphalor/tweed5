package de.siphalor.tweed5.utils.api.collection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ImmutableArrayBackedMap<K, V> implements SortedMap<K, V> {
	private final K[] keys;
	private final V[] values;

	@SuppressWarnings("unchecked")
	public static <K extends Comparable<K>, V> SortedMap<K, V> ofEntries(Collection<Map.Entry<K, V>> entries) {
		if (entries.isEmpty()) {
			return Collections.emptySortedMap();
		}

		Entry<K, V> any = entries.iterator().next();
		int size = entries.size();
		K[] keys = (K[]) Array.newInstance(any.getKey().getClass(), size);
		V[] values = (V[]) Array.newInstance(any.getValue().getClass(), size);

		int i = 0;
		Iterator<Entry<K, V>> iterator = entries.stream().sorted(Entry.comparingByKey()).iterator();
		while (iterator.hasNext()) {
			Entry<K, V> entry = iterator.next();
			keys[i] = entry.getKey();
			values[i] = entry.getValue();
			i++;
		}

		return new ImmutableArrayBackedMap<>(keys, values);
	}

	@Override
	public @Nullable Comparator<? super K> comparator() {
		return null;
	}

	@Override
	public @NonNull SortedMap<K, V> subMap(K fromKey, K toKey) {
		int from = findKey(fromKey);
		if (from < 0) {
			from = -from - 1;
		}
		if (from == 0) {
			return headMap(toKey);
		} else if (from >= keys.length) {
			return Collections.emptySortedMap();
		}
		int to = findKey(toKey, from + 1);
		if (to < 0) {
			to = -to - 1;
		}
		if (to == keys.length) {
			return this;
		} else if (to == from) {
			return Collections.emptySortedMap();
		}
		return new ImmutableArrayBackedMap<>(Arrays.copyOfRange(keys, from, to), Arrays.copyOfRange(values, from, to));
	}

	@Override
	public @NonNull SortedMap<K, V> headMap(K toKey) {
		int to = findKey(toKey);
		if (to < 0) {
			to = -to - 1;
		}
		if (to == keys.length) {
			return this;
		} else if (to == 0) {
			return Collections.emptySortedMap();
		}
		return new ImmutableArrayBackedMap<>(Arrays.copyOf(keys, to), Arrays.copyOf(values, to));
	}

	@Override
	public @NonNull SortedMap<K, V> tailMap(K fromKey) {
		int from = findKey(fromKey);
		if (from < 0) {
			from = -from - 1;
		}
		if (from == 0) {
			return this;
		} else if (from >= keys.length) {
			return Collections.emptySortedMap();
		}
		return new ImmutableArrayBackedMap<>(
				Arrays.copyOfRange(keys, from, keys.length),
				Arrays.copyOfRange(values, from, keys.length)
		);
	}

	@Override
	public K firstKey() {
		return keys[0];
	}

	@Override
	public K lastKey() {
		return keys[keys.length - 1];
	}

	@Override
	public int size() {
		return keys.length;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		return findKey(key) >= 0;
	}

	@Override
	public boolean containsValue(Object value) {
		return Arrays.binarySearch(values, value) >= 0;
	}

	@Override
	public @Nullable V get(Object key) {
		int index = findKey(key);
		if (index < 0) {
			return null;
		}
		return values[index];
	}

	@Override
	public @Nullable V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(@NonNull Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet() {
		return new ImmutableArrayBackedSet<>(keys);
	}

	@Override
	public Collection<V> values() {
		return new AbstractList<V>() {
			@Override
			public V get(int index) {
				return values[index];
			}

			@Override
			public int size() {
				return values.length;
			}
		};
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		//noinspection unchecked
		return new ImmutableArrayBackedSet<Entry<K ,V>>(
				IntStream.range(0, keys.length)
					.mapToObj(index -> new AbstractMap.SimpleEntry<>(keys[index], values[index]))
					.toArray(Entry[]::new)
		);
	}

	private int findKey(Object key) {
		return Arrays.binarySearch(keys, key);
	}

	private int findKey(Object key, int from) {
		return Arrays.binarySearch(keys, from, keys.length, key);
	}
}

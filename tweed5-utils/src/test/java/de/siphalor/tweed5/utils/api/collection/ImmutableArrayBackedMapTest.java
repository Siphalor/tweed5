package de.siphalor.tweed5.utils.api.collection;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableArrayBackedMapTest {

	@Test
	void ofEntriesEmpty() {
		assertThat(ImmutableArrayBackedMap.ofEntries(Collections.emptyList())).isSameAs(Collections.emptySortedMap());
	}

	@Test
	void ofEntries() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10),
				Map.entry(3, 30)
		));
		assertThat(map).containsExactly(Map.entry(1, 10), Map.entry(2, 20), Map.entry(3, 30));
	}

	@Test
	void comparator() {
		assertThat(ImmutableArrayBackedMap.ofEntries(List.of(Map.entry(1, 10))).comparator()).isNull();
	}

	@Test
	void subMap() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(40, 400),
				Map.entry(20, 200),
				Map.entry(10, 100),
				Map.entry(30, 300)
		));

		assertThat(map.subMap(1, 100)).isSameAs(map);
		assertThat(map.subMap(10, 40)).containsExactly(Map.entry(10, 100), Map.entry(20, 200), Map.entry(30, 300));
		assertThat(map.subMap(0, 20)).containsExactly(Map.entry(10, 100));
		assertThat(map.subMap(0, 1)).isEmpty();
		assertThat(map.subMap(41, 100)).isEmpty();
	}

	@Test
	void headMap() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(40, 400),
				Map.entry(20, 200),
				Map.entry(10, 100),
				Map.entry(30, 300)
		));

		assertThat(map.headMap(41)).isSameAs(map);
		assertThat(map.headMap(40)).containsExactly(Map.entry(10, 100), Map.entry(20, 200), Map.entry(30, 300));
		assertThat(map.headMap(10)).isEmpty();
		assertThat(map.headMap(0)).isEmpty();
	}

	@Test
	void tailMap() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(40, 400),
				Map.entry(20, 200),
				Map.entry(10, 100),
				Map.entry(30, 300)
		));

		assertThat(map.tailMap(0)).isSameAs(map);
		assertThat(map.tailMap(10)).isSameAs(map);
		assertThat(map.tailMap(30)).containsExactly(Map.entry(30, 300), Map.entry(40, 400));
		assertThat(map.tailMap(41)).isEmpty();
	}

	@Test
	void firstKey() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10)
		));
		assertThat(map.firstKey()).isEqualTo(1);
	}

	@Test
	void lastKey() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10)
		));
		assertThat(map.lastKey()).isEqualTo(2);
	}

	@Test
	void size() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10)
		));
		assertThat(map).hasSize(2);
		assertThat(map).isNotEmpty();
	}

	@Test
	void containsKey() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10)
		));
		assertThat(map.containsKey(0)).isFalse();
		assertThat(map.containsKey(1)).isTrue();
		assertThat(map.containsKey(2)).isTrue();
		assertThat(map.containsKey(3)).isFalse();
	}

	@Test
	void containsValue() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10)
		));
		assertThat(map.containsValue(0)).isFalse();
		assertThat(map.containsValue(10)).isTrue();
		assertThat(map.containsValue(20)).isTrue();
		assertThat(map.containsValue(30)).isFalse();
	}

	@Test
	void get() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10)
		));
		assertThat(map.get(0)).isNull();
		assertThat(map.get(1)).isEqualTo(10);
		assertThat(map.get(2)).isEqualTo(20);
		assertThat(map.get(3)).isNull();
	}

	@Test
	void unsupported() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10)
		));
		assertThatThrownBy(() -> map.put(5, 50)).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> map.putAll(Map.of(3, 30, 5, 50))).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> map.remove(2)).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(map::clear).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void keySet() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 20),
				Map.entry(1, 10),
				Map.entry(3, 30)
		));
		Set<Integer> keySet = map.keySet();
		assertThat(keySet).containsExactly(1, 2, 3).hasSize(3);
		assertThatThrownBy(() -> keySet.add(4)).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> keySet.remove(2)).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(keySet::clear).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void values() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 40),
				Map.entry(1, 90),
				Map.entry(3, 10)
		));
		Collection<Integer> values = map.values();
		assertThat(values).containsExactly(90, 40, 10).hasSize(3);
		assertThatThrownBy(() -> values.add(50)).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> values.remove(10)).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(values::clear).isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void entrySet() {
		SortedMap<Integer, Integer> map = ImmutableArrayBackedMap.ofEntries(List.of(
				Map.entry(2, 40),
				Map.entry(1, 90),
				Map.entry(3, 10)
		));
		Set<Map.Entry<Integer, Integer>> entrySet = map.entrySet();
		assertThat(entrySet).containsExactly(Map.entry(1, 90), Map.entry(2, 40), Map.entry(3, 10)).hasSize(3);
		assertThatThrownBy(() -> entrySet.add(Map.entry(4, 50))).isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> entrySet.remove(Map.entry(1, 90))).isInstanceOf(UnsupportedOperationException.class);
	}
}

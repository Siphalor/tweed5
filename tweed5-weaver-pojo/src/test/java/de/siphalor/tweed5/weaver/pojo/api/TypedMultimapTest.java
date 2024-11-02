package de.siphalor.tweed5.weaver.pojo.api;

import de.siphalor.tweed5.core.api.collection.TypedMultimap;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("java:S5838") // Since we're testing collections methods here, AssertJ's shorthands are not applicable
class TypedMultimapTest {

	@Test
	void size() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		assertThat(map).isEmpty();
		map.add("abc");
		assertThat(map.size()).isEqualTo(1);
		map.add(456);
		assertThat(map.size()).isEqualTo(2);
		map.add("def");
		assertThat(map.size()).isEqualTo(3);
		map.remove(456);
		assertThat(map.size()).isEqualTo(2);
	}

	@Test
	void isEmpty() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		assertThat(map.isEmpty()).isTrue();
		map.add("def");
		assertThat(map.isEmpty()).isFalse();
		map.remove("def");
		assertThat(map.isEmpty()).isTrue();
	}

	@Test
	void contains() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		assertThat(map.contains(123)).isFalse();
		map.add(456);
		assertThat(map.contains(123)).isFalse();
		map.add(123);
		assertThat(map.contains(123)).isTrue();
	}

	@Test
	void classes() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, "abc", "def", "ghi", 789L));
		assertThat(map.classes()).containsExactlyInAnyOrder(Integer.class, String.class, Long.class);
	}

	@Test
	void iterator() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.add("abc");
		map.add(123);
		map.add("def");
		map.add(456);

		Iterator<Object> iterator = map.iterator();
		assertThatThrownBy(iterator::remove).isInstanceOf(IllegalStateException.class);
		assertThat(iterator).hasNext();
		assertThat(iterator.next()).isEqualTo("abc");
		iterator.remove();
		assertThat(iterator).hasNext();
		assertThat(iterator.next()).isEqualTo("def");
		iterator.remove();
		assertThat(iterator).hasNext();
		assertThat(iterator.next()).isEqualTo(123);
		assertThat(iterator).hasNext();
		assertThat(iterator.next()).isEqualTo(456);
		assertThat(iterator).isExhausted();
		assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void toArray() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.add("abc");
		map.add(123);
		map.add("def");
		map.add(456);

		assertThat(map.toArray()).isEqualTo(new Object[] { "abc", "def", 123, 456 });
	}

	@Test
	void toArrayProvided() {
		TypedMultimap<Number> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.add(12);
		map.add(34L);
		map.add(56);
		map.add(78L);

		assertThat(map.toArray(new Number[0])).isEqualTo(new Object[] { 12, 56, 34L, 78L });
	}

	@Test
	void add() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), HashSet::new);
		assertThat(map).isEmpty();
		map.add(123);
		assertThat(map).hasSize(1);
		map.add("abc");
		assertThat(map).hasSize(2);
		map.add(123);
		assertThat(map).hasSize(2);
		map.add("abc");
		assertThat(map).hasSize(2);
	}

	@Test
	void remove() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, "abc", "def"));
		assertThat(map).hasSize(4);
		map.remove("def");
		assertThat(map).hasSize(3);
		map.remove("abc");
		assertThat(map).hasSize(2);
	}

	@Test
	void getAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, "abc", "def"));
		assertThat(map.getAll(Integer.class)).containsExactly(123, 456);
		assertThat(map.getAll(String.class)).containsExactly("abc", "def");
		assertThat(map.getAll(Long.class)).isEmpty();
	}

	@Test
	void removeAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def"));
		map.removeAll(Arrays.asList(456, "def"));
		assertThat(map).hasSize(3);
		assertThat(map.getAll(Integer.class)).containsExactly(123, 789);
		assertThat(map.getAll(String.class)).containsExactly("abc");
		map.removeAll(Arrays.asList(123, 789));
		assertThat(map.toArray()).containsExactly("abc");
	}

	@Test
	void containsAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def"));
		assertThat(map.containsAll(Arrays.asList(456, "def"))).isTrue();
		assertThat(map.containsAll(Arrays.asList(123, 789))).isTrue();
		assertThat(map.containsAll(Arrays.asList(404, 789))).isFalse();
	}

	@Test
	void addAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def"));
		assertThat(map).hasSize(5);
		assertThat(map.getAll(Integer.class)).containsExactlyElementsOf(Arrays.asList(123, 456, 789));
		map.addAll(Arrays.asList(123L, 456L));
		assertThat(map.getAll(Long.class)).containsExactlyElementsOf(Arrays.asList(123L, 456L));
	}

	@Test
	void removeAllByClass() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def", 123L));
		map.removeAll(Integer.class);
		assertThat(map).hasSize(3);
		map.removeAll(Long.class);
		assertThat(map).hasSize(2);
		map.removeAll(String.class);
		assertThat(map.isEmpty()).isTrue();
	}

	@Test
	void retainAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def", 123L));
		map.retainAll(Arrays.asList("abc", 456));
		assertThat(map).hasSize(2);
		assertThat(map.toArray()).containsExactly(456, "abc");
		map.retainAll(Collections.emptyList());
		assertThat(map.isEmpty()).isTrue();
	}

	@Test
	void clear() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def", 123L));
		map.clear();
		assertThat(map.isEmpty()).isTrue();
	}
}
package de.siphalor.tweed5.weaver.pojo.api;

import de.siphalor.tweed5.core.api.collection.TypedMultimap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TypedMultimapTest {

	@Test
	void size() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		assertEquals(0, map.size());
		map.add("abc");
		assertEquals(1, map.size());
		map.add(456);
		assertEquals(2, map.size());
		map.add("def");
		assertEquals(3, map.size());
		map.remove(456);
		assertEquals(2, map.size());
	}

	@Test
	void isEmpty() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		assertTrue(map.isEmpty());
		map.add("def");
		assertFalse(map.isEmpty());
		map.remove("def");
		assertTrue(map.isEmpty());
	}

	@Test
	void contains() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		assertFalse(map.contains(123));
		map.add(456);
		assertFalse(map.contains(123));
		map.add(123);
		assertTrue(map.contains(123));
	}

	@Test
	void classes() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, "abc", "def", "ghi", 789L));
		assertEquals(new HashSet<>(Arrays.asList(Integer.class, String.class, Long.class)), map.classes());
	}

	@Test
	void iterator() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.add("abc");
		map.add(123);
		map.add("def");
		map.add(456);

		Iterator<Object> iterator = map.iterator();
		assertThrows(IllegalStateException.class, iterator::remove);
		assertTrue(iterator.hasNext());
		assertEquals("abc", iterator.next());
		iterator.remove();
		assertTrue(iterator.hasNext());
		assertEquals("def", iterator.next());
		iterator.remove();
		assertTrue(iterator.hasNext());
		assertEquals(123, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(456, iterator.next());
		assertFalse(iterator.hasNext());
		assertThrows(NoSuchElementException.class, iterator::next);
	}

	@Test
	void toArray() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.add("abc");
		map.add(123);
		map.add("def");
		map.add(456);

		Object[] array = map.toArray();
		assertArrayEquals(new Object[] { "abc", "def", 123, 456 }, array);
	}

	@Test
	void toArrayProvided() {
		TypedMultimap<Number> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.add(12);
		map.add(34L);
		map.add(56);
		map.add(78L);

		@NotNull Number[] array = map.toArray(new Number[0]);
		assertArrayEquals(new Object[] { 12, 56, 34L, 78L }, array);
	}

	@Test
	void add() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), HashSet::new);
		assertTrue(map.isEmpty());
		map.add(123);
		assertEquals(1, map.size());
		map.add("abc");
		assertEquals(2, map.size());
		map.add(123);
		assertEquals(2, map.size());
		map.add("abc");
		assertEquals(2, map.size());
	}

	@Test
	void remove() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, "abc", "def"));
		assertEquals(4, map.size());
		map.remove("def");
		assertEquals(3, map.size());
		map.remove("abc");
		assertEquals(2, map.size());
	}

	@Test
	void getAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, "abc", "def"));
		assertEquals(Arrays.asList(123, 456), map.getAll(Integer.class));
		assertEquals(Arrays.asList("abc", "def"), map.getAll(String.class));
		assertEquals(Collections.emptyList(), map.getAll(Long.class));
	}

	@Test
	void removeAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def"));
		map.removeAll(Arrays.asList(456, "def"));
		assertEquals(3, map.size());
		assertEquals(Arrays.asList(123, 789), map.getAll(Integer.class));
		assertEquals(Collections.singletonList("abc"), map.getAll(String.class));
		map.removeAll(Arrays.asList(123, 789));
		assertArrayEquals(new Object[] { "abc" }, map.toArray());
	}

	@Test
	void containsAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def"));
		assertTrue(map.containsAll(Arrays.asList(456, "def")));
		assertTrue(map.containsAll(Arrays.asList(123, 789)));
		assertFalse(map.containsAll(Arrays.asList(404, 789)));
	}

	@Test
	void addAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def"));
		assertEquals(5, map.size());
		assertEquals(Arrays.asList(123, 456, 789), map.getAll(Integer.class));
		map.addAll(Arrays.asList(123L, 456L));
		assertEquals(Arrays.asList(123L, 456L), map.getAll(Long.class));
	}

	@Test
	void removeAllByClass() {
		TypedMultimap<Object> map = new TypedMultimap<>(new HashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def", 123L));
		map.removeAll(Integer.class);
		assertEquals(3, map.size());
		map.removeAll(Long.class);
		assertEquals(2, map.size());
		map.removeAll(String.class);
		assertTrue(map.isEmpty());
	}

	@Test
	void retainAll() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def", 123L));
		map.retainAll(Arrays.asList("abc", 456));
		assertEquals(2, map.size());
		assertArrayEquals(new Object[] { 456, "abc" }, map.toArray());
		map.retainAll(Collections.emptyList());
		assertTrue(map.isEmpty());
	}

	@Test
	void clear() {
		TypedMultimap<Object> map = new TypedMultimap<>(new LinkedHashMap<>(), ArrayList::new);
		map.addAll(Arrays.asList(123, 456, 789, "abc", "def", 123L));
		map.clear();
		assertTrue(map.isEmpty());
	}
}
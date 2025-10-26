package de.siphalor.tweed5.utils.api.collection;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

class ClassToInstanceMapTest {

	@Test
	void size() {
		ClassToInstanceMap<Number> map = new ClassToInstanceMap<>();
		assertThat(map.size()).isZero();
		map.put(1234);
		assertThat(map.size()).isEqualTo(1);
		map.put(456);
		assertThat(map.size()).isEqualTo(1);
		map.put(789L);
		assertThat(map.size()).isEqualTo(2);
	}

	@Test
	void isEmpty() {
		ClassToInstanceMap<Number> map = new ClassToInstanceMap<>();
		assertThat(map.isEmpty()).isTrue();
		map.put(123L);
		assertThat(map.isEmpty()).isFalse();
		map.remove(Long.class);
		assertThat(map.isEmpty()).isTrue();
	}

	@Test
	void containsClass() {
		ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();
		map.put(123L);
		map.put("abc");
		assertThat(map.containsClass(Long.class)).isTrue();
		assertThat(map.containsClass(String.class)).isTrue();
		assertThat(map.containsClass(Integer.class)).isFalse();
	}

	@Test
	void containsValue() {
		ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();
		map.put(123.45D);
		map.put("test");
		assertThat(map.containsValue(123)).isFalse();
		assertThat(map.containsValue(123.4D)).isFalse();
		assertThat(map.containsValue(123.45D)).isTrue();
		assertThat(map.containsValue("TEST")).isFalse();
		assertThat(map.containsValue("test")).isTrue();
	}

	@Test
	void get() {
		ClassToInstanceMap<Number> map = new ClassToInstanceMap<>();
		assertThat(map.get(Integer.class)).isNull();
		map.put(123);
		map.put(456L);
		assertThat(map.get(Float.class)).isNull();
		assertThat(map.get(Integer.class)).isEqualTo(123);
		assertThat(map.get(Long.class)).isEqualTo(456L);
	}

	@Test
	void put() {
		ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();
		map.put(123);
		assertThat(map.size()).isEqualTo(1);
		map.put(456);
		assertThat(map.size()).isEqualTo(1);
		map.put(123L);
		assertThat(map.size()).isEqualTo(2);
		map.put(456);
		assertThat(map.size()).isEqualTo(2);
	}

	@Test
	void remove() {
		ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();
		map.put(123);
		map.put("abcdefg");
		assertThat(map.size()).isEqualTo(2);
		map.remove(Long.class);
		assertThat(map.size()).isEqualTo(2);
		map.remove(String.class);
		assertThat(map.size()).isEqualTo(1);
		map.remove(Integer.class);
		assertThat(map.size()).isZero();
	}

	@Test
	void clear() {
		ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();
		map.put(123);
		map.put("abcdefg");
		assertThat(map.size()).isEqualTo(2);
		map.clear();
		assertThat(map.isEmpty()).isTrue();
	}

	@Test
	void classes() {
		ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();
		assertThat(map.classes()).isEmpty();
		map.put(123);
		assertThat(map.classes()).containsExactlyInAnyOrder(Integer.class);
		map.put(456);
		assertThat(map.classes()).containsExactlyInAnyOrder(Integer.class);
		map.put("heyho");
		assertThat(map.classes()).containsExactlyInAnyOrder(Integer.class, String.class);
	}

	@Test
	void values() {
		ClassToInstanceMap<Number> map = new ClassToInstanceMap<>();
		assertThat(map.values()).isEmpty();
		map.put(123);
		assertThat(map.values()).containsExactlyInAnyOrder(123);
		map.put(123L);
		assertThat(map.values()).containsExactlyInAnyOrder(123, 123L);
		map.put(456);
		assertThat(map.values()).containsExactlyInAnyOrder(456, 123L);
	}

	@Test
	void iterator() {
		ClassToInstanceMap<Object> map = new ClassToInstanceMap<>();
		assertThat(map.iterator()).isExhausted();
		map.put(123);
		Iterator<Object> iterator = map.iterator();
		assertThat(iterator).hasNext();
		assertThat(iterator.next()).isEqualTo(123);
		assertThat(iterator).isExhausted();

		map.put(123L);
		iterator = map.iterator();
		assertThat(iterator).hasNext();
		Object first = iterator.next();
		assertThat(first).satisfiesAnyOf(value -> assertThat(value).isEqualTo(123), value -> assertThat(value).isEqualTo(123L));
		assertThat(iterator).hasNext();
		assertThat(iterator.next())
				.as("must be different from the first value")
				.isNotEqualTo(first)
				.satisfiesAnyOf(value -> assertThat(value).isEqualTo(123), value -> assertThat(value).isEqualTo(123L));
		iterator.remove();
		assertThat(iterator).isExhausted();

		assertThat(map.size()).isEqualTo(1);
	}
}
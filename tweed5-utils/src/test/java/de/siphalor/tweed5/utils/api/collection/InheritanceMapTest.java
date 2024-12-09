package de.siphalor.tweed5.utils.api.collection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InheritanceMapTest {
	@Test
	void full() {
		InheritanceMap<Object> map = new InheritanceMap<>(Object.class);
		map.put(123L);
		map.put(123);
		map.put(456);

		assertThat(map.getAllInstances(Long.class)).containsExactlyInAnyOrder(123L);
		assertThat(map.getAllInstances(Integer.class)).containsExactlyInAnyOrder(123, 456);
		assertThat(map.getAllInstances(Number.class)).containsExactlyInAnyOrder(123L, 123, 456);
	}
}
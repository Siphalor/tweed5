package de.siphalor.tweed5.testutils.generic;

import java.util.*;

public class MapTestUtils {
	public static <K, V> SequencedMap<K, V> sequencedMap(Collection<Map.Entry<K, V>> entries) {
		var map = LinkedHashMap.<K, V>newLinkedHashMap(entries.size());
		entries.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
		return Collections.unmodifiableSequencedMap(map);
	}
}

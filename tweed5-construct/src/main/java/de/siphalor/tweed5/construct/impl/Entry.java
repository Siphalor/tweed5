package de.siphalor.tweed5.construct.impl;

import lombok.Value;

@Value
class Entry<K, V> {
	K key;
	V value;
}

package de.siphalor.tweed5.namingformat.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodePointReaderTest {

	@Test
	void ofString() {
		CodePointReader reader = CodePointReader.ofString("Aüc");

		assertTrue(reader.hasNext());
		assertEquals('A', reader.next());
		assertTrue(reader.hasNext());
		assertEquals('ü', reader.peek());
		assertEquals('ü', reader.peek());
		assertTrue(reader.hasNext());
		assertEquals('ü', reader.next());
		assertTrue(reader.hasNext());
		assertEquals('c', reader.next());
		assertFalse(reader.hasNext());
		assertThrows(Exception.class, reader::next);
	}
}
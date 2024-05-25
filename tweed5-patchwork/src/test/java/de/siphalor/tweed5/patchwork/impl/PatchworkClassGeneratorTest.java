package de.siphalor.tweed5.patchwork.impl;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PatchworkClassGeneratorTest {

	@Test
	void notAnInterface() {
		PatchworkClassGenerator generator = createGenerator(Collections.singletonList(NotAnInterface.class));
		assertThrows(PatchworkClassGenerator.VerificationException.class, generator::verify);
	}

	@Test
	void nonPublicInterface() {
		PatchworkClassGenerator generator = createGenerator(Collections.singletonList(NonPublicInterface.class));
		assertThrows(PatchworkClassGenerator.VerificationException.class, generator::verify);
	}

	@Test
	void duplicateFields() {
		PatchworkClassGenerator generator = createGenerator(Arrays.asList(DuplicateA.class, DuplicateB.class));
		assertThrows(PatchworkClassGenerator.VerificationException.class, generator::verify);
	}

	PatchworkClassGenerator createGenerator(Collection<Class<?>> partClasses) {
		return new PatchworkClassGenerator(
				new PatchworkClassGenerator.Config("de.siphalor.tweed5.patchwork.test.generated"),
				partClasses.stream().map(PatchworkClassPart::new).collect(Collectors.toList())
		);
	}

	public static class NotAnInterface {
	}

	interface NonPublicInterface {
	}

	public interface DuplicateA {
		void test();
	}

	public interface DuplicateB {
		void test();
	}
}
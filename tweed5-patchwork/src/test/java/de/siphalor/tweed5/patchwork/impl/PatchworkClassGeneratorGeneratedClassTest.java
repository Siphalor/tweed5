package de.siphalor.tweed5.patchwork.impl;

import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartIsNullException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PatchworkClassGeneratorGeneratedClassTest {

	PatchworkClassPart partA;
	PatchworkClassPart partB;

	byte[] bytes;
	Class<Patchwork<?>> patchworkClass;
	Patchwork<?> patchwork;

	@BeforeEach
	void setUp() {
		partA = new PatchworkClassPart(ExtensionA.class);
		partB = new PatchworkClassPart(ExtensionB.class);
		PatchworkClassGenerator generator = new PatchworkClassGenerator(
				new PatchworkClassGenerator.Config("de.siphalor.tweed5.core.test")
						.classPrefix("FullTest$")
						.markerInterfaces(Collections.singletonList(MarkerInterface.class)),
				Arrays.asList(partA, partB)
		);

		assertDoesNotThrow(generator::verify);
		assertDoesNotThrow(generator::generate);

		bytes = generator.emit();
		//noinspection unchecked
		patchworkClass = (Class<Patchwork<?>>) assertDoesNotThrow(() -> ByteArrayClassLoader.loadClass(null, bytes));

		patchwork = createPatchworkInstance();
	}

	@Test
	@Disabled("Dumping the class is only for testing purposes")
	void dumpClass() {
		try {
			Path target = File.createTempFile("tweed5-patchwork", ".class").toPath();
			try (OutputStream os = Files.newOutputStream(target)) {
				os.write(bytes);
			}
			System.out.println("Dumped generated class to " + target);

		} catch (IOException e) {
			assertNull(e, "Must not throw exception");
		}
	}

	@Test
	void packageName() {
		assertEquals("de.siphalor.tweed5.core.test", patchworkClass.getPackage().getName());
	}

	@Test
	void className() {
		assertTrue(patchworkClass.getSimpleName().startsWith("FullTest$"), "Generated class name must start with prefix FullTest$, got " + patchworkClass.getSimpleName());
	}

	@Test
	void implementsInterfaces() {
		assertImplements(MarkerInterface.class, patchworkClass);
		assertImplements(Patchwork.class, patchworkClass);
		assertImplements(ExtensionA.class, patchworkClass);
		assertImplements(ExtensionB.class, patchworkClass);
	}

	@Test
	void toStringMethod() {
		int defaultHashCode = System.identityHashCode(patchwork);
		String stringResult = patchwork.toString();
		assertFalse(stringResult.contains(Integer.toHexString(defaultHashCode)), "Expected toString not to be the default toString, got: " + stringResult);
	}

	@Test
	void toStringContent() {
		setFieldValue(patchwork, partA.fieldName(), new ExtensionAImpl());
		((ExtensionA) patchwork).setText("Hello World!");
		String stringResult = patchwork.toString();
		assertTrue(stringResult.contains("Hello World!"), "Expected toString to contain the toString of its extensions, got: " + stringResult);
	}

	@Test
	void hashCodeMethod() {
		int emptyHashCode = patchwork.hashCode();

		setFieldValue(patchwork, partA.fieldName(), new ExtensionAImpl());
		int hashCodeWithA = patchwork.hashCode();

		((ExtensionA) patchwork).setText("Hello World!");
		int hashCodeWithAContent = patchwork.hashCode();

		assertNotEquals(emptyHashCode, hashCodeWithA, "Expected hashCode to be different, got: " + emptyHashCode + " and " + hashCodeWithA);
		assertNotEquals(emptyHashCode, hashCodeWithAContent, "Expected hashCode to be different, got: " + emptyHashCode + " and " + hashCodeWithAContent);
		assertNotEquals(hashCodeWithA, hashCodeWithAContent, "Expected hashCode to be different, got: " + hashCodeWithA + " and " + hashCodeWithAContent);
	}

	@SuppressWarnings("SimplifiableAssertion")
	@Test
	void equalsMethod() {
		//noinspection ConstantValue,SimplifiableAssertion
		assertFalse(patchwork.equals(null));

		//noinspection EqualsWithItself
		assertTrue(patchwork.equals(patchwork));

		Patchwork<?> other = createPatchworkInstance();
		assertTrue(patchwork.equals(other));
		assertTrue(other.equals(patchwork));

		setFieldValue(patchwork, partA.fieldName(), new ExtensionAImpl());
		assertFalse(patchwork.equals(other));

		setFieldValue(other, partA.fieldName(), new ExtensionAImpl());
		assertTrue(patchwork.equals(other));
		assertTrue(other.equals(patchwork));

		((ExtensionA) patchwork).setText("Hello 1!");
		((ExtensionA) other).setText("Hello 2!");
		assertFalse(patchwork.equals(other));
	}

	@Test
	void copy() {
		ExtensionAImpl aImpl = new ExtensionAImpl();
		setFieldValue(patchwork, partA.fieldName(), aImpl);

		Patchwork<?> copy = patchwork.copy();
		Object aValue = getFieldValue(copy, partA.fieldName());
		Object bValue = getFieldValue(copy, partB.fieldName());

		assertSame(aImpl, aValue);
		assertNull(bValue);
	}

	@Test
	void isPartDefined() {
		assertPartDefined(ExtensionA.class, patchwork);
		assertPartDefined(ExtensionB.class, patchwork);
		assertPartUndefined(ExtensionC.class, patchwork);
	}

	static void assertPartDefined(Class<?> anInterface, Patchwork<?> patchwork) {
		assertTrue(patchwork.isPatchworkPartDefined(anInterface), "Patchwork part " + anInterface.getName() + " must be defined");
	}

	static void assertPartUndefined(Class<?> anInterface, Patchwork<?> patchwork) {
		assertFalse(patchwork.isPatchworkPartDefined(anInterface), "Patchwork part " + anInterface.getName() + " must not be defined");
	}

	@Test
	void checkFieldsExist() {
		assertFieldExists(partA.fieldName(), ExtensionA.class);
		assertFieldExists(partB.fieldName(), ExtensionB.class);
	}

	void assertFieldExists(String fieldName, Class<?> fieldType) {
		Field field = getField(fieldName);
		assertEquals(fieldType, field.getType());
	}

	@Test
	void isPartSet() {
		assertPartUnset(ExtensionA.class, patchwork);
		assertPartUnset(ExtensionB.class, patchwork);
		assertPartUnset(ExtensionC.class, patchwork);

		setFieldValue(patchwork, partA.fieldName(), new ExtensionAImpl());

		assertPartSet(ExtensionA.class, patchwork);
		assertPartUnset(ExtensionB.class, patchwork);
		assertPartUnset(ExtensionC.class, patchwork);

		setFieldValue(patchwork, partB.fieldName(), new ExtensionBImpl());

		assertPartSet(ExtensionA.class, patchwork);
		assertPartSet(ExtensionB.class, patchwork);
		assertPartUnset(ExtensionC.class, patchwork);
	}

	@Test
	void inheritedMethodCalls() {
		assertThrows(PatchworkPartIsNullException.class, () -> ((ExtensionA) patchwork).getText());
		assertThrows(PatchworkPartIsNullException.class, () -> ((ExtensionB) patchwork).multiply(1, 2));

		setFieldValue(patchwork, partA.fieldName(), new ExtensionAImpl());
		setFieldValue(patchwork, partB.fieldName(), new ExtensionBImpl());

		assertEquals(6, assertDoesNotThrow(() -> ((ExtensionB) patchwork).multiply(2, 3)), "Method of extension b must be working");

		assertDoesNotThrow(() -> ((ExtensionA) patchwork).setText("something"));
		assertEquals("something", ((ExtensionA) patchwork).getText());
	}

	Patchwork<?> createPatchworkInstance() {
		Constructor<?> constructor = assertDoesNotThrow(() -> patchworkClass.getConstructor(), "The generated class' constructor must be accessible");
		return assertDoesNotThrow(() -> (Patchwork<?>) constructor.newInstance());
	}

	Object getFieldValue(Patchwork<?> patchwork, String fieldName) {
		return assertDoesNotThrow(() -> getField(fieldName).get(patchwork));
	}

	void setFieldValue(Patchwork<?> patchwork, String fieldName, Object value) {
		assertDoesNotThrow(() -> getField(fieldName).set(patchwork, value), "Field " + fieldName + " must be accessible");
	}

	Field getField(String fieldName) {
		return assertDoesNotThrow(() -> patchworkClass.getField(fieldName), "Field " + fieldName + " must exist and be public");
	}

	static void assertPartSet(Class<?> anInterface, Patchwork<?> patchwork) {
		assertTrue(patchwork.isPatchworkPartSet(anInterface), "Patchwork part " + anInterface.getName() + " must be set");
	}

	static void assertPartUnset(Class<?> anInterface, Patchwork<?> patchwork) {
		assertFalse(patchwork.isPatchworkPartSet(anInterface), "Patchwork part " + anInterface.getName() + " must not be set");
	}

	static void assertImplements(Class<?> anInterface, Class<?> theClass) {
		assertTrue(anInterface.isAssignableFrom(theClass), "Class " + theClass.getName() + " must implement " + anInterface.getName());
	}

	public interface MarkerInterface {}

	public interface ExtensionA {
		String getText();

		void setText(String text);
	}

	@Data
	@Accessors(fluent = false)
	static class ExtensionAImpl implements ExtensionA {
		private String text;
	}

	public interface ExtensionB {
		int multiply(int a, int b);
	}

	static class ExtensionBImpl implements ExtensionB {
		public int multiply(int a, int b) {
			return a * b;
		}
	}

	public interface ExtensionC {
		void test();
	}
}
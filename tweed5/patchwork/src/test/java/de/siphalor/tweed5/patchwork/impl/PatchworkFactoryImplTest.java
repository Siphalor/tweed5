package de.siphalor.tweed5.patchwork.impl;

import de.siphalor.tweed5.patchwork.api.InvalidPatchworkAccessException;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatchworkFactoryImplTest {
	@Test
	void test() {
		PatchworkFactoryImpl.Builder builder = new PatchworkFactoryImpl.Builder();
		PatchworkPartAccess<String> stringAccess1 = builder.registerPart(String.class);
		PatchworkPartAccess<@Nullable Number> integerAccess1 = builder.registerPart(Number.class);
		PatchworkPartAccess<String> stringAccess2 = builder.registerPart(String.class);
		PatchworkFactory factory = builder.build();

		Patchwork patchwork = factory.create();

		assertThat(patchwork.get(stringAccess1)).isNull();
		assertThat(patchwork.get(integerAccess1)).isNull();
		assertThat(patchwork.get(stringAccess2)).isNull();

		patchwork.set(stringAccess1, "Hello");
		patchwork.set(stringAccess2, "World");
		patchwork.set(integerAccess1, 123);

		assertThat(patchwork.get(stringAccess1)).isEqualTo("Hello");
		assertThat(patchwork.get(integerAccess1)).isEqualTo(123);
		assertThat(patchwork.get(stringAccess2)).isEqualTo("World");

		patchwork.set(integerAccess1, null);
		assertThat(patchwork.get(integerAccess1)).isNull();
	}

	@Test
	void copy() {
		PatchworkFactoryImpl.Builder builder = new PatchworkFactoryImpl.Builder();
		PatchworkPartAccess<String> stringAccess = builder.registerPart(String.class);
		PatchworkFactory factory = builder.build();

		Patchwork patchwork = factory.create();
		patchwork.set(stringAccess, "Hello");

		Patchwork copy = patchwork.copy();
		assertThat(copy.get(stringAccess)).isEqualTo("Hello");
		assertThat(copy).isEqualTo(patchwork);
	}

	@Test
	void lateRegister() {
		PatchworkFactoryImpl.Builder builder = new PatchworkFactoryImpl.Builder();
		builder.build();
		assertThatThrownBy(() -> builder.registerPart(String.class)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void doubleBuild() {
		PatchworkFactoryImpl.Builder builder = new PatchworkFactoryImpl.Builder();
		builder.build();
		assertThatThrownBy(builder::build).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void invalidAccess() {
		PatchworkFactoryImpl.Builder builder = new PatchworkFactoryImpl.Builder();
		builder.registerPart(String.class);
		PatchworkFactory factory = builder.build();

		Patchwork patchwork = factory.create();

		PatchworkFactoryImpl.Builder otherBuilder = new PatchworkFactoryImpl.Builder();
		PatchworkPartAccess<String> otherAccess = otherBuilder.registerPart(String.class);
		otherBuilder.build();

		assertThatThrownBy(() -> patchwork.get(otherAccess)).isInstanceOf(InvalidPatchworkAccessException.class);
		assertThatThrownBy(() -> patchwork.set(otherAccess, "Hello")).isInstanceOf(InvalidPatchworkAccessException.class);
	}

	@Test
	void setWrongType() {
		PatchworkFactoryImpl.Builder builder = new PatchworkFactoryImpl.Builder();
		//noinspection unchecked
		PatchworkPartAccess<Object> access = ((PatchworkPartAccess<Object>)(Object) builder.registerPart(String.class));
		PatchworkFactory factory = builder.build();

		Patchwork patchwork = factory.create();
		assertThatThrownBy(() -> patchwork.set(access, 123)).isInstanceOf(IllegalArgumentException.class);
	}
}

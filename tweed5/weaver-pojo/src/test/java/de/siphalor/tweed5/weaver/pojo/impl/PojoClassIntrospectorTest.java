package de.siphalor.tweed5.weaver.pojo.impl;

import de.siphalor.tweed5.weaver.pojo.impl.weaving.PojoClassIntrospector;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
class PojoClassIntrospectorTest {
	@Test
	void propertiesClassicAccessors() {
		PojoClassIntrospector introspector = PojoClassIntrospector.forClass(ClassicPojo.class);

		ClassicPojo instance = new ClassicPojo(123);

		Map<String, PojoClassIntrospector.Property> result = assertDoesNotThrow(introspector::properties);
		assertThat(result).hasSize(4)
				.hasEntrySatisfying("integer", property -> {
					assertThat(property.field().getName()).isEqualTo("integer");
					assertThat(property.field().getDeclaringClass()).isEqualTo(ClassicPojo.class);
					assertThat(property.type()).isEqualTo(Integer.TYPE);
					assertThat(property.isFinal()).isTrue();
					assertThat(property.getter()).isNotNull();
					assertThat(property.setter()).isNull();

					assertThatNoException()
							.isThrownBy(() -> assertThat((int) property.getter().invokeExact(instance)).isEqualTo(123));
				})
				.hasEntrySatisfying("str", property -> {
					assertThat(property.field().getName()).isEqualTo("str");
					assertThat(property.field().getDeclaringClass()).isEqualTo(ClassicPojo.class);
					assertThat(property.type()).isEqualTo(String.class);
					assertThat(property.isFinal()).isFalse();
					assertThat(property.getter()).isNull();
					assertThat(property.setter()).isNotNull();

					assertThatNoException().isThrownBy(() -> property.setter().invoke(instance, "Hello"));
					assertThat(instance.str).isEqualTo("Hello");
				})
				.hasEntrySatisfying("bool", property -> {
					assertThat(property.field().getName()).isEqualTo("bool");
					assertThat(property.field().getDeclaringClass()).isEqualTo(ClassicPojo.class);
					assertThat(property.type()).isEqualTo(Boolean.TYPE);
					assertThat(property.isFinal()).isFalse();
					assertThat(property.getter()).isNotNull();
					assertThat(property.setter()).isNotNull();

					assertThatNoException()
							.isThrownBy(() -> property.setter().invoke(instance, true));
					assertThatNoException()
							.isThrownBy(() -> assertThat((boolean) property.getter().invokeExact(instance)).isTrue());
				})
				.hasEntrySatisfying("boolObj", property -> {
					assertThat(property.field().getName()).isEqualTo("boolObj");
					assertThat(property.field().getDeclaringClass()).isEqualTo(ClassicPojo.class);
					assertThat(property.type()).isEqualTo(Boolean.class);
					assertThat(property.getter()).isNotNull();
					assertThat(property.setter()).isNotNull();

					assertThatNoException()
							.isThrownBy(() -> property.setter().invoke(instance, true));
					assertThatNoException()
							.isThrownBy(() -> assertThat((Boolean) property.getter().invokeExact(instance)).isTrue());
				});
	}

	@Test
	void propertiesFluidAndChainedAccessors() {
		PojoClassIntrospector introspector = PojoClassIntrospector.forClass(FluidChainedPojo.class);

		FluidChainedPojo instance = new FluidChainedPojo(123);

		Map<String, PojoClassIntrospector.Property> result = assertDoesNotThrow(introspector::properties);
		assertThat(result).hasSize(3)
				.hasEntrySatisfying("integer", property -> {
					assertThat(property.field().getName()).isEqualTo("integer");
					assertThat(property.field().getDeclaringClass()).isEqualTo(FluidChainedPojo.class);
					assertThat(property.type()).isEqualTo(Integer.TYPE);
					assertThat(property.isFinal()).isTrue();
					assertThat(property.getter()).isNotNull();
					assertThat(property.setter()).isNull();

					assertThatNoException()
							.isThrownBy(() -> assertThat((int) property.getter().invokeExact(instance)).isEqualTo(123));
				})
				.hasEntrySatisfying("str", property -> {
					assertThat(property.field().getName()).isEqualTo("str");
					assertThat(property.field().getDeclaringClass()).isEqualTo(FluidChainedPojo.class);
					assertThat(property.type()).isEqualTo(String.class);
					assertThat(property.isFinal()).isFalse();
					assertThat(property.getter()).isNull();
					assertThat(property.setter()).isNotNull();

					assertThatNoException().isThrownBy(() -> property.setter().invoke(instance, "Hello"));
					assertThat(instance.str).isEqualTo("Hello");
				})
				.hasEntrySatisfying("bool", property -> {
					assertThat(property.field().getName()).isEqualTo("bool");
					assertThat(property.field().getDeclaringClass()).isEqualTo(FluidChainedPojo.class);
					assertThat(property.type()).isEqualTo(Boolean.TYPE);
					assertThat(property.isFinal()).isFalse();
					assertThat(property.getter()).isNotNull();
					assertThat(property.setter()).isNotNull();

					assertThatNoException()
							.isThrownBy(() -> property.setter().invoke(instance, true));
					assertThatNoException()
							.isThrownBy(() -> assertThat((boolean) property.getter().invokeExact(instance)).isTrue());
				});
	}

	@Test
	void propertiesDirectAccess() {
		PojoClassIntrospector introspector = PojoClassIntrospector.forClass(DirectAccessPojo.class);

		DirectAccessPojo instance = new DirectAccessPojo(123);

		Map<String, PojoClassIntrospector.Property> result = assertDoesNotThrow(introspector::properties);
		assertThat(result).hasSize(2)
				.hasEntrySatisfying("integer", property -> {
					assertThat(property.field().getName()).isEqualTo("integer");
					assertThat(property.field().getDeclaringClass()).isEqualTo(DirectAccessPojo.class);
					assertThat(property.type()).isEqualTo(Integer.TYPE);
					assertThat(property.isFinal()).isTrue();
					assertThat(property.getter()).isNotNull();
					assertThat(property.setter()).isNull();

					assertThatNoException()
							.isThrownBy(() -> assertThat(property.getter().invoke(instance)).isEqualTo(123));
				})
				.hasEntrySatisfying("str", property -> {
					assertThat(property.field().getName()).isEqualTo("str");
					assertThat(property.field().getDeclaringClass()).isEqualTo(DirectAccessPojo.class);
					assertThat(property.type()).isEqualTo(String.class);
					assertThat(property.isFinal()).isFalse();
					assertThat(property.getter()).isNotNull();
					assertThat(property.setter()).isNotNull();

					assertThatNoException()
							.isThrownBy(() -> property.setter().invoke(instance, "abcd"));
					assertThat(instance.str).isEqualTo("abcd");
				});
	}

	@Test
	void noArgsConstructorNone() {
		PojoClassIntrospector introspector = PojoClassIntrospector.forClass(ClassicPojo.class);
		assertThat(introspector.noArgsConstructor()).isNull();
	}

	@Test
	void noArgsConstructor() {
		PojoClassIntrospector introspector = PojoClassIntrospector.forClass(NoArgs.class);

		assertThat(introspector.noArgsConstructor())
				.isNotNull()
				.satisfies(constructor -> assertThat(constructor.invoke()).isInstanceOf(NoArgs.class));
	}

	@SuppressWarnings("unused")
	@RequiredArgsConstructor
	public static class ClassicPojo {
		final int integer;
		String str;
		boolean bool;
		Boolean boolObj;

		public int getInteger() {
			return integer;
		}

		public void setStr(String str) {
			this.str = str;
		}

		public boolean isBool() {
			return bool;
		}

		public void setBool(boolean bool) {
			this.bool = bool;
		}

		public Boolean isBoolObj() {
			return boolObj;
		}

		public void setBoolObj(Boolean boolObj) {
			this.boolObj = boolObj;
		}
	}

	@SuppressWarnings("unused")
	@RequiredArgsConstructor
	public static class FluidChainedPojo {
		final int integer;
		String str;
		boolean bool;

		public int integer() {
			return integer;
		}

		public FluidChainedPojo str(String value) {
			this.str = value;
			return this;
		}

		public boolean bool() {
			return bool;
		}

		public FluidChainedPojo bool(boolean value) {
			this.bool = value;
			return this;
		}
	}

	@SuppressWarnings("unused")
	@RequiredArgsConstructor
	public static class DirectAccessPojo {
		public final int integer;
		public String str;
	}

	public static class NoArgs {
		public String noop;
	}
}
package de.siphalor.tweed5.construct.impl;

import de.siphalor.tweed5.construct.api.ConstructParameter;
import de.siphalor.tweed5.construct.api.TweedConstruct;
import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import lombok.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@SuppressWarnings("unused")
class TweedConstructFactoryImplTest {
	@SuppressWarnings("unchecked")
	@Test
	void factoryBuilder() {
		val builder = TweedConstructFactoryImpl.builder(DummyBase.class);
		builder.typedArg(Integer.class).typedArg(String.class);
		builder.namedArg("hey", String.class).namedArg("ho", String.class);
		TweedConstructFactory<DummyBase> factory = builder.build();
		assertThat(factory)
				.asInstanceOf(type(TweedConstructFactoryImpl.class))
				.satisfies(
						f -> assertThat(f.constructBaseClass()).isEqualTo(DummyBase.class),
						f -> assertThat(f.typedArgs())
								.containsExactlyInAnyOrder(Integer.class, String.class),
						f -> assertThat(f.namedArgs())
								.containsEntry("hey", String.class)
								.containsEntry("ho", String.class)
								.hasSize(2)
				);

	}

	@SuppressWarnings("unchecked")
	@Test
	void factoryBuilderPrimitives() {
		val builder = TweedConstructFactoryImpl.builder(DummyBase.class);
		builder.typedArg(int.class).typedArg(long.class);
		builder.namedArg("bool", boolean.class).namedArg("byte", byte.class);
		TweedConstructFactory<DummyBase> factory = builder.build();
		assertThat(factory)
				.asInstanceOf(type(TweedConstructFactoryImpl.class))
				.satisfies(
						f -> assertThat(f.typedArgs())
								.containsExactlyInAnyOrder(Integer.class, Long.class),
						f -> assertThat(f.namedArgs())
								.containsEntry("bool", Boolean.class)
								.containsEntry("byte", Byte.class)
								.hasSize(2)
				);
	}

	@Test
	void factoryBuilderDuplicateTypedArgs() {
		val builder = TweedConstructFactoryImpl.builder(DummyBase.class);
		assertThatThrownBy(() -> {
			builder.typedArg(Integer.class).typedArg(String.class).typedArg(Integer.class);
			builder.build();
		}).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("java.lang.Integer");
	}

	@Test
	void factoryBuilderDuplicateNamedArgs() {
		val builder = TweedConstructFactoryImpl.builder(DummyBase.class);
		assertThatThrownBy(() -> {
			builder.namedArg("hey", String.class).namedArg("ho", String.class).namedArg("hey", Integer.class);
			builder.build();
		}).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("hey");
	}

	@Test
	void constructMissingInheritance() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class).typedArg(Integer.class).build();
		//noinspection unchecked,RedundantCast
		assertThatThrownBy(() ->
				factory.construct((Class<? extends DummyBase>) (Class<?>) MissingInheritance.class)
		).isInstanceOf(IllegalArgumentException.class).hasMessageContaining(DummyBase.class.getName());
	}

	@Test
	void constructPrivateConstructor() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class).typedArg(Integer.class).build();
		assertThatThrownBy(() -> factory.construct(PrivateConstructor.class)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void constructConflictingConstructors() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class).typedArg(Integer.class).build();
		assertThatThrownBy(() -> factory.construct(ConstructorConflict.class))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void constructConflictingStatics() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class).typedArg(Integer.class).build();
		assertThatThrownBy(() -> factory.construct(StaticConflict.class))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void constructConflictingMixed() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class).typedArg(Integer.class).build();
		assertThatThrownBy(() -> factory.construct(MixedConflict.class))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void constructMissingTypedValue() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.namedArg("context", String.class)
				.build();
		assertThatThrownBy(() ->
				factory.construct(SingleConstructor.class)
						.namedArg("user", "Siphalor")
						.namedArg("context", "world")
						.finish()
		).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("java.lang.Integer");
	}

	@Test
	void constructMissingNamedValue() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.namedArg("context", String.class)
				.build();
		assertThatThrownBy(() ->
				factory.construct(SingleConstructor.class)
						.typedArg(123)
						.namedArg("context", "world")
						.finish()
		).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("user");
	}

	@Test
	void constructForSingleConstructor() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.namedArg("context", String.class)
				.namedArg("other", String.class)
				.build();
		val result = factory.construct(SingleConstructor.class)
				.typedArg(123)
				.namedArg("user", "Siphalor")
				.namedArg("context", "world")
				.namedArg("other", "something")
				.finish();
		assertThat(result).isEqualTo(new SingleConstructor(123, "Siphalor", "world"));
	}

	@Test
	void constructForStatic() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.namedArg("context", String.class)
				.namedArg("other", String.class)
				.build();
		val result = factory.construct(Static.class)
				.typedArg(123)
				.namedArg("user", "Siphalor")
				.namedArg("context", "world")
				.namedArg("other", "something")
				.finish();
		assertThat(result).isEqualTo(new Static(1230, "Siphalor", "static"));
	}

	@ParameterizedTest
	@CsvSource(
			{
					"de.siphalor.tweed5.construct.impl.TweedConstructFactoryImplTest$DummyBase, base, 4560",
					"de.siphalor.tweed5.construct.impl.TweedConstructFactoryImplTest$DummyOtherBase, other, -456",
					"de.siphalor.tweed5.construct.impl.TweedConstructFactoryImplTest$DummyAltBase, alt, -4560",
			}
	)
	void constructFindBase(Class<? super FindBase> base, String origin, int value) {
		val factory = TweedConstructFactoryImpl.builder(base).typedArg(int.class).build();
		val result = factory.construct(FindBase.class).typedArg(456).finish();
		assertThat(result.origin()).isEqualTo(origin);
		assertThat(result.value()).isEqualTo(value);
	}

	@Test
	void constructPrimitives() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.typedArg(long.class)
				.build();
		val result = factory.construct(Primitives.class)
				.typedArg(1)
				.typedArg(2L)
				.finish();
		assertThat(result).isEqualTo(new Primitives(1, 2L));
	}

	@Test
	void constructNamedCasting() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class).namedArg("test", Integer.class).build();
		val result = factory.construct(NamedCasting.class).namedArg("test", 1234).finish();
		assertThat(result.value()).isEqualTo(1234);
	}

	@Test
	void constructDuplicateParams() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(String.class)
				.typedArg(Long.class)
				.namedArg("number", int.class)
				.build();
		assertThatThrownBy(() -> factory.construct(DuplicateParams.class))
				.isInstanceOf(IllegalStateException.class)
				.message()
				.contains("java.lang.String", "number")
				.containsIgnoringCase("typed")
				.containsIgnoringCase("named")
				.hasLineCount(3);
	}

	@Test
	void constructUnexpectedTypedParameter() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Long.class)
				.namedArg("user", String.class)
				.build();
		assertThatThrownBy(() -> factory.construct(Static.class))
				.isInstanceOf(IllegalStateException.class)
				.message()
				.contains("java.lang.Integer")
				.containsIgnoringCase("typed")
				.hasLineCount(2);
	}

	@Test
	void constructUnexpectedNamedParameter() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("other", String.class)
				.build();
		assertThatThrownBy(() -> factory.construct(Static.class))
				.isInstanceOf(IllegalStateException.class)
				.message()
				.contains("user", "java.lang.String")
				.containsIgnoringCase("named")
				.hasLineCount(2);
	}

	@Test
	void constructIllegalNamedType() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", Long.class)
				.build();
		assertThatThrownBy(() -> factory.construct(Static.class))
				.isInstanceOf(IllegalStateException.class)
				.message()
				.contains("user", "java.lang.String", "java.lang.Long")
				.containsIgnoringCase("named")
				.hasLineCount(2);
	}

	@Test
	void constructFinishUnknownTypedArgument() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.build();
		assertThatThrownBy(() ->
				factory.construct(Static.class)
						.typedArg(12)
						.namedArg("user", "Someone")
						.typedArg(567L)
						.finish()
		).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("java.lang.Long");
	}

	@Test
	void constructFinishUnknownNamedArgument() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.build();
		assertThatThrownBy(() ->
				factory.construct(Static.class)
						.typedArg(12)
						.namedArg("user", "Someone")
						.namedArg("other", "test")
						.finish()
		).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("other");
	}

	@Test
	void constructFinishNamedArgumentWrongType() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.build();
		assertThatThrownBy(() ->
				factory.construct(Static.class)
						.typedArg(12)
						.namedArg("user", 456L)
						.finish())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("user", "java.lang.String", "java.lang.Long");
	}

	@Test
	void constructFinishInconsistentNamedArgument() {
		val factory = TweedConstructFactoryImpl.builder(DummyBase.class)
				.typedArg(Integer.class)
				.namedArg("user", String.class)
				.build();
		//noinspection unchecked
		assertThatThrownBy(() ->
				factory.construct(Static.class)
						.typedArg((Class<Object>)(Class<?>) String.class, 123)
						.finish()
		).isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@CsvSource(
			{
					"boolean, java.lang.Boolean",
					"java.lang.Boolean, java.lang.Boolean",
					"byte, java.lang.Byte",
					"java.lang.Byte, java.lang.Byte",
					"char, java.lang.Character",
					"java.lang.Character, java.lang.Character",
					"short, java.lang.Short",
					"java.lang.Short, java.lang.Short",
					"int, java.lang.Integer",
					"java.lang.Integer, java.lang.Integer",
					"long, java.lang.Long",
					"java.lang.Long, java.lang.Long",
					"float, java.lang.Float",
					"java.lang.Float, java.lang.Float",
					"double, java.lang.Double",
					"java.lang.Double, java.lang.Double",
					"void, java.lang.Void",
					"java.lang.Void, java.lang.Void",
					"java.lang.String, java.lang.String",
			}
	)
	void boxClass(Class<?> type, Class<?> expected) {
		assertThat(TweedConstructFactoryImpl.boxClass(type)).isEqualTo(expected);
	}

	interface DummyBase {
	}

	interface DummyOtherBase {
	}

	interface DummyAltBase {
	}

	public static class MissingInheritance {
		@TweedConstruct(DummyBase.class)
		public MissingInheritance() {
		}
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class PrivateConstructor implements DummyBase {
	}

	public static class ConstructorConflict implements DummyBase {
		@TweedConstruct(DummyBase.class)
		public ConstructorConflict() {
		}

		@TweedConstruct(DummyBase.class)
		public ConstructorConflict(String context) {
		}
	}

	public static class StaticConflict implements DummyBase {
		@TweedConstruct(DummyBase.class)
		public static StaticConflict ofA() {
			return null;
		}

		@TweedConstruct(DummyBase.class)
		public static StaticConflict ofB() {
			return null;
		}
	}

	public static class MixedConflict implements DummyBase {
		@TweedConstruct(DummyBase.class)
		public MixedConflict() {
		}

		@TweedConstruct(DummyBase.class)
		public static MixedConflict of() {
			return null;
		}
	}

	@Getter
	@EqualsAndHashCode
	public static class SingleConstructor implements DummyBase {
		private final Integer times;
		private final String user;
		private final String context;

		public SingleConstructor(
				Integer times,
				@ConstructParameter(name = "user") String user,
				@ConstructParameter(name = "context") String context
		) {
			this.times = times;
			this.user = user;
			this.context = context;
		}
	}

	@Value
	public static class Static implements DummyBase {
		Integer times;
		String user;
		String context;

		@TweedConstruct(DummyBase.class)
		public static Static of(Integer times, @ConstructParameter(name = "user") String user) {
			return new Static(times * 10, user, "static");
		}
	}

	@Value
	@AllArgsConstructor
	public static class FindBase implements DummyBase, DummyOtherBase, DummyAltBase {
		String origin;
		int value;

		@TweedConstruct(DummyBase.class)
		public FindBase(int value) {
			this("base", value * 10);
		}

		@TweedConstruct(DummyOtherBase.class)
		public static FindBase ofOther(int value) {
			return new FindBase("other", value * -1);
		}

		@TweedConstruct(DummyAltBase.class)
		public static FindBase ofAlt(int value) {
			return new FindBase("alt", value * -10);
		}
	}

	@Value
	public static class Primitives implements DummyBase {
		int a;
		Long b;
	}

	@Value
	public static class NamedCasting implements DummyBase {
		Number value;
		public NamedCasting(@ConstructParameter(name = "test") Number value) {
			this.value = value;
		}
	}

	@Value
	public static class DuplicateParams implements DummyBase {
		public DuplicateParams(
				String one,
				String two,
				Long test,
				@ConstructParameter(name = "number") int three,
				@ConstructParameter(name = "number") int four
		) {
		}
	}
}

package de.siphalor.tweed5.construct.api;

import de.siphalor.tweed5.construct.impl.TweedConstructFactoryImpl;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A factory that allows to construct instances of subclasses of a specific type.
 * <p>
 * This factory basically extends the interfaces' contract to include
 * a public constructor or public static method with any of the defined arguments.
 * <p>
 * The factory should usually be defined as a public static final member of the base class.
 *
 * @param <T> the base class/interface
 */
public interface TweedConstructFactory<T> {
	/**
	 * Starts building a new factory for the given base class.
	 */
	static <T> TweedConstructFactory.@NotNull FactoryBuilder<T> builder(Class<T> baseClass) {
		return TweedConstructFactoryImpl.builder(baseClass);
	}

	/**
	 * Starts the instantiation process for a subclass.
	 * All defined arguments must be bound to values.
	 */
	@CheckReturnValue
	@Contract(pure = true)
	<C extends T> @NotNull Construct<C> construct(@NotNull Class<C> subClass);

	/**
	 * Builder for the factory.
	 */
	interface FactoryBuilder<T> {
		/**
		 * Defines a new typed argument of the given type.
		 */
		@Contract(mutates = "this", value = "_ -> this")
		<A> @NotNull FactoryBuilder<T> typedArg(@NotNull Class<A> argType);

		/**
		 * Defines a new named argument with the given name and value type.
		 */
		@Contract(mutates = "this", value = "_, _ -> this")
		<A> @NotNull FactoryBuilder<T> namedArg(@NotNull String name, @NotNull Class<A> argType);

		/**
		 * Builds the factory.
		 */
		@Contract(pure = true)
		@NotNull TweedConstructFactory<T> build();
	}

	/**
	 * Builder-style helper for the instantiation process.
	 * <p>
	 * Allows to successively bind all previously defined arguments to actual values.
	 * <p>
	 * Any method call in this class may perform checks against the defined arguments and throw according exceptions.
	 */
	interface Construct<C> {
		/**
		 * Binds a value to a typed argument of the exact same class.
		 * <p>
		 * This will not work if the given value merely inherits from the defined class.
		 * Use {@link #typedArg(Class, Object)} for these cases instead.
		 * @see #namedArg(String, Object)
		 */
		@Contract(mutates = "this", value = "_ -> this")
		<A> @NotNull Construct<C> typedArg(@NotNull A value);

		/**
		 * Binds a value to a typed argument of the given type.
		 * <p>
		 * This allows binding the value to super classes of the value.
		 * @see #typedArg(Object)
		 * @see #namedArg(String, Object)
		 */
		@Contract(mutates = "this", value = "_, _ -> this")
		<A> @NotNull Construct<C> typedArg(@NotNull Class<? super A> argType, @Nullable A value);

		/**
		 * Binds a value to a named argument.
		 * @see #typedArg(Object)
		 */
		@Contract(mutates = "this", value = "_, _ -> this")
		<A> @NotNull Construct<C> namedArg(@NotNull String name, @Nullable A value);

		/**
		 * Finishes the binding and actually constructs the class.
		 */
		@Contract(pure = true)
		@NotNull C finish();
	}
}

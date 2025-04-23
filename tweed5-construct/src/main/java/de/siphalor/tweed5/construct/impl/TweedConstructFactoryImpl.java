package de.siphalor.tweed5.construct.impl;

import de.siphalor.tweed5.construct.api.ConstructParameter;
import de.siphalor.tweed5.construct.api.TweedConstruct;
import de.siphalor.tweed5.construct.api.TweedConstructFactory;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter(AccessLevel.PACKAGE)
public class TweedConstructFactoryImpl<T> implements TweedConstructFactory<T> {
	private static final int CONSTRUCTOR_MODIFIERS = Modifier.PUBLIC;
	private static final int STATIC_METHOD_MODIFIERS = Modifier.PUBLIC | Modifier.STATIC;

	private final Class<T> constructBaseClass;
	private final Set<Class<?>> typedArgs;
	private final Map<String, Class<?>> namedArgs;
	private final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
	private final Map<Class<?>, Optional<ConstructTarget<?>>> cachedConstructTargets = new HashMap<>();
	@SuppressWarnings("unused")
	private final ReadWriteLock cachedConstructTargetsLock = new ReentrantReadWriteLock();

	public static <T> TweedConstructFactoryImpl.FactoryBuilder<T> builder(Class<T> baseClass) {
		return new FactoryBuilder<>(baseClass);
	}

	@Override
	public <C extends T> TweedConstructFactory.@NotNull Construct<C> construct(@NotNull Class<C> subClass) {
		return new Construct<>(getConstructTarget(subClass));
	}

	private <C extends T> @NotNull ConstructTarget<C> getConstructTarget(Class<C> type) {
		ConstructTarget<C> cachedConstructTarget = readConstructTargetFromCache(type);
		if (cachedConstructTarget != null) {
			return cachedConstructTarget;
		}
		ConstructTarget<C> constructTarget = locateConstructTarget(type);
		cacheConstructTarget(type, constructTarget);
		return constructTarget;
	}

	@Locked.Read("cachedConstructTargetsLock")
	private <C extends T> @Nullable ConstructTarget<C> readConstructTargetFromCache(Class<C> type) {
		Optional<ConstructTarget<?>> cachedConstructTarget = cachedConstructTargets.get(type);
		if (cachedConstructTarget != null) {
			if (!cachedConstructTarget.isPresent()) {
				throw new IllegalStateException("Could not locate construct for " + type.getName());
			} else {
				//noinspection unchecked
				return (ConstructTarget<C>) cachedConstructTarget.get();
			}
		}
		return null;
	}

	@Locked.Write("cachedConstructTargetsLock")
	private <C extends T> void cacheConstructTarget(Class<C> type, ConstructTarget<C> constructTarget) {
		cachedConstructTargets.put(type, Optional.of(constructTarget));
	}

	private <C extends T> ConstructTarget<C> locateConstructTarget(Class<C> type) {
		if (!constructBaseClass.isAssignableFrom(type)) {
			throw new IllegalArgumentException(
					"Type " + type.getName() + " is not a subclass of " + constructBaseClass.getName()
			);
		}

		Collection<Constructor<?>> constructorCandidates = findConstructorCandidates(type);
		Collection<Method> staticConstructorCandidates = findStaticConstructorCandidates(type);

		List<Executable> annotated = Stream.concat(constructorCandidates.stream(), staticConstructorCandidates.stream())
				.filter(candidate -> {
					TweedConstruct annotation = candidate.getAnnotation(TweedConstruct.class);
					return annotation != null && annotation.value().equals(constructBaseClass);
				})
				.collect(Collectors.toList());

		if (annotated.size() > 1) {
			throw new IllegalStateException(
					"Found multiple matching constructors for " + type.getName()
							+ " annotated with a matching TweedConstruct for " + constructBaseClass.getName() + ": "
							+ annotated
			);
		} else if (annotated.size() == 1) {
			return resolveConstructTarget(type, annotated.get(0));
		} else if (constructorCandidates.size() == 1) {
			return resolveConstructTarget(type, constructorCandidates.iterator().next());
		} else {
			throw new IllegalStateException(
					"Failed to determine actual constructor on " + type.getName()
							+ " for " + constructBaseClass.getName() + ". "
							+ "Constructor candidates: " + constructorCandidates + "; "
							+ "Static method candidates: " + staticConstructorCandidates + ". "
							+ "The desired constructor should be marked with @" + TweedConstruct.class.getName()
			);
		}
	}

	private Collection<Constructor<?>> findConstructorCandidates(Class<?> type) {
		return Arrays.stream(type.getConstructors())
				.filter(constructor -> (constructor.getModifiers() & CONSTRUCTOR_MODIFIERS) == CONSTRUCTOR_MODIFIERS)
				.collect(Collectors.toList());
	}

	private Collection<Method> findStaticConstructorCandidates(Class<?> type) {
		return Arrays.stream(type.getDeclaredMethods())
				.filter(method -> (method.getModifiers() & STATIC_METHOD_MODIFIERS) == STATIC_METHOD_MODIFIERS)
				.filter(method -> type.isAssignableFrom(method.getReturnType()))
				.collect(Collectors.toList());
	}

	private <C extends T> ConstructTarget<C> resolveConstructTarget(Class<C> type, Executable executable) {
		Object[] argOrder = new Object[executable.getParameterCount()];

		Map<Class<?>, List<Parameter>> typedParameters = new HashMap<>();
		Map<String, List<Parameter>> namedParameters = new HashMap<>();
		Parameter[] parameters = executable.getParameters();
		boolean issue = false;
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			ConstructParameter annotation = parameter.getAnnotation(ConstructParameter.class);
			if (annotation != null) {
				String name = annotation.name();
				List<Parameter> named = namedParameters.computeIfAbsent(name, n -> new ArrayList<>());
				named.add(parameter);
				Class<?> argType = namedArgs.get(name);
				argOrder[i] = name;
				if (!issue && (
						named.size() > 1
								|| argType == null
								|| !boxClass(parameter.getType()).isAssignableFrom(argType)
				)) {
					issue = true;
				}
			} else {
				Class<?> paramType = boxClass(parameter.getType());
				List<Parameter> typed = typedParameters.computeIfAbsent(paramType, n -> new ArrayList<>());
				typed.add(parameter);
				argOrder[i] = paramType;
				if (!issue && (typed.size() > 1 || !typedArgs.contains(paramType))) {
					issue = true;
				}
			}
		}

		if (issue) {
			throw new IllegalStateException(
					createConstructorTargetArgCheckFailMessage(executable, typedParameters, namedParameters)
			);
		}

		return new ConstructTarget<>(type, argOrder, createInvokerFromCandidate(type, executable));
	}

	private <C> Function<Object[], C> createInvokerFromCandidate(Class<C> type, Executable executable) {
		MethodHandle handle;
		try {
			if (executable instanceof Method) {
				handle = lookup.unreflect((Method) executable);
			} else if (executable instanceof Constructor) {
				handle = lookup.unreflectConstructor((Constructor<?>) executable);
			} else {
				throw new IllegalStateException("Unsupported executable type: " + executable);
			}
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Constructor for type " + type.getName() + " is not accessible", e);
		}
		return args -> {
			try {
				//noinspection unchecked
				return (C) handle.invokeWithArguments(args);
			} catch (ClassCastException | WrongMethodTypeException e) {
				throw new IllegalStateException(
						"Failed to construct type " + type.getName() + " as " + constructBaseClass.getName(), e
				);
			} catch (Throwable e) {
				throw new RuntimeException(
						"Uncaught exception during construct of type " + type.getName()
								+ " as " + constructBaseClass.getName(),
						e
				);
			}
		};
	}

	private String createConstructorTargetArgCheckFailMessage(
			Executable executable,
			Map<Class<?>, List<Parameter>> typedParameters,
			Map<String, List<Parameter>> namedParameters
	) {
		StringBuilder sb = new StringBuilder();
		sb.append("Failed to resolve parameters for ");
		sb.append(executable);
		sb.append(" (for ");
		sb.append(constructBaseClass.getName());
		sb.append("). The following issues have been detected:");

		Set<Class<?>> unexpectedTypes = new HashSet<>(typedParameters.keySet());
		unexpectedTypes.removeAll(typedArgs);
		if (!unexpectedTypes.isEmpty()) {
			for (Class<?> unexpectedType : unexpectedTypes) {
				sb.append("\n - Typed parameter of type ");
				sb.append(unexpectedType.getName());
				sb.append(" is not known: ");
				sb.append(typedParameters.get(unexpectedType));
			}
		}
		Set<String> unexpectedNames = new HashSet<>(namedParameters.keySet());
		unexpectedNames.removeAll(namedArgs.keySet());
		if (!unexpectedNames.isEmpty()) {
			for (String unexpectedName : unexpectedNames) {
				sb.append("\n - Named parameter ");
				sb.append(unexpectedName);
				sb.append(" is not known: ");
				sb.append(namedParameters.get(unexpectedName));
			}
		}

		typedParameters.entrySet().stream()
				.filter(entry -> entry.getValue().size() > 1)
				.forEach(entry -> sb.append("\n - Duplicate typed parameter ")
						.append(entry.getKey())
						.append(": ")
						.append(entry.getValue()));
		namedParameters.entrySet().stream()
				.filter(entry -> entry.getValue().size() > 1)
				.forEach(entry -> sb.append("\n - Duplicate named parameter ")
						.append(entry.getKey())
						.append(": ")
						.append(entry.getValue()));

		namedParameters.entrySet().stream()
				.filter(entry -> !unexpectedNames.contains(entry.getKey()))
				.flatMap(entry -> entry.getValue().stream().map(parameter -> new Entry<>(entry.getKey(), parameter)))
				.forEach(entry -> {
					Class<?> argType = namedArgs.get(entry.key());
					if (!boxClass(entry.value().getType()).isAssignableFrom(argType)) {
						sb.append("\n - Named parameter ").append(entry.key());
						sb.append(" expects values of type ").append(argType.getName());
						sb.append(": ").append(entry.value());
					}
				});

		return sb.toString();
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class FactoryBuilder<T> implements TweedConstructFactory.FactoryBuilder<T> {
		private final Class<T> constructBaseClass;
		private final Set<Class<?>> typedArgs = new HashSet<>();
		private final Map<String, Class<?>> namedArgs = new HashMap<>();

		@Override
		public <A> TweedConstructFactory.@NotNull FactoryBuilder<T> typedArg(@NotNull Class<A> argType) {
			argType = boxClass(argType);
			if (typedArgs.contains(argType)) {
				throw new IllegalArgumentException("Argument for type " + argType + " has already been registered");
			}
			typedArgs.add(argType);
			return this;
		}

		@Override
		public <A> TweedConstructFactory.@NotNull FactoryBuilder<T> namedArg(
				@NotNull String name,
				@NotNull Class<A> argType
		) {
			Class<?> existingArgType = namedArgs.get(name);
			if (existingArgType != null) {
				throw new IllegalArgumentException(
						"Argument for name " + name + " has already been registered; "
								+ "existing type " + existingArgType.getName() + "; "
								+ "new type " + argType.getName()
				);
			}
			namedArgs.put(name, boxClass(argType));
			return this;
		}

		@Override
		public @NotNull TweedConstructFactory<T> build() {
			return new TweedConstructFactoryImpl<>(
					constructBaseClass,
					typedArgs,
					namedArgs
			);
		}
	}

	@RequiredArgsConstructor
	private class Construct<C> implements TweedConstructFactory.Construct<C> {
		private final ConstructTarget<C> target;
		private final Map<Class<?>, Object> typedArgValues = new HashMap<>();
		private final Map<String, Object> namedArgValues = new HashMap<>();

		@Override
		public <A> TweedConstructFactory.@NotNull Construct<C> typedArg(@NotNull A value) {
			requireTypedArgExists(value.getClass(), value);
			typedArgValues.put(value.getClass(), value);
			return this;
		}

		@Override
		public <A> TweedConstructFactory.@NotNull Construct<C> typedArg(@NotNull Class<? super A> argType, @Nullable A value) {
			argType = boxClass(argType);
			if (value != null && !argType.isAssignableFrom(value.getClass())) {
				throw new IllegalArgumentException(
						"Typed argument for type " + argType.getName()
								+ " is of incorrect type " + value.getClass().getName()
								+ ", value: " + value
				);
			}
			requireTypedArgExists(argType, value);
			typedArgValues.put(argType, value);
			return this;
		}

		private <A> void requireTypedArgExists(@NotNull Class<?> type, @Nullable A value) {
			if (!typedArgs.contains(type)) {
				throw new IllegalArgumentException(
						"Typed argument for type " + type.getName() + " does not exist, value: " + value
				);
			}
		}

		@Override
		public <A> TweedConstructFactory.@NotNull Construct<C> namedArg(@NotNull String name, @Nullable A value) {
			Class<?> argType = namedArgs.get(name);
			if (argType == null) {
				throw new IllegalArgumentException(
						"Named argument for name " + name + " does not exist, value: " + value
				);
			} else if (value != null && !argType.isAssignableFrom(value.getClass())) {
				throw new IllegalArgumentException(
						"Named argument for name " + name + " is defined with type " + argType.getName() +
								" but got type " + value.getClass().getName() + " with value " + value
				);
			}
			namedArgValues.put(name, value);
			return this;
		}

		@Override
		public @NotNull C finish() {
			checkAllArgsFilled();

			Object[] argValues = new Object[target.argOrder.length];
			for (int i = 0; i < target.argOrder.length; i++) {
				Object arg = target.argOrder[i];
				if (arg instanceof Class<?>) {
					argValues[i] = typedArgValues.get((Class<?>) arg);
				} else if (arg instanceof String) {
					argValues[i] = namedArgValues.get((String) arg);
				} else {
					throw new IllegalStateException("Encountered illegal argument indicator " + arg + " at " + i);
				}
			}
			return target.invoker.apply(argValues);
		}

		private void checkAllArgsFilled() {
			Set<Class<?>> missingTypedArgs = Collections.emptySet();
			if (typedArgValues.size() != typedArgs.size()) {
				missingTypedArgs = new HashSet<>(typedArgs);
				missingTypedArgs.removeAll(typedArgValues.keySet());
			}
			Set<String> missingNamedArgs = Collections.emptySet();
			if (namedArgValues.size() != namedArgs.size()) {
				missingNamedArgs = new HashSet<>(namedArgs.keySet());
				missingNamedArgs.removeAll(namedArgValues.keySet());
			}

			if (!missingTypedArgs.isEmpty() || !missingNamedArgs.isEmpty()) {
				throw new IllegalArgumentException(createMissingArgsMessage(missingTypedArgs, missingNamedArgs));
			}
		}

		private String createMissingArgsMessage(Set<Class<?>> missingTypedArgs, Set<String> missingNamedArgs) {
			StringBuilder message = new StringBuilder()
					.append("Missing arguments for construction of ")
					.append(target.type().getName())
					.append(" as ")
					.append(constructBaseClass.getName())
					.append(", missing: ");

			if (!missingTypedArgs.isEmpty()) {
				message.append("typed args (");
				boolean requiresDelimiter = false;
				for (Class<?> missingTypedArg : missingTypedArgs) {
					if (requiresDelimiter) {
						message.append(", ");
					}
					message.append(missingTypedArg.getName());
					requiresDelimiter = true;
				}
				message.append(") ");
			}
			if (!missingNamedArgs.isEmpty()) {
				message.append("named args (");
				boolean requiresDelimiter = false;
				for (String missingNamedArg : missingNamedArgs) {
					if (requiresDelimiter) {
						message.append(", ");
					}
					message.append(missingNamedArg);
					requiresDelimiter = true;
				}
				message.append(") ");
			}
			return message.toString();
		}
	}

	/**
	 * Boxes primitive classes into their reference variants.
	 * Allows for easier class comparison down the line.
	 */
	@SuppressWarnings("unchecked")
	static <V> Class<V> boxClass(Class<V> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == boolean.class) {
			return (Class<V>) Boolean.class;
		} else if (type == byte.class) {
			return (Class<V>) Byte.class;
		} else if (type == char.class) {
			return (Class<V>) Character.class;
		} else if (type == short.class) {
			return (Class<V>) Short.class;
		} else if (type == int.class) {
			return (Class<V>) Integer.class;
		} else if (type == long.class) {
			return (Class<V>) Long.class;
		} else if (type == float.class) {
			return (Class<V>) Float.class;
		} else if (type == double.class) {
			return (Class<V>) Double.class;
		} else if (type == void.class) {
			return (Class<V>) Void.class;
		} else {
			throw new IllegalArgumentException("Unsupported primitive type " + type);
		}
	}

	@Value
	private static class ConstructTarget<C> {
		Class<?> type;
		Object[] argOrder;
		Function<Object[], C> invoker;
	}
}

package de.siphalor.tweed5.typeutils.api.type;

import de.siphalor.tweed5.typeutils.api.annotations.LayeredTypeAnnotations;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a runtime type with the parameters and annotations that are actually in use.
 *
 * @param <T> the type represented by this class
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ActualType<T> implements AnnotatedElement {
	/**
	 * The raw {@link Class} that the type has been originally declared as.
	 */
	@Getter
	private final Class<T> declaredType;
	/**
	 * The {@link AnnotatedType} that represents the type that is actually in use (without parameters).
	 */
	@Getter(AccessLevel.PROTECTED)
	private final @Nullable AnnotatedType usedType;
	/**
	 * The {@link AnnotatedParameterizedType} that represents the type that is actually in use with parameters.
	 */
	private final @Nullable AnnotatedParameterizedType usedParameterizedType;

	/**
	 * A representation of the layered annotations of this type.
	 * These usually consist of the annotations on {@link #declaredType()} combined with those from {@link #usedType()}
	 */
	private final LayeredTypeAnnotations layeredTypeAnnotations;

	/**
	 * Internal cache for the resolved actual type parameters.
	 */
	@Nullable
	private List<ActualType<?>> resolvedParameters;

	/**
	 * Creates a basic actual type from just a declared class.
	 */
	public static <T> ActualType<T> ofClass(Class<T> clazz) {
		return new ActualType<>(
				clazz,
				null,
				null,
				LayeredTypeAnnotations.of(TypeAnnotationLayer.TYPE_DECLARATION, clazz)
		);
	}

	/**
	 * Creates an actual type from a Java type usage.
	 *
	 * @throws UnsupportedOperationException when the given annotated type is not yet supported by this class
	 */
	public static ActualType<?> ofUsedType(AnnotatedType annotatedType) throws UnsupportedOperationException {
		Class<?> clazz = getDeclaredClassForUsedType(annotatedType);

		LayeredTypeAnnotations layeredTypeAnnotations = new LayeredTypeAnnotations();
		layeredTypeAnnotations.appendLayerFrom(TypeAnnotationLayer.TYPE_DECLARATION, clazz);
		layeredTypeAnnotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, annotatedType);

		if (annotatedType instanceof AnnotatedParameterizedType) {
			return new ActualType<>(clazz, annotatedType, (AnnotatedParameterizedType) annotatedType, layeredTypeAnnotations);
		} else {
			return new ActualType<>(clazz, annotatedType, null, layeredTypeAnnotations);
		}
	}

	/**
	 * Resolves the declared {@link Class} of the {@link AnnotatedType} as Java has no generic way to do that.
	 *
	 * @throws UnsupportedOperationException if the given parameter is not supported yet
	 */
	private static Class<?> getDeclaredClassForUsedType(AnnotatedType annotatedType) throws UnsupportedOperationException {
		if (annotatedType.getType() instanceof Class) {
			return (Class<?>) annotatedType.getType();
		} else if (annotatedType.getType() instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) annotatedType.getType()).getRawType();
		} else if (annotatedType instanceof AnnotatedWildcardType) {
			AnnotatedType[] upperBounds = ((AnnotatedWildcardType) annotatedType).getAnnotatedUpperBounds();
			if (upperBounds.length == 1) {
				return getDeclaredClassForUsedType(upperBounds[0]);
			}
			return Object.class;
		} else {
			throw new UnsupportedOperationException(
					"Failed to resolve raw class of annotated type: " + annotatedType + " (" + annotatedType.getClass() + ")"
			);
		}
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return layeredTypeAnnotations.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return layeredTypeAnnotations.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return layeredTypeAnnotations.getDeclaredAnnotations();
	}

	/**
	 * Resolves the type parameters of this type as {@link ActualType}s.
	 */
	public List<ActualType<?>> parameters() {
		if (resolvedParameters != null) {
			return resolvedParameters;
		} else if (usedParameterizedType == null) {
			int paramCount = declaredType.getTypeParameters().length;
			if (paramCount == 0) {
				resolvedParameters = Collections.emptyList();
			} else {
				resolvedParameters = new ArrayList<>(paramCount);
				for (int i = 0; i < paramCount; i++) {
					resolvedParameters.add(ActualType.ofClass(Object.class));
				}
			}
		} else {
			resolvedParameters = Arrays.stream(usedParameterizedType.getAnnotatedActualTypeArguments())
					.map(ActualType::ofUsedType)
					.collect(Collectors.toList());
		}
		return resolvedParameters;
	}

	/**
	 * Resolves the actual type parameters of a super-class or super-interface of this type.
	 * @param targetClass the class to check
	 * @return the list of type parameters if the given class is assignable from this type or {@code null} if not
	 */
	public @Nullable List<ActualType<?>> getTypesOfSuperArguments(Class<?> targetClass) {
		if (targetClass.getTypeParameters().length == 0) {
			if (targetClass.isAssignableFrom(declaredType)) {
				return Collections.emptyList();
			} else {
				return null;
			}
		}

		ActualType<?> superType = getViewOnSuperType(targetClass, this);
		if (superType == null) {
			return null;
		}
		return superType.parameters();
	}

	private static @Nullable ActualType<?> getViewOnSuperType(
			Class<?> targetClass,
			ActualType<?> currentType
	) {
		Class<?> currentClass = currentType.declaredType();
		if (currentClass == targetClass) {
			return currentType;
		}

		List<ActualType<?>> currentParameters = currentType.parameters();

		Map<String, AnnotatedType> paramMap;
		if (currentParameters.isEmpty()) {
			paramMap = Collections.emptyMap();
		} else {
			paramMap = new HashMap<>();
			for (int i = 0; i < currentParameters.size(); i++) {
				// used types are always known in resolved parameters
				//noinspection DataFlowIssue
				paramMap.put(currentClass.getTypeParameters()[i].getName(), currentParameters.get(i).usedType());
			}
		}

		if (targetClass.isInterface()) {
			for (AnnotatedType annotatedInterface : currentClass.getAnnotatedInterfaces()) {
				ActualType<?> interfaceType = resolveTypeWithParameters(annotatedInterface, paramMap);
				ActualType<?> resultType = getViewOnSuperType(targetClass, interfaceType);
				if (resultType != null) {
					return resultType;
				}
			}
		}
		if (currentClass != Object.class && !currentClass.isInterface()) {
			ActualType<?> superType = resolveTypeWithParameters(currentClass.getAnnotatedSuperclass(), paramMap);
			ActualType<?> resultType = getViewOnSuperType(targetClass, superType);
			if (resultType != null) {
				return resultType;
			}
		}
		return null;
	}

	private static ActualType<?> resolveTypeWithParameters(AnnotatedType annotatedType, Map<String, AnnotatedType> parameters) {
		if (annotatedType instanceof AnnotatedTypeVariable) {
			ActualType<?> actualType = ofUsedType(parameters.get(annotatedType.getType().getTypeName()));
			actualType.layeredTypeAnnotations.appendLayerFrom(TypeAnnotationLayer.TYPE_USE, annotatedType);
			return actualType;
		} else if (annotatedType instanceof AnnotatedParameterizedType) {
			List<ActualType<?>> resolvedParameters = Arrays.stream(((AnnotatedParameterizedType) annotatedType).getAnnotatedActualTypeArguments())
					.map(p -> resolveTypeWithParameters(p, parameters))
					.collect(Collectors.toList());
			ActualType<?> actualType = ofUsedType(annotatedType);
			actualType.resolvedParameters = resolvedParameters;
			return actualType;
		} else {
			return ofUsedType(annotatedType);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ActualType)) {
			return false;
		} else if (usedParameterizedType != null) {
			return usedParameterizedType.equals(((ActualType<?>) obj).usedParameterizedType);
		} else if (usedType != null) {
			return usedType.equals(((ActualType<?>) obj).usedType);
		} else {
			return declaredType.equals(((ActualType<?>) obj).declaredType);
		}
	}

	@Override
	public int hashCode() {
		return getMostSpecificTypeObject().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (usedType != null) {
			appendAnnotationsToString(sb, usedType.getAnnotations());
		}
		sb.append(declaredType.getName());
		List<ActualType<?>> parameters = parameters();
		if (!parameters.isEmpty()) {
			sb.append("<");
			for (ActualType<?> parameter : parameters) {
				sb.append(parameter);
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
			sb.append(">");
		}
		return sb.toString();
	}

	private void appendAnnotationsToString(StringBuilder sb, Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			sb.append(annotation);
			sb.append(' ');
		}
	}

	protected Object getMostSpecificTypeObject() {
		if (usedParameterizedType != null) {
			return usedParameterizedType;
		} else if (usedType != null) {
			return usedType;
		} else {
			return declaredType;
		}
	}
}

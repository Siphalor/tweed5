package de.siphalor.tweed5.weaver.pojo.impl.weaving;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PojoClassIntrospector {
	private final Class<?> clazz;
	private final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

	private Map<String, Property> properties;

	public static PojoClassIntrospector forClass(Class<?> clazz) {
		if ((clazz.getModifiers() & Modifier.PUBLIC) == 0) {
			throw new IllegalStateException("Class " + clazz.getName() + " must be public");
		}
		return new PojoClassIntrospector(clazz);
	}

	public Class<?> type() {
		return clazz;
	}

	public @Nullable MethodHandle noArgsConstructor() {
		try {
			return lookup.findConstructor(clazz, MethodType.methodType(void.class));
		} catch (NoSuchMethodException | IllegalAccessException | SecurityException e) {
			return null;
		}
	}

	public Map<String, Property> properties() {
		if (this.properties == null) {
			this.properties = new HashMap<>();
			Class<?> currentClass = clazz;
			while (currentClass != null) {
				appendClassProperties(currentClass);
				currentClass = currentClass.getSuperclass();
			}
		}

		return Collections.unmodifiableMap(this.properties);
	}

	private void appendClassProperties(Class<?> targetClass) {
		try {
			Field[] fields = targetClass.getDeclaredFields();
			for (Field field : fields) {
				if (shouldIgnoreField(field)) {
					continue;
				}

				if (!properties.containsKey(field.getName())) {
					Property property = introspectProperty(field);
					properties.put(property.field.getName(), property);
				} else {
					Property existingProperty = properties.get(field.getName());
					log.error(
							"Duplicate property \"{}\" detected in hierarchy of {} in classes: {} and {}",
							field.getName(),
							clazz.getName(),
							existingProperty.field().getDeclaringClass().getName(),
							targetClass.getName()
					);
				}
			}
		} catch (Exception e) {
			log.error(
					"Got unexpected error introspecting the properties of class {} (in hierarchy of {})",
					targetClass.getName(),
					clazz.getName(),
					e
			);
		}
	}

	private boolean shouldIgnoreField(Field field) {
		return (field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0;
	}

	private Property introspectProperty(Field field) {
		int modifiers = field.getModifiers();

		return Property.builder()
				.field(field)
				.isFinal((modifiers & Modifier.FINAL) != 0)
				.getter(findGetter(field))
				.setter(findSetter(field))
				.type(field.getGenericType())
				.build();
	}

	@Nullable
	private MethodHandle findGetter(Field field) {
		String fieldName = field.getName();
		// fluid getters
		MethodHandle method = findMethod(
				clazz,
				new MethodDescriptor(fieldName, MethodType.methodType(field.getType()))
		);
		if (method != null) {
			return method;
		}
		// boolean getters
		if (field.getType() == Boolean.class || field.getType() == Boolean.TYPE) {
			method = findMethod(
					clazz,
					new MethodDescriptor("is" + firstToUpper(fieldName), MethodType.methodType(field.getType()))
			);
			if (method != null) {
				return method;
			}
		}
		// classic getters
		method = findMethod(
				clazz,
				new MethodDescriptor("get" + firstToUpper(fieldName), MethodType.methodType(field.getType()))
		);
		if (method != null) {
			return method;
		}

		// public field access
		int modifiers = field.getModifiers();
		if ((modifiers & Modifier.PUBLIC) != 0) {
			return findFieldGetter(field);
		}
		return null;
	}

	@Nullable
	private MethodHandle findSetter(Field field) {
		String fieldName = field.getName();

		String classicSetterName = "set" + firstToUpper(fieldName);
		MethodHandle method = findFirstMethod(
				clazz,
				// fluid
				new MethodDescriptor(fieldName, MethodType.methodType(Void.TYPE, field.getType())),
				// fluid + chain
				new MethodDescriptor(fieldName, MethodType.methodType(field.getDeclaringClass(), field.getType())),
				// classic
				new MethodDescriptor(classicSetterName, MethodType.methodType(Void.TYPE, field.getType())),
				// classic + chain
				new MethodDescriptor(
						classicSetterName,
						MethodType.methodType(field.getDeclaringClass(), field.getType())
				)
		);
		if (method != null) {
			return method;
		}

		// public field access
		int modifiers = field.getModifiers();
		if ((modifiers & Modifier.PUBLIC) != 0) {
			return findFieldSetter(field);
		}
		return null;
	}

	@Nullable
	private MethodHandle findFirstMethod(Class<?> targetClass, MethodDescriptor... methodDescriptors) {
		for (MethodDescriptor methodDescriptor : methodDescriptors) {
			MethodHandle method = findMethod(targetClass, methodDescriptor);
			if (method != null) {
				return method;
			}
		}
		return null;
	}

	@Nullable
	private MethodHandle findMethod(Class<?> targetClass, MethodDescriptor methodDescriptor) {
		try {
			return lookup.findVirtual(targetClass, methodDescriptor.name(), methodDescriptor.methodType());
		} catch (NoSuchMethodException e) {
			return null;
		} catch (IllegalAccessException e) {
			log.warn(
					"Failed to access method \"{}\" of class {} in hierarchy of {}",
					methodDescriptor,
					targetClass.getName(),
					clazz.getName(),
					e
			);
			return null;
		}
	}

	@Nullable
	private MethodHandle findFieldGetter(Field field) {
		try {
			return lookup.findGetter(field.getDeclaringClass(), field.getName(), field.getType());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (IllegalAccessException e) {
			log.warn(
					"Failed to access getter for field \"{}\" of class {} in hierarchy of {}",
					field.getName(),
					field.getDeclaringClass().getName(),
					clazz.getName(),
					e
			);
			return null;
		}
	}

	@Nullable
	private MethodHandle findFieldSetter(Field field) {
		try {
			return lookup.findSetter(field.getDeclaringClass(), field.getName(), field.getType());
		} catch (NoSuchFieldException e) {
			return null;
		} catch (IllegalAccessException e) {
			log.warn(
					"Failed to access setter for field \"{}\" of class {} in hierarchy of {}",
					field.getName(),
					field.getDeclaringClass().getName(),
					clazz.getName(),
					e
			);
			return null;
		}
	}

	private static String firstToUpper(String text) {
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}

	@Value
	private static class MethodDescriptor {
		String name;
		MethodType methodType;
	}

	@Value
	@Builder
	public static class Property {
		Field field;
		boolean isFinal;
		Type type;
		@Nullable
		MethodHandle getter;
		@Nullable
		MethodHandle setter;
	}
}

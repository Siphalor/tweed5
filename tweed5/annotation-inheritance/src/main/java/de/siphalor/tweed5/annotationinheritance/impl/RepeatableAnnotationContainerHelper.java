package de.siphalor.tweed5.annotationinheritance.impl;

import de.siphalor.tweed5.typeutils.api.annotations.AnnotationRepeatType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.*;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RepeatableAnnotationContainerHelper {
	/**
	 * Class version to use for generation (Java 8)
	 */
	private static final int CLASS_VERSION = Opcodes.V1_8;
	private static final String GENERATED_PACKAGE = RepeatableAnnotationContainerHelper.class.getPackage().getName()
			+ ".generated";

	private static String generateUniqueIdentifier() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}

	private static final Map<Class<? extends Annotation>, Function<Annotation[], Annotation>> CACHE = new HashMap<>();
	private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();

	@SuppressWarnings("unchecked")
	public static <R extends Annotation, C extends Annotation> C createContainer(R[] elements) {
		if (elements.length == 0) {
			throw new IllegalArgumentException("elements must not be empty");
		}
		Class<R> repeatableClass = (Class<R>) elements[0].annotationType();
		AnnotationRepeatType repeatType = AnnotationRepeatType.getType(repeatableClass);
		if (!(repeatType instanceof AnnotationRepeatType.Repeatable)) {
			throw new IllegalArgumentException(repeatableClass.getName() + " is not a repeatable");
		}
		Class<? extends Annotation> containerClass
				= ((AnnotationRepeatType.Repeatable) repeatType).containerAnnotationClass();

		CACHE_LOCK.readLock().lock();
		try {
			Function<Annotation[], Annotation> constructor = CACHE.get(containerClass);
			if (constructor != null) {
				return (C) constructor.apply(elements);
			}
		} finally {
			CACHE_LOCK.readLock().unlock();
		}

		Function<R[], ? extends Annotation> constructor = createContainerClassConstructor(
				containerClass,
				repeatableClass
		);
		CACHE_LOCK.writeLock().lock();
		try {
			//noinspection rawtypes
			CACHE.put(containerClass, (Function) constructor);
		} finally {
			CACHE_LOCK.writeLock().unlock();
		}
		return (C) constructor.apply(elements);
	}

	@SuppressWarnings("unchecked")
	private static <R extends Annotation, C extends Annotation> Function<R[], C> createContainerClassConstructor(
			Class<C> containerClass,
			Class<R> repeatableClass
	) {
		try {
			if (!containerClass.isAnnotation()) {
				throw new IllegalArgumentException(containerClass.getName() + " is not a container annotation");
			}

			String generatedClassName = GENERATED_PACKAGE + ".RepeatableContainer$" + generateUniqueIdentifier();
			byte[] bytes = createContainerClassBytes(generatedClassName, containerClass, repeatableClass);
			Class<?>
					generatedClass
					= new ByteArrayClassLoader(RepeatableAnnotationContainerHelper.class.getClassLoader())
					.createClass(generatedClassName, bytes);

			MethodHandle constructorHandle = MethodHandles.lookup().findConstructor(
					generatedClass,
					MethodType.methodType(void.class, arrayType(repeatableClass))
			);

			return (repeatedValues) -> {
				try {
					return (C) constructorHandle.invoke((Object) repeatedValues);
				} catch (Throwable e) {
					throw new RuntimeException("Failed to instantiate generated container annotation", e);
				}
			};
		} catch (Exception e) {
			throw new IllegalStateException("Class generation failed", e);
		}
	}

	static <R extends Annotation, C extends Annotation> byte[] createContainerClassBytes(
			String generatedClassName,
			Class<C> containerClass,
			Class<R> repeatableClass
	) {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		String generatedClassNameInternal = generatedClassName.replace('.', '/');
		classWriter.visit(
				CLASS_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
				generatedClassNameInternal,
				null,
				"java/lang/Object",
				new String[]{containerClass.getName().replace('.', '/')}
		);
		Class<?> repeatableArrayClass = arrayType(repeatableClass);

		classWriter.visitField(Opcodes.ACC_PRIVATE, "values", descriptor(repeatableArrayClass), null, null);

		appendConstructor(classWriter, repeatableArrayClass, generatedClassNameInternal);
		appendValueMethod(classWriter, repeatableArrayClass, generatedClassNameInternal);
		appendEqualsMethod(classWriter, repeatableArrayClass, containerClass, generatedClassNameInternal);
		appendHashCodeMethod(classWriter, repeatableArrayClass, generatedClassNameInternal);
		appendToStringMethod(classWriter, repeatableArrayClass, containerClass, generatedClassNameInternal);
		appendAnnotationTypeMethod(classWriter, containerClass);

		classWriter.visitEnd();

		return classWriter.toByteArray();
	}

	private static void appendConstructor(
			ClassWriter classWriter,
			Class<?> repeatableArrayClass,
			String generatedClassNameInternal
	) {
		MethodVisitor methodWriter = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC,
				"<init>",
				"(" + descriptor(repeatableArrayClass) + ")V",
				null,
				null
		);
		methodWriter.visitParameter("values", 0);
		methodWriter.visitCode();
		methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
		methodWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
		methodWriter.visitVarInsn(Opcodes.ALOAD, 1);
		methodWriter.visitFieldInsn(
				Opcodes.PUTFIELD,
				generatedClassNameInternal,
				"values",
				descriptor(repeatableArrayClass)
		);
		methodWriter.visitInsn(Opcodes.RETURN);
		methodWriter.visitMaxs(2, 2);
		methodWriter.visitEnd();
	}

	private static void appendValueMethod(
			ClassWriter classWriter,
			Class<?> repeatableArrayClass,
			String generatedClassNameInternal
	) {
		MethodVisitor methodWriter = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC,
				"value",
				"()" + descriptor(repeatableArrayClass),
				null,
				null
		);
		methodWriter.visitCode();
		methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
		methodWriter.visitFieldInsn(
				Opcodes.GETFIELD,
				generatedClassNameInternal,
				"values",
				descriptor(repeatableArrayClass)
		);
		methodWriter.visitInsn(Opcodes.ARETURN);
		methodWriter.visitMaxs(1, 1);
		methodWriter.visitEnd();
	}

	private static void appendEqualsMethod(
			ClassWriter classWriter,
			Class<?> repeatableArrayClass,
			Class<?> containerAnnotationClass,
			String generatedClassNameInternal
	) {
		MethodVisitor methodWriter = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC,
				"equals",
				"(Ljava/lang/Object;)Z",
				null,
				null
		);
		methodWriter.visitParameter("other", 0);
		Label falseLabel = new Label();

		methodWriter.visitCode();
		methodWriter.visitVarInsn(Opcodes.ALOAD, 1);
		String containerAnnotationClassBinaryName = Type.getInternalName(containerAnnotationClass);
		methodWriter.visitTypeInsn(Opcodes.INSTANCEOF, containerAnnotationClassBinaryName);
		methodWriter.visitJumpInsn(Opcodes.IFEQ, falseLabel);

		methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
		methodWriter.visitFieldInsn(
				Opcodes.GETFIELD,
				generatedClassNameInternal,
				"values",
				descriptor(repeatableArrayClass)
		);
		methodWriter.visitVarInsn(Opcodes.ALOAD, 1);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEINTERFACE,
				containerAnnotationClassBinaryName,
				"value",
				"()" + descriptor(repeatableArrayClass),
				true
		);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				"java/util/Arrays",
				"equals",
				"([Ljava/lang/Object;[Ljava/lang/Object;)Z",
				false
		);
		methodWriter.visitInsn(Opcodes.IRETURN);

		methodWriter.visitLabel(falseLabel);
		methodWriter.visitLdcInsn(false);
		methodWriter.visitInsn(Opcodes.IRETURN);

		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}

	private static void appendHashCodeMethod(
			ClassWriter classWriter,
			Class<?> repeatableArrayClass,
			String generatedClassNameInternal
	) {
		MethodVisitor methodWriter = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC,
				"hashCode",
				"()I",
				null,
				null
		);

		final int keyHashCode = "value".hashCode() * 127;
		methodWriter.visitCode();
		methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
		methodWriter.visitFieldInsn(
				Opcodes.GETFIELD,
				generatedClassNameInternal,
				"values",
				descriptor(repeatableArrayClass)
		);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				"java/util/Arrays",
				"hashCode",
				"([Ljava/lang/Object;)I",
				false
		);
		methodWriter.visitLdcInsn(keyHashCode);
		methodWriter.visitInsn(Opcodes.IXOR);
		methodWriter.visitInsn(Opcodes.IRETURN);
		methodWriter.visitMaxs(2, 2);
		methodWriter.visitEnd();
	}

	private static void appendToStringMethod(
			ClassWriter classWriter,
			Class<?> repeatableArrayClass,
			Class<?> containerAnnotationClass,
			String generatedClassNameInternal
	) {
		MethodVisitor methodWriter = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC,
				"toString",
				"()Ljava/lang/String;",
				null,
				null
		);

		String prefix = "@" + containerAnnotationClass.getName() + "(value=";
		String suffix = ")";
		String stringBuilderBinaryName = StringBuilder.class.getName().replace('.', '/');
		methodWriter.visitCode();
		methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
		methodWriter.visitFieldInsn(
				Opcodes.GETFIELD,
				generatedClassNameInternal,
				"values",
				descriptor(repeatableArrayClass)
		);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				"java/util/Arrays",
				"toString",
				"([Ljava/lang/Object;)Ljava/lang/String;",
				false
		);
		methodWriter.visitInsn(Opcodes.DUP);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				"java/lang/String",
				"length",
				"()I",
				false
		);
		methodWriter.visitLdcInsn(prefix.length() + suffix.length());
		methodWriter.visitInsn(Opcodes.IADD);
		methodWriter.visitTypeInsn(Opcodes.NEW, stringBuilderBinaryName);
		methodWriter.visitInsn(Opcodes.DUP_X1); // S, I, SB -> S, SB, I, SB
		methodWriter.visitInsn(Opcodes.SWAP); // S, SB, I, SB -> S, SB, SB, I
		methodWriter.visitMethodInsn(
				Opcodes.INVOKESPECIAL,
				stringBuilderBinaryName,
				"<init>",
				"(I)V",
				false
		);
		methodWriter.visitLdcInsn(prefix);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				stringBuilderBinaryName,
				"append",
				"(Ljava/lang/CharSequence;)L" + stringBuilderBinaryName + ";",
				false
		);
		methodWriter.visitInsn(Opcodes.SWAP); // S, SB -> SB, S
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				stringBuilderBinaryName,
				"append",
				"(Ljava/lang/String;)L" + stringBuilderBinaryName + ";",
				false
		);
		methodWriter.visitLdcInsn(suffix);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				stringBuilderBinaryName,
				"append",
				"(Ljava/lang/CharSequence;)L" + stringBuilderBinaryName + ";",
				false
		);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				stringBuilderBinaryName,
				"toString",
				"()Ljava/lang/String;",
				false
		);
		methodWriter.visitInsn(Opcodes.ARETURN);
		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}

	private static void appendAnnotationTypeMethod(
			ClassWriter classWriter,
			Class<?> containerAnnotationClass
	) {
		MethodVisitor methodWriter = classWriter.visitMethod(
				Opcodes.ACC_PUBLIC,
				"annotationType",
				"()Ljava/lang/Class;",
				null,
				null
		);
		methodWriter.visitCode();
		methodWriter.visitLdcInsn(Type.getType(containerAnnotationClass));
		methodWriter.visitInsn(Opcodes.ARETURN);
		methodWriter.visitMaxs(1, 1);
		methodWriter.visitEnd();
	}

	private static String descriptor(Class<?> clazz) {
		return Type.getType(clazz).getDescriptor();
	}

	private static <T> Class<T[]> arrayType(Class<T> clazz) {
		try {
			//noinspection unchecked
			return (Class<T[]>) Class.forName("[L" + clazz.getName() + ";");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Failed to get array class for " + clazz.getName(), e);
		}
	}
}

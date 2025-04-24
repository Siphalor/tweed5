package de.siphalor.tweed5.patchwork.impl;

import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartIsNullException;
import de.siphalor.tweed5.patchwork.impl.util.StreamUtils;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class PatchworkClassGenerator {
	/**
	 * Class version to use (Java 8)
	 */
	private static final int CLASS_VERSION = Opcodes.V1_8;
	private static final String TARGET_PACKAGE = "de.siphalor.tweed5.core.generated.contextextensions";
	private static final List<Type> DEFAULT_PATHWORK_INTERFACES
			= Collections.singletonList(Type.getType(Patchwork.class));

	private static final String INNER_EQUALS_METHOD_NAME = "patchwork$innerEquals";

	private static String generateUniqueIdentifier() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}

	private final Config config;
	private final Collection<PatchworkClassPart> parts;
	private final String className;
	@Getter(AccessLevel.NONE)
	private final ClassWriter classWriter;

	public PatchworkClassGenerator(Config config, Collection<PatchworkClassPart> parts) {
		this.config = config;
		this.parts = parts;
		className = config.classPrefix() + generateUniqueIdentifier();
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	}

	public String internalClassName() {
		return config.classPackage().replace('.', '/') + "/" + className();
	}

	public String binaryClassName() {
		return config.classPackage() + "." + className();
	}

	public void verify() throws VerificationException {
		for (PatchworkClassPart part : parts) {
			verifyClass(part.partInterface());
		}
		verifyPartMethods();
	}

	private void verifyClass(Class<?> partClass) throws InvalidPatchworkPartClassException {
		if (!partClass.isInterface()) {
			throw new InvalidPatchworkPartClassException(partClass, "Must be an interface");
		}
		if ((partClass.getModifiers() & Modifier.PUBLIC) == 0) {
			throw new InvalidPatchworkPartClassException(partClass, "Interface must be public");
		}
	}

	private void verifyPartMethods() throws DuplicateMethodsException {
		Map<MethodDescriptor, Collection<Method>> methodsBySignature = new HashMap<>();

		for (PatchworkClassPart patchworkPart : parts) {
			for (Method method : patchworkPart.partInterface().getMethods()) {
				MethodDescriptor signature = new MethodDescriptor(method.getName(), method.getParameterTypes());

				methodsBySignature
						.computeIfAbsent(signature, s -> new ArrayList<>())
						.add(method);
			}
		}

		List<Method> duplicateMethods = methodsBySignature.entrySet().stream()
				.filter(entry -> entry.getValue().size() > 1)
				.flatMap(entry -> entry.getValue().stream())
				.collect(Collectors.toList());

		if (!duplicateMethods.isEmpty()) {
			throw new DuplicateMethodsException(duplicateMethods);
		}
	}

	public void generate() throws GenerationException {
		beginClass();
		generateSimpleConstructor();
		for (PatchworkClassPart extensionClass : parts) {
			addPart(extensionClass);
		}
		appendPojoMethods();
		appendDefaultPatchworkMethods();
		classWriter.visitEnd();
	}

	public byte[] emit() {
		return classWriter.toByteArray();
	}

	private void beginClass() {
		String[] interfaces = StreamUtils.concat(
				Stream.of(Type.getInternalName(Patchwork.class)),
				config.markerInterfaces().stream().map(Type::getInternalName),
				parts.stream().map(ext -> Type.getInternalName(ext.partInterface()))
		).toArray(String[]::new);

		classWriter.visit(
				CLASS_VERSION,
				Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL,
				internalClassName(),
				null,
				Type.getInternalName(Object.class),
				interfaces
		);
	}

	private void generateSimpleConstructor() {
		MethodVisitor methodWriter = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		methodWriter.visitCode();
		methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
		methodWriter.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		methodWriter.visitInsn(Opcodes.RETURN);
		methodWriter.visitMaxs(1, 1);
		methodWriter.visitEnd();
	}

	private void appendPojoMethods() {
		appendEqualsMethod();
		appendHashCodeMethod();
		appendToStringMethod();
	}

	// <editor-fold desc="POJO Methods">
	private void appendEqualsMethod() {
		appendInnerEqualsMethod();

		GeneratorAdapter methodWriter = createMethod(
				Opcodes.ACC_PUBLIC,
				"equals",
				"(Ljava/lang/Object;)Z",
				null,
				null
		);
		methodWriter.visitParameter("other", Opcodes.ACC_FINAL);
		methodWriter.visitCode();


		Label falseLabel = methodWriter.newLabel();
		Label continueLabel = methodWriter.newLabel();

		methodWriter.loadArg(0);
		methodWriter.loadThis();
		methodWriter.visitJumpInsn(Opcodes.IF_ACMPNE, continueLabel);
		methodWriter.push(true);
		methodWriter.returnValue();

		methodWriter.visitLabel(continueLabel);
		methodWriter.loadArg(0);
		methodWriter.visitTypeInsn(Opcodes.INSTANCEOF, internalClassName());
		methodWriter.visitJumpInsn(Opcodes.IFEQ, falseLabel);

		methodWriter.loadArg(0);
		methodWriter.visitTypeInsn(Opcodes.CHECKCAST, internalClassName());
		methodWriter.loadThis();
		methodWriter.visitMethodInsn(
				Opcodes.INVOKESPECIAL,
				internalClassName(),
				INNER_EQUALS_METHOD_NAME,
				"(L" + internalClassName() + ";)Z",
				false
		);
		methodWriter.visitJumpInsn(Opcodes.IFEQ, falseLabel);

		methodWriter.push(true);
		methodWriter.returnValue();

		methodWriter.visitLabel(falseLabel);
		methodWriter.push(false);
		methodWriter.returnValue();

		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}

	private void appendInnerEqualsMethod() {
		GeneratorAdapter methodWriter = createMethod(
				Opcodes.ACC_PRIVATE,
				INNER_EQUALS_METHOD_NAME,
				"(L" + internalClassName() + ";)Z",
				null,
				null
		);

		methodWriter.visitParameter("other", Opcodes.ACC_FINAL);
		methodWriter.visitCode();

		Label falseLabel = methodWriter.newLabel();
		for (PatchworkClassPart part : parts) {
			methodWriter.loadArg(0);
			visitFieldInsn(methodWriter, part, Opcodes.GETFIELD);
			methodWriter.loadThis();
			visitFieldInsn(methodWriter, part, Opcodes.GETFIELD);
			methodWriter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Objects.class),
					"equals",
					"(Ljava/lang/Object;Ljava/lang/Object;)Z",
					false
			);
			methodWriter.visitJumpInsn(Opcodes.IFEQ, falseLabel);
		}

		methodWriter.push(true);
		methodWriter.returnValue();

		methodWriter.visitLabel(falseLabel);
		methodWriter.push(false);
		methodWriter.returnValue();

		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}

	private void appendHashCodeMethod() {
		GeneratorAdapter methodWriter = createMethod(
				Opcodes.ACC_PUBLIC,
				"hashCode",
				"()I",
				null,
				null
		);

		methodWriter.visitCode();

		methodWriter.push(parts.size());
		methodWriter.newArray(Type.getType(Object.class));

		int i = 0;
		for (PatchworkClassPart part : parts) {
			methodWriter.dup();
			methodWriter.push(i);

			methodWriter.loadThis();
			visitFieldInsn(methodWriter, part, Opcodes.GETFIELD);

			methodWriter.visitInsn(Opcodes.AASTORE);

			i++;
		}

		methodWriter.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				"java/util/Objects",
				"hash",
				"([Ljava/lang/Object;)I",
				false
		);

		methodWriter.returnValue();

		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}

	private void appendToStringMethod() {
		GeneratorAdapter methodWriter = createMethod(
				Opcodes.ACC_PUBLIC,
				"toString",
				"()Ljava/lang/String;",
				null,
				null
		);

		methodWriter.visitCode();

		String stringBuilderType = Type.getInternalName(StringBuilder.class);
		methodWriter.visitTypeInsn(Opcodes.NEW, stringBuilderType);
		methodWriter.dup();
		methodWriter.push(className().length() + 10 + parts.size() * 64);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKESPECIAL,
				stringBuilderType,
				"<init>",
				"(I)V",
				false
		);

		StringBuilder constantConcat = new StringBuilder();
		constantConcat.append(className()).append("{\n");

		for (PatchworkClassPart part : parts) {
			constantConcat.append("\t").append(part.partInterface().getSimpleName()).append(": ");
			methodWriter.push(constantConcat.toString());
			constantConcat.setLength(0);
			visitStringBuilderAppendString(methodWriter);

			Label nullLabel = methodWriter.newLabel();
			Label continueLabel = methodWriter.newLabel();
			methodWriter.loadThis();
			visitFieldInsn(methodWriter, part, Opcodes.GETFIELD);
			methodWriter.dup();
			methodWriter.visitJumpInsn(Opcodes.IFNULL, nullLabel);

			visitToString(methodWriter);
			methodWriter.visitJumpInsn(Opcodes.GOTO, continueLabel);

			methodWriter.visitLabel(nullLabel);
			methodWriter.pop();
			methodWriter.push("<unset>");

			methodWriter.visitLabel(continueLabel);
			visitStringBuilderAppendString(methodWriter);

			constantConcat.append(",\n");
		}
		constantConcat.append("}");
		methodWriter.push(constantConcat.toString());
		visitStringBuilderAppendString(methodWriter);
		visitToString(methodWriter);
		methodWriter.returnValue();

		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}
	// </editor-fold>

	private void appendDefaultPatchworkMethods() {
		appendCopyMethod();
		appendIsPatchworkPartDefinedMethod();
		appendIsPatchworkPartSetMethod();
	}

	// <editor-fold desc="Patchwork Methods">
	private void appendCopyMethod() {
		GeneratorAdapter methodWriter = createMethod(
				Opcodes.ACC_PUBLIC,
				"copy",
				"()L" + Type.getInternalName(Patchwork.class) + ";",
				null,
				null
		);
		methodWriter.visitCode();
		methodWriter.visitTypeInsn(Opcodes.NEW, internalClassName());
		methodWriter.dup();
		methodWriter.visitMethodInsn(
				Opcodes.INVOKESPECIAL,
				internalClassName(),
				"<init>",
				"()V",
				false
		);
		for (PatchworkClassPart part : parts) {
			methodWriter.dup();
			methodWriter.loadThis();
			visitFieldInsn(methodWriter, part, Opcodes.GETFIELD);
			visitFieldInsn(methodWriter, part, Opcodes.PUTFIELD);
		}
		methodWriter.returnValue();
		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}

	private void appendIsPatchworkPartDefinedMethod() {
		GeneratorAdapter methodWriter = createMethod(
				Opcodes.ACC_PUBLIC,
				"isPatchworkPartDefined",
				"(Ljava/lang/Class;)Z",
				null,
				null
		);
		methodWriter.visitParameter(null, Opcodes.ACC_FINAL);
		methodWriter.visitCode();
		Label trueLabel = methodWriter.newLabel();
		for (PatchworkClassPart part : parts) {
			methodWriter.loadArg(0);
			methodWriter.push(Type.getType(part.partInterface()));
			methodWriter.ifCmp(Type.getType(Object.class), GeneratorAdapter.EQ, trueLabel);
		}
		methodWriter.push(false);
		methodWriter.returnValue();
		methodWriter.visitLabel(trueLabel);
		methodWriter.push(true);
		methodWriter.returnValue();
		methodWriter.visitMaxs(0, 0);
		methodWriter.visitEnd();
	}

	private void appendIsPatchworkPartSetMethod() {
		GeneratorAdapter methodWriter = createMethod(
				Opcodes.ACC_PUBLIC,
				"isPatchworkPartSet",
				"(Ljava/lang/Class;)Z",
				null,
				null
		);
		methodWriter.visitParameter(null, Opcodes.ACC_FINAL);
		methodWriter.visitCode();
		Label[] labels = new Label[parts.size()];
		int i = 0;
		for (PatchworkClassPart part : parts) {
			labels[i] = methodWriter.newLabel();
			methodWriter.loadArg(0);
			methodWriter.push(Type.getType(part.partInterface()));
			methodWriter.ifCmp(Type.getType(Object.class), GeneratorAdapter.EQ, labels[i]);
			i++;
		}
		methodWriter.push(false);
		methodWriter.returnValue();

		Label falseLabel = methodWriter.newLabel();
		i = 0;
		for (PatchworkClassPart part : parts) {
			methodWriter.visitLabel(labels[i]);
			methodWriter.loadThis();
			visitFieldInsn(methodWriter, part, Opcodes.GETFIELD);
			methodWriter.push((String) null);
			methodWriter.ifCmp(Type.getType(part.partInterface()), GeneratorAdapter.EQ, falseLabel);
			methodWriter.push(true);
			methodWriter.returnValue();
			i++;
		}
		methodWriter.visitLabel(falseLabel);
		methodWriter.push(false);
		methodWriter.returnValue();
		methodWriter.visitMaxs(1, 1);
		methodWriter.visitEnd();
	}

	public void addPart(PatchworkClassPart patchworkPart) throws GenerationException {
		patchworkPart.fieldName("f_" + generateUniqueIdentifier());

		classWriter.visitField(
				Opcodes.ACC_PUBLIC,
				patchworkPart.fieldName(),
				patchworkPart.partInterface().descriptorString(),
				null,
				null
		);

		appendPartMethods(patchworkPart);
	}

	private void appendPartMethods(PatchworkClassPart patchworkPart) throws GenerationException {
		try {
			ClassReader classReader = new ClassReader(patchworkPart.partInterface().getName());
			classReader.accept(new PartClassVisitor(patchworkPart), ClassReader.SKIP_FRAMES);
		} catch (IOException e) {
			throw new GenerationException("Failed to read interface class file", e);
		}
	}
	// </editor-fold>

	private GeneratorAdapter createMethod(
			int access,
			String name,
			String desc,
			@Nullable String signature,
			String @Nullable [] exceptions
	) {
		MethodVisitor methodVisitor = classWriter.visitMethod(access, name, desc, signature, exceptions);
		return new GeneratorAdapter(methodVisitor, access, name, desc);
	}

	private void visitFieldInsn(MethodVisitor methodWriter, PatchworkClassPart part, int opcode) {
		methodWriter.visitFieldInsn(
				opcode,
				internalClassName(),
				part.fieldName(),
				Type.getDescriptor(part.partInterface())
		);
	}

	private static void visitToString(MethodVisitor methodWriter) {
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				Type.getInternalName(Object.class),
				"toString",
				"()Ljava/lang/String;",
				false
		);
	}

	private static void visitStringBuilderAppendString(MethodVisitor methodWriter) {
		String stringBuilderType = Type.getInternalName(StringBuilder.class);
		methodWriter.visitMethodInsn(
				Opcodes.INVOKEVIRTUAL,
				stringBuilderType,
				"append",
				"(Ljava/lang/String;)L" + stringBuilderType + ";",
				false
		);
	}

	private class PartClassVisitor extends ClassVisitor {
		private final PatchworkClassPart extensionClass;

		protected PartClassVisitor(PatchworkClassPart extensionClass) {
			super(Opcodes.ASM9);
			this.extensionClass = extensionClass;
		}

		@Override
		public MethodVisitor visitMethod(
				int access,
				String name,
				String descriptor,
				String signature,
				String[] exceptions
		) {
			GeneratorAdapter methodWriter = createMethod(Opcodes.ACC_PUBLIC, name, descriptor, signature, exceptions);
			return new PartMethodVisitor(api, methodWriter, descriptor, extensionClass);
		}
	}

	private class PartMethodVisitor extends MethodVisitor {
		private final GeneratorAdapter methodWriter;
		private final String methodDescriptor;
		private final PatchworkClassPart patchworkPart;

		protected PartMethodVisitor(
				int api,
				GeneratorAdapter methodWriter,
				String methodDescriptor,
				PatchworkClassPart patchworkPart
		) {
			super(api);
			this.methodWriter = methodWriter;
			this.patchworkPart = patchworkPart;
			this.methodDescriptor = methodDescriptor;
		}

		@Override
		public void visitParameter(String name, int access) {
			methodWriter.visitParameter(name, access);
		}

		@Override
		public void visitEnd() {
			Label nullLabel = methodWriter.newLabel();

			methodWriter.visitCode();
			methodWriter.visitVarInsn(Opcodes.ALOAD, 0);
			methodWriter.visitFieldInsn(
					Opcodes.GETFIELD,
					internalClassName(),
					patchworkPart.fieldName(),
					Type.getDescriptor(patchworkPart.partInterface())
			);
			methodWriter.dup();
			methodWriter.ifNull(nullLabel);
			methodWriter.loadArgs();
			methodWriter.visitMethodInsn(
					Opcodes.INVOKEINTERFACE,
					Type.getInternalName(patchworkPart.partInterface()),
					methodWriter.getName(),
					methodDescriptor,
					true
			);
			methodWriter.returnValue();
			methodWriter.visitLabel(nullLabel);
			methodWriter.pop();
			methodWriter.throwException(
					Type.getType(PatchworkPartIsNullException.class),
					"The patchwork part " + patchworkPart.partInterface().getSimpleName() + " has not been set"
			);

			methodWriter.visitMaxs(-1, -1);
			methodWriter.visitEnd();
		}
	}

	@Value
	private static class MethodDescriptor {
		String name;
		Class<?>[] parameterTypes;
	}

	@Data
	public static class Config {
		@lombok.NonNull
		private @NonNull String classPackage;
		private String classPrefix = "";
		private Collection<Class<?>> markerInterfaces = Collections.emptyList();
	}

	public static class VerificationException extends Exception {
		private VerificationException(String message) {
			super(message);
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	public static class InvalidPatchworkPartClassException extends VerificationException {
		Class<?> partClass;

		public InvalidPatchworkPartClassException(Class<?> partClass, String message) {
			super("Invalid patchwork part class " + partClass.getName() + ": " + message);
			this.partClass = partClass;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = true)
	public static class DuplicateMethodsException extends VerificationException {
		transient Collection<Method> signatures;

		private DuplicateMethodsException(Collection<Method> methods) {
			super("Duplicate method signatures:\n" + methods.stream()
					.map(DuplicateMethodsException::getMethodMessage)
					.collect(Collectors.joining("\n")));
			this.signatures = methods;
		}

		private static String getMethodMessage(Method method) {
			StringBuilder stringBuilder = new StringBuilder("\t- "
					+ method.getDeclaringClass().getCanonicalName()
					+ "#(");
			for (Class<?> parameterType : method.getParameterTypes()) {
				stringBuilder.append(parameterType.getCanonicalName());
				stringBuilder.append(", ");
			}
			stringBuilder.append(")");
			stringBuilder.append(method.getReturnType().getCanonicalName());
			stringBuilder.append("\n");
			return stringBuilder.toString();
		}
	}

	public static class GenerationException extends Exception {
		public GenerationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}

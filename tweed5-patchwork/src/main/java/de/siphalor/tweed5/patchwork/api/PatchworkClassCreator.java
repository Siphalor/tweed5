package de.siphalor.tweed5.patchwork.api;

import de.siphalor.tweed5.patchwork.impl.ByteArrayClassLoader;
import de.siphalor.tweed5.patchwork.impl.PatchworkClass;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassGenerator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassPart;
import lombok.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class PatchworkClassCreator<P extends Patchwork<P>> {
	@NonNull
	Class<P> patchworkInterface;
	@NonNull
	PatchworkClassGenerator.Config generatorConfig;

	public static <P extends Patchwork<P>> Builder<P> builder() {
		return new Builder<>();
	}

	public PatchworkClass<P> createClass(Collection<Class<?>> partInterfaces) throws PatchworkClassGenerator.GenerationException {
		List<PatchworkClassPart> parts = partInterfaces.stream().map(PatchworkClassPart::new).collect(Collectors.toList());

		PatchworkClassGenerator generator = new PatchworkClassGenerator(generatorConfig, parts);
		try {
			generator.verify();
		} catch (PatchworkClassGenerator.VerificationException e) {
			throw new IllegalArgumentException(e);
		}
		generator.generate();
		byte[] classBytes = generator.emit();
		//noinspection unchecked
		Class<P> patchworkClass = (Class<P>) ByteArrayClassLoader.loadClass(null, classBytes);

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		for (PatchworkClassPart part : parts) {
			try {
				MethodHandle setterHandle = lookup.findSetter(patchworkClass, part.fieldName(), part.partInterface());
				part.fieldSetter(setterHandle);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new IllegalStateException("Failed to access setter for patchwork part " + part.partInterface().getName(), e);
			}
		}
		try {
			MethodHandle constructorHandle = lookup.findConstructor(patchworkClass, MethodType.methodType(Void.TYPE));

			return PatchworkClass.<P>builder()
					.classPackage(generatorConfig.classPackage())
					.className(generator.className())
					.theClass(patchworkClass)
					.constructor(constructorHandle)
					.parts(parts)
					.build();

		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to access constructor of patchwork class", e);
		}
	}

	@Setter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder<P extends Patchwork<P>> {
		@NonNull
		private Class<P> patchworkInterface;
		@NonNull
		private String classPackage;
		private String classPrefix = "";

		public PatchworkClassCreator<P> build() {
			return new PatchworkClassCreator<>(
					patchworkInterface,
					new PatchworkClassGenerator.Config(classPackage)
							.classPrefix(classPrefix)
							.markerInterfaces(Collections.singletonList(patchworkInterface))
			);
		}
	}
}

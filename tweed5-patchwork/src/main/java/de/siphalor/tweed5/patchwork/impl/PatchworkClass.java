package de.siphalor.tweed5.patchwork.impl;

import de.siphalor.tweed5.patchwork.api.Patchwork;
import lombok.Builder;
import lombok.Value;

import java.lang.invoke.MethodHandle;
import java.util.Collection;

@Value
@Builder
public class PatchworkClass<P extends Patchwork<P>> {
	String classPackage;
	String className;
	Class<P> theClass;
	MethodHandle constructor;
	Collection<PatchworkClassPart> parts;
}

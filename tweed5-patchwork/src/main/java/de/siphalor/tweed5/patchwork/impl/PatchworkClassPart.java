package de.siphalor.tweed5.patchwork.impl;

import lombok.Data;

import java.lang.invoke.MethodHandle;

@Data
public class PatchworkClassPart {
	private final Class<?> partInterface;
	private String fieldName;
	private MethodHandle fieldSetter;
}
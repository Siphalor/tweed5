package de.siphalor.tweed5.patchwork.impl;


import org.jspecify.annotations.Nullable;

public class ByteArrayClassLoader extends ClassLoader {
	public static Class<?> loadClass(@Nullable String binaryClassName, byte[] byteCode) {
		return new ByteArrayClassLoader(ByteArrayClassLoader.class.getClassLoader())
				.createClass(binaryClassName, byteCode);
	}

	private ByteArrayClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> createClass(@Nullable String binaryClassName, byte[] byteCode) {
		Class<?> clazz = defineClass(binaryClassName, byteCode, 0, byteCode.length);
		resolveClass(clazz);
		return clazz;
	}
}

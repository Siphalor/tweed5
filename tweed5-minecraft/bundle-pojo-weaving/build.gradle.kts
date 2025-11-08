plugins {
	id("de.siphalor.tweed5.minecraft.mod.bundle")
}

dependencies {
	minecraftJij("de.siphalor.tweed5:tweed5-annotation-inheritance:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-naming-format:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-type-utils:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-attributes-extension:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-presets-extension:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-serde-extension:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-validation-extension:${project.version}")
}

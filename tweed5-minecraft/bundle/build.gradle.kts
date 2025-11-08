plugins {
	id("de.siphalor.tweed5.minecraft.mod.bundle")
}

dependencies {
	minecraftJij("de.siphalor.tweed5:tweed5-attributes-extension:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-construct:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-core:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-default-extensions:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-patchwork:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-serde-api:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-serde-extension:${project.version}")
	minecraftJij("de.siphalor.tweed5:tweed5-utils:${project.version}")
	minecraftJij(project(":tweed5-logging"))
}

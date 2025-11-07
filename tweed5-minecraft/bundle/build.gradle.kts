plugins {
	id("de.siphalor.tweed5.minecraft.mod.bundle")
}

dependencies {
	minecraftJij("de.siphalor.tweed5:tweed5-core")
	minecraftJij("de.siphalor.tweed5:tweed5-attributes-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-default-extensions")
	minecraftJij("de.siphalor.tweed5:tweed5-serde-extension")
	minecraftJij(project(":tweed5-logging"))
}

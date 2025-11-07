plugins {
	id("de.siphalor.tweed5.minecraft.mod.bundle")
}

configurations.minecraftJijElements {
	isTransitive = false
}

dependencies {
	minecraftJij("de.siphalor.tweed5:tweed5-annotation-inheritance")
	minecraftJij("de.siphalor.tweed5:tweed5-naming-format")
	minecraftJij("de.siphalor.tweed5:tweed5-type-utils")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-attributes-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-presets-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-serde-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-validation-extension")
}

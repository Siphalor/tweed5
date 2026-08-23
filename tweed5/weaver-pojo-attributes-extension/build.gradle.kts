plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Attributes Extension for Tweed5 Weaver POJO"
    description = "Adds support for declaring generic attributes using annotations to the Tweed5 Weaver POJO."
}

dependencies {
	api(project(":tweed5-weaver-pojo"))
	api(project(":tweed5-attributes-extension"))

	testImplementation(project(":tweed5-default-extensions"))
	testImplementation(project(":tweed5-serde-hjson"))
}

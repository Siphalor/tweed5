plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

dependencies {
	api(project(":tweed5-weaver-pojo"))
	api(project(":tweed5-attributes-extension"))

	testImplementation(project(":tweed5-default-extensions"))
	testImplementation(project(":tweed5-serde-hjson"))
}

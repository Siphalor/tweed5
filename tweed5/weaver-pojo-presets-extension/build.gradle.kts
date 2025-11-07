plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

dependencies {
	api(project(":tweed5-construct"))
	api(project(":tweed5-default-extensions"))
	api(project(":tweed5-weaver-pojo"))
}

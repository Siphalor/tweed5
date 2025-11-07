plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

dependencies {
	implementation(project(":tweed5-core"))
	implementation(project(":tweed5-default-extensions"))

	testImplementation(project(":tweed5-serde-gson"))
}

plugins {
	id("de.siphalor.tweed5.base-module")
}

dependencies {
	implementation(project(":tweed5-utils"))
	implementation(project(":tweed5-type-utils"))
	implementation(libs.asm.core)
}

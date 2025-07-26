plugins {
	id("de.siphalor.tweed5.base-module")
}

dependencies {
	implementation(project(":tweed5-core"))
	compileOnly(project(":tweed5-serde-extension"))
	testImplementation(project(":tweed5-serde-extension"))
	testImplementation(project(":tweed5-serde-hjson"))
}

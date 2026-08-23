plugins {
    id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Jackson"
    description = "Tweed 5 module that adds support for reading and writing JSON files using the Jackson library."
}

dependencies {
    implementation(project(":tweed5-serde-api"))
	implementation(libs.jackson.core)
	shadowOnly(libs.jackson.core)

	testImplementation(project(":serde-json-test-utils"))
}

tasks.shadowJar {
	relocate("com.fasterxml.jackson.core", "de.siphalor.tweed5.data.jackson.shadowed.jackson.core")
}

plugins {
    id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Hjson"
    description = "Tweed 5 module that adds support for reading and writing Hjson files."
}

dependencies {
    api(project(":tweed5-serde-api"))

	testImplementation(project(":serde-json-test-utils"))
}

plugins {
    id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Gson"
    description = "Tweed 5 module that adds support for reading and writing JSON using the Gson library."
}

dependencies {
    implementation(project(":tweed5-serde-api"))
	api(libs.gson)

	testImplementation(project(":serde-json-test-utils"))
}


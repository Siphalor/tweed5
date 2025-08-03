plugins {
    id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

dependencies {
    api(project(":tweed5-serde-api"))

	testImplementation(project(":serde-json-test-utils"))
}

tasks.shadowJar {
	configurations = setOf()
}

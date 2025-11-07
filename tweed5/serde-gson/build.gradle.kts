plugins {
    id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

dependencies {
    implementation(project(":tweed5-serde-api"))
	api(libs.gson)

	testImplementation(project(":serde-json-test-utils"))
}


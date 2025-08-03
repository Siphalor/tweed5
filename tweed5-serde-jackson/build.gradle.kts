plugins {
    id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
	id("de.siphalor.tweed5.shadow.explicit")
}

dependencies {
    implementation(project(":tweed5-serde-api"))
	implementation(libs.jackson.core)
	shadowOnly(libs.jackson.core)
}

tasks.shadowJar {
	relocate("com.fasterxml.jackson.core", "de.siphalor.tweed5.data.jackson.shadowed.jackson.core")
}

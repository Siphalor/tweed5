import dev.panuszewski.gradle.pluginMarker

plugins {
    `kotlin-dsl`
}

group = "de.siphalor.tweed5"

dependencies {
	implementation(project(":tweed5-build-logic-helpers"))
	implementation(pluginMarker(libs.plugins.lombok))
	implementation(pluginMarker(libs.plugins.shadow))
}

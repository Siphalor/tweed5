import dev.panuszewski.gradle.pluginMarker

plugins {
    `kotlin-dsl`
}

group = "de.siphalor.tweed5"

dependencies {
	implementation(project(":helpers"))
	implementation(pluginMarker(libs.plugins.lombok))
	implementation(pluginMarker(libs.plugins.shadow))
}

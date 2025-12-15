import dev.panuszewski.gradle.pluginMarker

plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("de.siphalor.tweed5:tweed5-conventions")
	implementation("de.siphalor.tweed5:tweed5-conventions-helpers")
	implementation(pluginMarker(mcCommonLibs.plugins.fabric.loom))
	implementation(pluginMarker(mcCommonLibs.plugins.jcyo))
	implementation(pluginMarker(libs.plugins.lombok))
	implementation(pluginMarker(libs.plugins.shadow))
}

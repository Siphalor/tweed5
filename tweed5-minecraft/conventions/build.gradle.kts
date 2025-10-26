import dev.panuszewski.gradle.pluginMarker

plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("de.siphalor.tweed5:tweed5-conventions")
	implementation(pluginMarker(mcCommonLibs.plugins.fabric.loom))
	implementation(pluginMarker(mcCommonLibs.plugins.jcyo))
}

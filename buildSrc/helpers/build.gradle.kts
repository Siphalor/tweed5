plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}

repositories {
	gradlePluginPortal()
}

gradlePlugin {
	plugins.register("minecraftModComponent") {
		id = "de.siphalor.tweed5.minecraft.mod.component"
		implementationClass = "de.siphalor.tweed5.gradle.plugin.minecraft.mod.MinecraftModComponentPlugin"
	}
}

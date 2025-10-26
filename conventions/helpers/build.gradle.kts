plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
}

group = "de.siphalor.tweed5"

gradlePlugin {
	plugins.register("minecraftModComponent") {
		id = "de.siphalor.tweed5.minecraft.mod.component"
		implementationClass = "de.siphalor.tweed5.gradle.plugin.minecraft.mod.MinecraftModComponentPlugin"
	}
}

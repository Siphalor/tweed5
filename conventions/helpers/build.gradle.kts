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
	plugins.register("moduleInfo") {
		id = "de.siphalor.tweed5.module-info"
		implementationClass = "de.siphalor.tweed5.gradle.plugin.module_info.ModuleInfoPlugin"
	}
	plugins.register("rootProperties") {
		id = "de.siphalor.tweed5.root-properties"
		implementationClass = "de.siphalor.tweed5.gradle.plugin.root_properties.RootPropertiesPlugin"
	}
}

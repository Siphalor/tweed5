import de.siphalor.tweed5.gradle.plugin.minecraft.mod.MinecraftModded

plugins {
	java
	id("de.siphalor.tweed5.publishing")
	id("de.siphalor.tweed5.minecraft.mod.base")
}

val minecraftJij = configurations.dependencyScope("minecraftJij")
val minecraftJijElements = configurations.named("minecraftJijElements") {
	attributes {
		attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
		attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
		attribute(MinecraftModded.MINECRAFT_MODDED_ATTRIBUTE, objects.named(MinecraftModded.MODDED))
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
		attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
		attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
	}
	extendsFrom(minecraftJij.get())
}
configurations.runtimeElements {
	extendsFrom(minecraftJijElements.get())
}
configurations.apiElements {
	extendsFrom(minecraftJijElements.get())
}

tasks.named<Jar>("jar") {
	dependsOn(tasks.named("processMinecraftModResources"))
	dependsOn(minecraftJijElements)
	from(project.layout.buildDirectory.dir("minecraftModResources"))
	from(minecraftJijElements) {
		into("META-INF/jars")
	}
}

publishing {
	publications {
		create<MavenPublication>("main") {
			artifactId = project.name
			version = project.version.toString()

			from(components["java"])
		}
	}
}

plugins {
	`maven-publish`
	alias(libs.plugins.shadow)
	java
	`java-library`
	id("de.siphalor.tweed5.minecraft.mod.component")
}

tasks.shadowJar {
	relocate("org.apache.commons", "de.siphalor.tweed5.shadowed.org.apache.commons")
}

val minecraftModJar = tasks.register<Jar>("minecraftModJar") {
	group = LifecycleBasePlugin.BUILD_GROUP

	from(zipTree(tasks.shadowJar.get().archiveFile))

	destinationDirectory.set(layout.buildDirectory.dir("minecraftModLibs"))
}
tasks.assemble {
	dependsOn(minecraftModJar)
}

val minecraftModSourcesJar = tasks.register<Jar>("minecraftModSourcesJar") {
	group = LifecycleBasePlugin.BUILD_GROUP

	from(zipTree(tasks.named<Jar>("sourcesJar").get().archiveFile))

	archiveClassifier = "sources"
	destinationDirectory.set(layout.buildDirectory.dir("minecraftModLibs"))
}

artifacts.add("minecraftModElements", minecraftModJar)
artifacts.add("minecraftModSourcesElements", minecraftModSourcesJar)

publishing {
	publications {
		create<MavenPublication>("minecraftMod") {
			groupId = "${project.group}.minecraft"
			artifactId = project.name
			version = project.version.toString()

			from(components["minecraftMod"])
		}
	}
}


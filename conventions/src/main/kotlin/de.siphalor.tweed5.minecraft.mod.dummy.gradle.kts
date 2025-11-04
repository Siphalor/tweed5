plugins {
	`maven-publish`
	id("de.siphalor.tweed5.publishing")
	id("de.siphalor.tweed5.minecraft.mod.base")
}

val minecraftModJar = tasks.register<Jar>("minecraftModJar") {
	group = LifecycleBasePlugin.BUILD_GROUP

	dependsOn(tasks.shadowJar)
	dependsOn(tasks.named("processMinecraftModResources"))

	from(zipTree(tasks.shadowJar.get().archiveFile))
	from(project.layout.buildDirectory.dir("minecraftModResources"))

	destinationDirectory.set(layout.buildDirectory.dir("minecraftModLibs"))
}
tasks.assemble {
	dependsOn(minecraftModJar)
}

val minecraftModSourcesJar = tasks.register<Jar>("minecraftModSourcesJar") {
	group = LifecycleBasePlugin.BUILD_GROUP

	dependsOn(tasks.named("sourcesJar"))
	dependsOn(tasks.named("processMinecraftModResources"))

	from(tasks.named<Jar>("sourcesJar").get().archiveFile.map {
		if (it.asFile.exists()) {
			zipTree(it)
		} else {
			files()
		}
	})
	from(project.layout.buildDirectory.dir("minecraftModResources"))

	archiveClassifier = "sources"
	destinationDirectory.set(layout.buildDirectory.dir("minecraftModLibs"))
}

artifacts.add("minecraftModElements", minecraftModJar)
artifacts.add("minecraftModApiElements", minecraftModJar)
artifacts.add("minecraftModSourcesElements", minecraftModSourcesJar)

publishing {
	publications {
		create<MavenPublication>("minecraftMod") {
			val projectGroup = project.group.toString()
			groupId = if (projectGroup.endsWith(".minecraft")) projectGroup else "$projectGroup.minecraft"
			artifactId = project.name
			version = project.version.toString()

			from(components["minecraftMod"])
		}
	}
}


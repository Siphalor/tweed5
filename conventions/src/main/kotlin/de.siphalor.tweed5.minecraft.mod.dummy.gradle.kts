import org.gradle.api.publish.internal.PublicationInternal

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

	dependsOn(tasks.named("processMinecraftModResources"))
	from(project.layout.buildDirectory.dir("minecraftModResources"))

	archiveClassifier = "sources"
	destinationDirectory.set(layout.buildDirectory.dir("minecraftModLibs"))
}

artifacts.add("minecraftModElements", minecraftModJar)
artifacts.add("minecraftModApiElements", minecraftModJar)
afterEvaluate {
	tasks.findByName("sourcesJar")?.let { sourcesJar ->
		artifacts.add("minecraftModSourcesElements", minecraftModSourcesJar)

		minecraftModSourcesJar.configure {
			val sourcesJar = sourcesJar as Jar
			dependsOn(sourcesJar)
			val sourcesJarArtifact = objects.fileCollection().from(sourcesJar.archiveFile)
			inputs.files(sourcesJarArtifact)
			from(files(sourcesJarArtifact).map { zipTree(it) })
		}
	}
}

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

// Required because the maven publish plugin only supports one publication per project dependency
// to be able to resolve the dependency's coordinates.
// Luckily, this is fine in our case, as the Minecraft mod jars should publish each other as dependencies
// anyway. With proper dependencies in place, missing dependencies tend to become an unexpected runtime
// error in Minecraft modding.
afterEvaluate {
	if (publishing.publications.names.size > 1) {
		(publishing.publications.findByName("minecraftMod") as? PublicationInternal<*>)?.isAlias = true
	}
}

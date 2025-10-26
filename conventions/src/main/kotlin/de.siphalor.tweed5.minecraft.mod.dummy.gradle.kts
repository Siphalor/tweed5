plugins {
	`maven-publish`
	id("de.siphalor.tweed5.publishing")
	id("de.siphalor.tweed5.minecraft.mod.base")
}

val processMinecraftModResources = tasks.register<Copy>("processMinecraftModResources") {
	inputs.property("id", project.name)
	inputs.property("version", project.version)
	inputs.property("name", properties["module.name"])
	inputs.property("description", properties["module.description"])

	from(project.layout.settingsDirectory.dir("../tweed5-minecraft/mod-template/resources"))
	expand(mapOf(
		"id" to project.name,
		"version" to project.version,
		"name" to properties["module.name"],
		"description" to properties["module.description"]
	))
	into(project.layout.buildDirectory.dir("minecraftModResources"))
}

tasks.named<Jar>("minecraftModJar") {
	from(project.layout.buildDirectory.dir("minecraftModResources"))
	dependsOn(processMinecraftModResources)
}

tasks.named<Jar>("minecraftModSourcesJar") {
	from(project.layout.buildDirectory.dir("minecraftModResources"))
	dependsOn(processMinecraftModResources)
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


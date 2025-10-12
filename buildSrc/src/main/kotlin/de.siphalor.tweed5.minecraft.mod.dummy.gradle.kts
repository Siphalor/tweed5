plugins {
	id("de.siphalor.tweed5.minecraft.mod.base")
}

val processMinecraftModResources = tasks.register<Copy>("processMinecraftModResources") {
	from(project.layout.settingsDirectory.dir("minecraft/mod-template/resources"))
	expand(mapOf(
		"id" to project.name,
		"version" to project.version,
		"name" to properties["minecraft.mod.name"],
		"description" to properties["minecraft.mod.description"]
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

plugins {
	id("com.gradleup.shadow")
	java
	`java-library`
	id("de.siphalor.tweed5.publishing")
	id("de.siphalor.tweed5.minecraft.mod.component")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.shadowJar {
	relocate("org.apache.commons", "de.siphalor.tweed5.shadowed.org.apache.commons")
}

val processMinecraftModResources = tasks.register<Sync>("processMinecraftModResources") {
	inputs.property("id", project.name)
	inputs.property("version", project.version)
	inputs.property("name", properties["module.name"])
	inputs.property("description", properties["module.description"])
	inputs.property("repoUrl", properties["git.url"])

	from(project.layout.settingsDirectory.dir("../tweed5-minecraft/mod-template/resources"))
	expand(mapOf(
		"id" to project.name.replace('-', '_'),
		"version" to project.version,
		"name" to properties["module.name"],
		"description" to properties["module.description"],
		"repoUrl" to properties["git.url"],
	))
	into(project.layout.buildDirectory.dir("minecraftModResources"))
}

val minecraftModJar = tasks.register<Jar>("minecraftModJar") {
	group = LifecycleBasePlugin.BUILD_GROUP

	dependsOn(processMinecraftModResources)

	from(zipTree(tasks.shadowJar.get().archiveFile))
	from(project.layout.buildDirectory.dir("minecraftModResources"))

	destinationDirectory.set(layout.buildDirectory.dir("minecraftModLibs"))
}
tasks.assemble {
	dependsOn(minecraftModJar)
}

val minecraftModSourcesJar = tasks.register<Jar>("minecraftModSourcesJar") {
	group = LifecycleBasePlugin.BUILD_GROUP

	dependsOn(processMinecraftModResources)

	from(zipTree(tasks.named<Jar>("sourcesJar").get().archiveFile))
	from(project.layout.buildDirectory.dir("minecraftModResources"))

	archiveClassifier = "sources"
	destinationDirectory.set(layout.buildDirectory.dir("minecraftModLibs"))
}

artifacts.add("minecraftModElements", minecraftModJar)
artifacts.add("minecraftModApiElements", minecraftModJar)
artifacts.add("minecraftModSourcesElements", minecraftModSourcesJar)

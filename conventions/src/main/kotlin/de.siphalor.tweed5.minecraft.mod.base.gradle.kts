plugins {
	id("com.gradleup.shadow")
	java
	`java-library`
	id("de.siphalor.tweed5.shadow.explicit")
	id("de.siphalor.tweed5.minecraft.mod.component")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

val minecraftJijElements = configurations.resolvable("minecraftJijElements")

tasks.shadowJar {
	relocate("org.apache.commons", "de.siphalor.tweed5.shadowed.org.apache.commons")
}

fun formatJarsForJson(jars: FileCollection): String {
	return jars.files.joinToString(",") { "{\"file\":\"META-INF/jars/${it.name}\"}"}
}

tasks.register<Sync>("processMinecraftModResources") {
	inputs.property("id", project.name)
	inputs.property("version", project.version)
	inputs.property("name", properties["module.name"])
	inputs.property("description", properties["module.description"])
	inputs.property("repoUrl", properties["git.url"])
	inputs.files(minecraftJijElements)

	val jars = objects.fileCollection().apply { from(minecraftJijElements) }

	from(project.layout.settingsDirectory.dir("../tweed5-minecraft/mod-template/resources")) {
		expand(
			mapOf(
				"id" to project.name.replace('-', '_'),
				"version" to project.version,
				"name" to properties["module.name"],
				"description" to properties["module.description"],
				"repoUrl" to properties["git.url"],
				"jars" to formatJarsForJson(jars)
			)
		)
	}
	from(project.layout.settingsDirectory.file("../images/logo-48.png")) {
		into("assets/tweed5")
	}
	into(project.layout.buildDirectory.dir("minecraftModResources"))
}

tasks.register<Sync>("processMinecraftTestmodResources") {
	inputs.property("id", project.name)
	inputs.property("version", project.version)
	inputs.property("name", properties["module.name"])
	inputs.property("description", properties["module.description"])
	inputs.property("repoUrl", properties["git.url"])
	inputs.files(minecraftJijElements)

	val jars = objects.fileCollection().apply { from(minecraftJijElements) }

	from(project.layout.settingsDirectory.dir("../tweed5-minecraft/mod-template/resources")) {
		expand(
			mapOf(
				"id" to "${project.name.replace('-', '_')}_testmod",
				"version" to project.version,
				"name" to "${properties["module.name"]} (test mod)",
				"description" to properties["module.description"],
				"repoUrl" to properties["git.url"],
				"jars" to formatJarsForJson(jars)
			)
		)
	}
	into(project.layout.buildDirectory.dir("minecraftTestmodResources"))
}

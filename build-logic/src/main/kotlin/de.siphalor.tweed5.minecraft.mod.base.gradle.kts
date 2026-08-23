plugins {
	id("com.gradleup.shadow")
	java
	`java-library`
	id("de.siphalor.tweed5.root-properties")
	id("de.siphalor.tweed5.module-info")
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
	inputs.property("name", moduleInfo.name)
	inputs.property("description", moduleInfo.description)
	inputs.property("repoUrl", rootProperties["git.url"])
	inputs.files(minecraftJijElements)

	val jars = objects.fileCollection().apply { from(minecraftJijElements) }

	from(project.layout.settingsDirectory.dir("../tweed5-minecraft/mod-template/resources")) {
		expand(
			mapOf(
				"id" to project.name.replace('-', '_'),
				"version" to project.version,
				"name" to moduleInfo.name,
				"description" to moduleInfo.description,
				"repoUrl" to rootProperties["git.url"].get(),
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
	inputs.property("name", moduleInfo.name)
	inputs.property("description", moduleInfo.description)
	inputs.property("repoUrl", rootProperties["git.url"])
	inputs.files(minecraftJijElements)

	val jars = objects.fileCollection().apply { from(minecraftJijElements) }

	from(project.layout.settingsDirectory.dir("../tweed5-minecraft/mod-template/resources")) {
		expand(
			mapOf(
				"id" to "${project.name.replace('-', '_')}_testmod",
				"version" to project.version,
				"name" to "${moduleInfo.name.get()} (test mod)",
				"description" to moduleInfo.description,
				"repoUrl" to rootProperties["git.url"].get(),
				"jars" to formatJarsForJson(jars)
			)
		)
	}
	into(project.layout.buildDirectory.dir("minecraftTestmodResources"))
}

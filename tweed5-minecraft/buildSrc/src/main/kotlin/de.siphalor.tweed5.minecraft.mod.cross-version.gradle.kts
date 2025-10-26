import de.siphalor.tweed5.gradle.plugin.minecraft.mod.MinecraftModded
import java.util.Properties

plugins {
	java
	id("fabric-loom")
	id("de.siphalor.tweed5.expanded-sources-jar")
	id("de.siphalor.jcyo")
	id("io.freefair.lombok")
	id("de.siphalor.tweed5.shadow.explicit")
	id("de.siphalor.tweed5.minecraft.mod.base")
}

val minecraftVersionDescriptor = project.property("minecraft.version.descriptor") as String
val mcProps = Properties().apply {
	val propFile = project.layout.settingsDirectory.file("gradle/mc-$minecraftVersionDescriptor/gradle.properties").asFile
	propFile.inputStream().use { load(it) }
}

val mcCatalog = versionCatalogs.named("mcLibs")

group = "de.siphalor.tweed5.minecraft.${project.name}"
val archivesBaseName = "${project.name}-mc$minecraftVersionDescriptor"
base {
	archivesName.set(archivesBaseName)
}
val shortVersion = project.property("tweed5.version").toString()
val minecraftVersion = getMcCatalogVersion("minecraft")
version = "$shortVersion+mc$minecraftVersion"

val testmod by sourceSets.creating {
	compileClasspath += sourceSets.main.get().compileClasspath
	runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

loom {
	runs {
		create("testmodClient") {
			client()
			name("${properties["module.name"]} Test Mod (client)")
			source(testmod)
		}
	}
	createRemapConfigurations(testmod)
}

// For some reason dependencyResolutionManagement from the settings.gradle doesn't seem to be passed through correctly,
// so we're defining the repositories right here
repositories {
	maven {
		name = "Parchment"
		url = uri("https://maven.parchmentmc.org")
		mavenContent {
			includeGroupAndSubgroups("org.parchmentmc")
		}
	}
	maven {
		name = "Siphalor"
		url = uri("https://maven.siphalor.de")
		mavenContent {
			includeGroupAndSubgroups("de.siphalor")
		}
	}
}

configurations {
	named("testmodRuntimeClasspath") {
		attributes {
			attribute(MinecraftModded.MINECRAFT_MODDED_ATTRIBUTE, objects.named(MinecraftModded.MODDED))
		}
	}
}

dependencies {
	minecraft(mcCatalog.findLibrary("minecraft").get())
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-$minecraftVersion:${getMcCatalogVersion("parchment")}@zip")
	})
	modImplementation(mcCommonLibs.fabric.loader)

	compileOnly(libs.jspecify.annotations)

	"testmodImplementation"(sourceSets.main.map { it.output })
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

lombok {
	version = libs.versions.lombok.get()
}

tasks.jar {
	dependsOn(tasks.processMinecraftModResources)
	from(project.layout.buildDirectory.dir("minecraftModResources"))
}

tasks.sourcesJar {
	dependsOn(tasks.processMinecraftModResources)
	from(project.layout.buildDirectory.dir("minecraftModResources"))
}

tasks.named<Copy>("processTestmodResources") {
	inputs.property("id", project.name)
	inputs.property("version", project.version)
	inputs.property("name", properties["module.name"])
	inputs.property("description", properties["module.description"])
	inputs.property("repoUrl", properties["git.url"])

	from(project.layout.settingsDirectory.dir("../tweed5-minecraft/mod-template/resources")) {
		expand(mapOf(
			"id" to project.name.replace('-', '_') + "_testmod",
			"version" to project.version,
			"name" to properties["module.name"].toString() + " (test mod)",
			"description" to properties["module.description"],
			"repoUrl" to properties["git.url"],
		))
	}
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

fun getMcCatalogVersion(name: String): String {
	return mcCatalog.findVersion(name).get().requiredVersion
}

import java.util.Properties

plugins {
	id("fabric-loom")
	id("de.siphalor.jcyo")
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

sourceSets {
	create("testmod") {
		compileClasspath += sourceSets.main.get().compileClasspath
		runtimeClasspath += sourceSets.main.get().runtimeClasspath
	}
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

dependencies {
	minecraft(mcCatalog.findLibrary("minecraft").get())
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-$minecraftVersion:${getMcCatalogVersion("parchment")}@zip")
	})
	modImplementation(mcCommonLibs.fabric.loader)
}

fun getMcCatalogVersion(name: String): String {
	return mcCatalog.findVersion(name).get().requiredVersion
}

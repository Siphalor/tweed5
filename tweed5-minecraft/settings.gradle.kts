rootProject.name = "tweed5-minecraft"

pluginManagement {
	includeBuild("../conventions")

	repositories {
		gradlePluginPortal()
		maven {
			name = "Siphalor"
			url = uri("https://maven.siphalor.de")
			mavenContent {
				includeGroupAndSubgroups("de.siphalor")
			}
		}
		maven {
			name = "FabricMC"
			url = uri("https://maven.fabricmc.net")
			mavenContent {
				includeGroupAndSubgroups("net.fabricmc")
				includeGroup("fabric-loom")
			}
		}
	}
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		maven {
			name = "FabricMC"
			url = uri("https://maven.fabricmc.net")
			mavenContent {
				includeGroupAndSubgroups("net.fabricmc")
			}
		}
	}

	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
		create("mcCommonLibs") {
			from(files("gradle/mcCommonLibs.versions.toml"))
		}
		create("mcLibs") {
			val mcVersionDescriptor = providers.gradleProperty("minecraft.version.descriptor").get()
			from(files("gradle/mc-$mcVersionDescriptor/mcLibs.versions.toml"))
		}
	}
}

includeBuild("../tweed5")

includeNormalModule("bundle")
includeNormalModule("fabric-helper")

fun includeNormalModule(name: String) {
	includeAs("tweed5-$name", name)
}

fun includeAs(name: String, path: String) {
	include(name)
	project(":$name").projectDir = file(path)
}

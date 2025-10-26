plugins {
	id("dev.panuszewski.typesafe-conventions") version "0.9.0"
}

typesafeConventions {
	autoPluginDependencies = false
}

dependencyResolutionManagement {
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
	versionCatalogs {
		create("libs") {
			from(files("../../gradle/libs.versions.toml"))
		}
	}
}

includeBuild("../../conventions")

rootProject.name = "tweed5-minecraft-conventions"

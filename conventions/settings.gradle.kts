plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.9.0"
}

dependencyResolutionManagement {
	repositories {
		gradlePluginPortal()
	}
	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}

include("helpers")

rootProject.name = "tweed5-conventions"

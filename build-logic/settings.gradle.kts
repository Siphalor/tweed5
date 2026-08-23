plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.9.1"
}

typesafeConventions {
	autoPluginDependencies = false
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

include(":tweed5-build-logic-helpers")
project(":tweed5-build-logic-helpers").projectDir = file("helpers")

rootProject.name = "tweed5-build-logic"

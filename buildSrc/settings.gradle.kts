plugins {
    id("dev.panuszewski.typesafe-conventions") version "0.7.3"
}

dependencyResolutionManagement {
	repositories {
		gradlePluginPortal()
	}
}

include("helpers")

rootProject.name = "tweed5-conventions"

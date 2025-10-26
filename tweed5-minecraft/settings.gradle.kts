rootProject.name = "tweed5-minecraft"

pluginManagement {
	includeBuild("../conventions")
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}

includeBuild("../tweed5")

includeNormalModule("bundle")

fun includeNormalModule(name: String) {
	includeAs("tweed5-$name", "$name")
}

fun includeAs(name: String, path: String) {
	include(name)
	project(":$name").projectDir = file(path)
}

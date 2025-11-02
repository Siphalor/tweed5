rootProject.name = "tweed5"

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

includeNormalModule("annotation-inheritance")
includeNormalModule("attributes-extension")
includeNormalModule("comment-loader-extension")
includeNormalModule("construct")
includeNormalModule("core")
includeNormalModule("default-extensions")
includeNormalModule("naming-format")
includeNormalModule("patchwork")
includeNormalModule("serde-api")
includeNormalModule("serde-extension")
includeNormalModule("serde-gson")
includeNormalModule("serde-hjson")
includeNormalModule("serde-jackson")
includeNormalModule("type-utils")
includeNormalModule("utils")
includeNormalModule("weaver-pojo")
includeNormalModule("weaver-pojo-attributes-extension")
includeNormalModule("weaver-pojo-presets-extension")
includeNormalModule("weaver-pojo-serde-extension")
includeNormalModule("weaver-pojo-validation-extension")

include("test-utils")
includeAs("generic-test-utils", "test-utils/generic")
includeAs("serde-json-test-utils", "test-utils/serde-json")

fun includeNormalModule(name: String) {
	includeAs("tweed5-$name", "$name")
}

fun includeAs(name: String, path: String) {
	include(name)
	project(":$name").projectDir = file(path)
}

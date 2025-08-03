rootProject.name = "tweed5"

include("test-utils")
include("tweed5-annotation-inheritance")
include("tweed5-attributes-extension")
include("tweed5-construct")
include("tweed5-core")
include("tweed5-default-extensions")
include("tweed5-naming-format")
include("tweed5-patchwork")
include("tweed5-serde-api")
include("tweed5-serde-extension")
include("tweed5-serde-hjson")
include("tweed5-type-utils")
include("tweed5-utils")
include("tweed5-weaver-pojo")
include("tweed5-weaver-pojo-attributes-extension")
include("tweed5-weaver-pojo-serde-extension")
include("tweed5-weaver-pojo-validation-extension")

includeAs("minecraft:tweed5-bundle", "minecraft/tweed5-bundle")

fun includeAs(name: String, path: String) {
    include(name)
    project(":$name").projectDir = file(path)
}

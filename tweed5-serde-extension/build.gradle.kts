dependencies {
    api(project(":tweed5-core"))
    api(project(":tweed5-patchwork"))
    api(project(":tweed5-serde-api"))

    testImplementation(project(":tweed5-serde-hjson"))
}
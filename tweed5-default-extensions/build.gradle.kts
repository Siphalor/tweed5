plugins {
    id("de.siphalor.tweed5.base-module")
}

dependencies {
    api(project(":tweed5-core"))
    api(project(":tweed5-serde-extension"))

    testImplementation(project(":tweed5-serde-hjson"))
}
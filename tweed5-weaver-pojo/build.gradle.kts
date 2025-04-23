plugins {
    id("de.siphalor.tweed5.base-module")
}

dependencies {
    implementation(project(":tweed5-construct"))
    api(project(":tweed5-core"))
    api(project(":tweed5-naming-format"))
    api(project(":tweed5-type-utils"))
}

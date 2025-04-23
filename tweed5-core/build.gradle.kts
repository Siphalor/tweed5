plugins {
    id("de.siphalor.tweed5.base-module")
}

dependencies {
    implementation(project(":tweed5-construct"))
    api(project(":tweed5-patchwork"))
    api(project(":tweed5-utils"))
}

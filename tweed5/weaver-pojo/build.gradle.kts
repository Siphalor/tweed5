plugins {
    id("de.siphalor.tweed5.base-module")
    id("de.siphalor.tweed5.minecraft.mod.dummy")
}

dependencies {
    implementation(project(":tweed5-construct"))
    api(project(":tweed5-annotation-inheritance"))
    api(project(":tweed5-core"))
    api(project(":tweed5-naming-format"))
    api(project(":tweed5-type-utils"))
}

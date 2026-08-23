plugins {
    id("de.siphalor.tweed5.base-module")
    id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Default Extensions"
    description = "A collection of commonly used Tweed 5 extensions bundled for convenience."
}

dependencies {
    api(project(":tweed5-core"))
    api(project(":tweed5-serde-extension"))

    testImplementation(project(":tweed5-serde-hjson"))
}

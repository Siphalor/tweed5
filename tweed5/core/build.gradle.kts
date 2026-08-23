plugins {
    id("de.siphalor.tweed5.base-module")
    id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Core"
    description = "Provides core APIs and functionality for Tweed 5, like entries, containers and extensions."
}

dependencies {
    implementation(project(":tweed5-construct"))
    api(project(":tweed5-patchwork"))
    api(project(":tweed5-utils"))
}

plugins {
    id("de.siphalor.tweed5.base-module")
    id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Serde Extension"
    description = "A Tweed extension that provides support for reading and writing entries using Tweed's serde API."
}

dependencies {
    api(project(":tweed5-core"))
    api(project(":tweed5-patchwork"))
    api(project(":tweed5-serde-api"))

    testImplementation(project(":tweed5-serde-hjson"))
}

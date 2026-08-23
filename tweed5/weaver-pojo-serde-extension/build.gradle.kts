plugins {
    id("de.siphalor.tweed5.base-module")
    id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Serde Extension for Tweed 5 Weaver POJO"
    description = "Adds support for declaring and deriving serializers and deserializers for Tweed 5's Serde API from annotations."
}

dependencies {
    api(project(":tweed5-weaver-pojo"))
    api(project(":tweed5-serde-extension"))

    testImplementation(project(":tweed5-default-extensions"))
    testImplementation(project(":tweed5-serde-hjson"))
}

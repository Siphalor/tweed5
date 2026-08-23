plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Validation Extension for Tweed 5 Weaver POJO"
    description = "Allows declaring validation constraints on POJOs using annotations."
}

dependencies {
	api(project(":tweed5-construct"))
	api(project(":tweed5-default-extensions"))
	api(project(":tweed5-weaver-pojo"))
}

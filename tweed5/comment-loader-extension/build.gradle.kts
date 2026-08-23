plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Comment Loader Extension"
    description = "Tweed 5 module that allows dynamically loading comments from data files, e.g., for i18n"
}

dependencies {
	implementation(project(":tweed5-core"))
	implementation(project(":tweed5-default-extensions"))

	testImplementation(project(":tweed5-serde-gson"))
}

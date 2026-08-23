plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Attributes Extension"
    description = "A Tweed extension that allows defining generic attributes on config entries."
}

dependencies {
	implementation(project(":tweed5-core"))
	compileOnly(project(":tweed5-serde-extension"))
	testImplementation(project(":tweed5-default-extensions"))
	testImplementation(project(":tweed5-serde-extension"))
	testImplementation(project(":tweed5-serde-hjson"))
}

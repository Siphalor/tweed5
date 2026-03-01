plugins {
	id("de.siphalor.tweed5.local-runtime-only")
	id("de.siphalor.tweed5.minecraft.mod.cross-version")
}

dependencies {
	modCompileOnly(fabricApi.module("fabric-networking-api-v1", mcLibs.versions.fabric.api.get()))
	compileOnly("de.siphalor.tweed5:tweed5-core")
	compileOnly("de.siphalor.tweed5:tweed5-serde-extension")

	testImplementation("de.siphalor.tweed5:tweed5-core")
	testImplementation("de.siphalor.tweed5:tweed5-serde-extension")
}

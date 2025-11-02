plugins {
	id("de.siphalor.tweed5.local-runtime-only")
	id("de.siphalor.tweed5.minecraft.mod.cross-version")
}

dependencies {
	modCompileOnly(fabricApi.module("fabric-networking-api-v1", mcLibs.versions.fabric.api.get()))
	compileOnly("de.siphalor.tweed5:tweed5-comment-loader-extension")
	compileOnly("de.siphalor.tweed5:tweed5-core")
	compileOnly("de.siphalor.tweed5:tweed5-default-extensions")
	compileOnly("de.siphalor.tweed5:tweed5-serde-extension")
	compileOnly("de.siphalor.tweed5:tweed5-serde-gson")
	compileOnly("de.siphalor.tweed5:tweed5-weaver-pojo")
	compileOnly("de.siphalor.tweed5:tweed5-weaver-pojo-attributes-extension")
	compileOnly("de.siphalor.tweed5:tweed5-weaver-pojo-serde-extension")
	compileOnly("de.siphalor.tweed5:tweed5-weaver-pojo-validation-extension")
	compileOnly("de.siphalor.tweed5:tweed5-weaver-pojo-presets-extension")

	listOf("fabric-networking-api-v1", "fabric-lifecycle-events-v1").forEach {
		modTestmodImplementation(fabricApi.module(it, mcLibs.versions.fabric.api.get()))
	}
	testmodImplementation(project(":tweed5-bundle", configuration = "minecraftModElements"))
	testmodImplementation("de.siphalor.tweed5:tweed5-comment-loader-extension")
	testmodImplementation("de.siphalor.tweed5:tweed5-serde-hjson")
	testmodImplementation("de.siphalor.tweed5:tweed5-serde-gson")
}

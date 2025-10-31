plugins {
	id("de.siphalor.tweed5.expanded-sources-jar")
	id("de.siphalor.tweed5.minecraft.mod.cross-version")
}

dependencies {
	compileOnly("de.siphalor.tweed5:tweed5-core")
	compileOnly("de.siphalor.tweed5:tweed5-attributes-extension")
	compileOnly("de.siphalor.tweed5:tweed5-default-extensions")
	compileOnly("de.siphalor.tweed5:tweed5-weaver-pojo")
	modCompileOnly(mcLibs.coat)

	listOf("fabric-key-binding-api-v1", "fabric-resource-loader-v0").forEach {
		modTestmodImplementation(fabricApi.module(it, mcLibs.versions.fabric.api.get()))
	}
	testmodImplementation(project(":tweed5-bundle", configuration = "minecraftModElements"))
	modTestmodImplementation(mcLibs.coat)
	modTestmodImplementation(mcLibs.amecs.api)
	testmodImplementation(project(":tweed5-fabric-helper"))
	testmodImplementation("de.siphalor.tweed5:tweed5-serde-hjson")
}

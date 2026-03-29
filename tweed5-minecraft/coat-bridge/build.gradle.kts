plugins {
	id("de.siphalor.tweed5.expanded-sources-jar")
	id("de.siphalor.tweed5.minecraft.mod.cross-version")
}

dependencies {
	compileOnly("de.siphalor.tweed5:tweed5-core")
	compileOnly("de.siphalor.tweed5:tweed5-attributes-extension")
	compileOnly("de.siphalor.tweed5:tweed5-default-extensions")
	compileOnly("de.siphalor.tweed5:tweed5-weaver-pojo")
	compileOnly(project(":tweed5-logging", configuration = "minecraftModApiElements"))
	modCompileOnly(mcLibs.coat)

	listOf(smcmtk.mcProps.getting("fabric.api.key_mapping").get(), "fabric-resource-loader-v0").forEach {
		modTestmodImplementation(fabricApi.module(it, mcLibs.versions.fabric.api.get()))
	}
	testmodImplementation(project(":tweed5-logging", configuration = "minecraftModElements"))
	testmodImplementation(project(":tweed5-bundle", configuration = "runtimeElements"))
	testmodImplementation(project(":tweed5-bundle-pojo-weaving", configuration = "runtimeElements"))
	testmodImplementation(project(":tweed5-fabric-helper", configuration = "namedElements"))
	modTestmodImplementation(mcLibs.coat)
	modTestmodImplementation(mcLibs.amecs.priorityKeyMappings)
	testmodImplementation("de.siphalor.tweed5:tweed5-serde-hjson")
}

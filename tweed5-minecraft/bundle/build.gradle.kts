import de.siphalor.tweed5.minecraft.bundled.sources.BundledSourcesJar

plugins {
	id("de.siphalor.tweed5.minecraft.mod.bundle")
}

val bundledSourcesConfiguration = configurations.resolvable("bundledSources") {
	extendsFrom(configurations.implementation.get())
	isTransitive = true
	attributes {
		attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
	}
}

dependencies {
	minecraftJij("de.siphalor.tweed5:tweed5-core")
	minecraftJij("de.siphalor.tweed5:tweed5-attributes-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-default-extensions")
	minecraftJij("de.siphalor.tweed5:tweed5-serde-extension")
	minecraftJij(project(":tweed5-logging"))
}

tasks.register<BundledSourcesJar>("sourcesJar") {
	sources.from(bundledSourcesConfiguration)
	archiveClassifier.set("sources")
}

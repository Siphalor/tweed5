import de.siphalor.tweed5.minecraft.bundled.sources.BundledSourcesJar

plugins {
	id("de.siphalor.tweed5.minecraft.mod.bundle")
}

configurations.minecraftJijElements {
	isTransitive = false
}
val bundledSourcesConfiguration = configurations.resolvable("bundledSources") {
	extendsFrom(configurations.minecraftJijElements.get())
	isTransitive = false
	attributes {
		attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
	}
}

dependencies {
	minecraftJij("de.siphalor.tweed5:tweed5-annotation-inheritance")
	minecraftJij("de.siphalor.tweed5:tweed5-naming-format")
	minecraftJij("de.siphalor.tweed5:tweed5-type-utils")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-attributes-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-presets-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-serde-extension")
	minecraftJij("de.siphalor.tweed5:tweed5-weaver-pojo-validation-extension")
}

tasks.register<BundledSourcesJar>("sourcesJar") {
	sources.from(bundledSourcesConfiguration)
	archiveClassifier.set("sources")
}

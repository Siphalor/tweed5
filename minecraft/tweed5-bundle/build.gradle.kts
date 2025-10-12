plugins {
	id("de.siphalor.tweed5.publishing")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

val bundledSourcesConfiguration = configurations.resolvable("bundledSources") {
	extendsFrom(configurations.implementation.get())
	isTransitive = true
	attributes {
		attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
	}
}

dependencies {
	implementation(project(":tweed5-core"))
	implementation(project(":tweed5-attributes-extension"))
	implementation(project(":tweed5-default-extensions"))
	implementation(project(":tweed5-serde-extension"))
	implementation(project(":tweed5-weaver-pojo"))
	implementation(project(":tweed5-weaver-pojo-attributes-extension"))
	implementation(project(":tweed5-weaver-pojo-serde-extension"))
	implementation(project(":tweed5-weaver-pojo-validation-extension"))
}

tasks.shadowJar {
	relocate("org.objectweb.asm", "de.siphalor.tweed5.shadowed.org.objectweb.asm")
}

tasks.register<Jar>("sourcesJar") {
	inputs.files(bundledSourcesConfiguration)
	from(
		bundledSourcesConfiguration.get().resolve()
			.filter { it.name.startsWith(rootProject.name) }
			.map { zipTree(it) }
	)
	archiveClassifier.set("sources")
}

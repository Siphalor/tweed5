import de.siphalor.tweed5.minecraft.bundled.sources.BundledSourcesJar

plugins {
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

val bundledSourcesConfiguration = configurations.resolvable("bundledSources") {
	extendsFrom(configurations.implementation.get())
	isTransitive = true
	attributes {
		attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
	}
}

configurations.implementation {
	exclude(group = "commons-logging", module = "commons-logging")
}

val vendoredCommonsLogging = project.layout.settingsDirectory.file("vendor/commons-logging").asFile

dependencies {
	implementation("de.siphalor.tweed5:tweed5-core")
	implementation("de.siphalor.tweed5:tweed5-attributes-extension")
	implementation("de.siphalor.tweed5:tweed5-default-extensions")
	implementation("de.siphalor.tweed5:tweed5-serde-extension")
	implementation("de.siphalor.tweed5:tweed5-weaver-pojo")
	implementation("de.siphalor.tweed5:tweed5-weaver-pojo-attributes-extension")
	implementation("de.siphalor.tweed5:tweed5-weaver-pojo-presets-extension")
	implementation("de.siphalor.tweed5:tweed5-weaver-pojo-serde-extension")
	implementation("de.siphalor.tweed5:tweed5-weaver-pojo-validation-extension")

	implementation(
		objects.fileCollection().apply {
			from(
				vendoredCommonsLogging.resolve("target")
					.listFiles { it.name.endsWith("SNAPSHOT.jar") }
			)
			builtBy("compileCommonsLogging")
		}
	)
}

tasks.register<Exec>("compileCommonsLogging") {
	inputs.file(vendoredCommonsLogging.resolve("pom.xml"))
	inputs.dir(vendoredCommonsLogging.resolve("src"))
	outputs.dir(vendoredCommonsLogging.resolve("target"))
	commandLine("mvn", "package", "-DskipTests")
	workingDir(vendoredCommonsLogging)
}

tasks.shadowJar {
	relocate("org.objectweb.asm", "de.siphalor.tweed5.shadowed.org.objectweb.asm")

	relocate("META-INF", "META-INF/tweed5-vendored/commons-logging") {
		include("META-INF/*.txt")
	}
	exclude("META-INF/maven/**")
	// Remove some obsolete classes
	exclude("org/apache/commons/logging/impl/WeakHashtable*")
}

tasks.register<BundledSourcesJar>("sourcesJar") {
	sources.from(bundledSourcesConfiguration)
	archiveClassifier.set("sources")
}

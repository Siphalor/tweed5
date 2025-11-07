plugins {
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

val vendoredCommonsLogging = project.layout.settingsDirectory.file("vendor/commons-logging").asFile

dependencies {
	shadowOnly(objects.fileCollection().apply {
		from(
			vendoredCommonsLogging.resolve("target")
				.listFiles { it.name.endsWith("SNAPSHOT.jar") }
		)
		builtBy("compileCommonsLogging")
	})
}

tasks.register<Exec>("compileCommonsLogging") {
	inputs.file(vendoredCommonsLogging.resolve("pom.xml"))
	inputs.dir(vendoredCommonsLogging.resolve("src"))
	outputs.dir(vendoredCommonsLogging.resolve("target"))
	commandLine("mvn", "package", "-DskipTests")
	workingDir(vendoredCommonsLogging)
}

tasks.shadowJar {
	relocate("META-INF", "META-INF/tweed5-vendored/commons-logging") {
		include("META-INF/*.txt")
	}
	exclude("META-INF/maven/**")
	// Remove some obsolete classes
	exclude("org/apache/commons/logging/impl/WeakHashtable*")
}

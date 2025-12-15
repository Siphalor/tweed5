import de.siphalor.jcyo.gradle.JcyoTask
import de.siphalor.tweed5.gradle.plugin.minecraft.mod.MinecraftModded
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

plugins {
	java
	id("fabric-loom")
	id("de.siphalor.tweed5.publishing")
	id("de.siphalor.tweed5.expanded-sources-jar")
	id("de.siphalor.jcyo")
	id("io.freefair.lombok")
	id("com.gradleup.shadow")
	id("de.siphalor.tweed5.shadow.explicit")
	id("de.siphalor.tweed5.minecraft.mod.base")
}

val minecraftVersionDescriptor = project.property("minecraft.version.descriptor") as String
val mcProps = Properties().apply {
	val propFile = project.layout.settingsDirectory.file("gradle/mc-$minecraftVersionDescriptor/gradle.properties").asFile
	propFile.inputStream().use { load(it) }
}

val mcCatalog = versionCatalogs.named("mcLibs")

group = "de.siphalor.tweed5.minecraft"
val archivesBaseName = "${project.name}-mc$minecraftVersionDescriptor"
base {
	archivesName.set(archivesBaseName)
}
val shortVersion = project.property("tweed5.version").toString()
val minecraftVersion = getMcCatalogVersion("minecraft")
version = "$shortVersion+mc$minecraftVersion"

val testmod by sourceSets.creating {
	compileClasspath += sourceSets.main.get().compileClasspath
	runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

loom {
	runs {
		create("testmodClient") {
			client()
			name("${properties["module.name"]} Test Mod (client)")
			source(testmod)
		}
	}
	createRemapConfigurations(testmod)
}

// For some reason dependencyResolutionManagement from the settings.gradle doesn't seem to be passed through correctly,
// so we're defining the repositories right here
repositories {
	maven {
		name = "Parchment"
		url = uri("https://maven.parchmentmc.org")
		mavenContent {
			includeGroupAndSubgroups("org.parchmentmc")
		}
	}
	maven {
		name = "Siphalor"
		url = uri("https://maven.siphalor.de")
		mavenContent {
			includeGroupAndSubgroups("de.siphalor")
		}
	}
	mavenLocal()
}

configurations {
	named("testmodRuntimeClasspath") {
		attributes {
			attribute(MinecraftModded.MINECRAFT_MODDED_ATTRIBUTE, objects.named(MinecraftModded.MODDED))
		}
	}
}

dependencies {
	minecraft(mcCatalog.findLibrary("minecraft").get())
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-$minecraftVersion:${getMcCatalogVersion("parchment")}@zip")
	})
	modImplementation(mcCommonLibs.fabric.loader)
	"modTestmodImplementation"(mcCommonLibs.fabric.loader)

	compileOnly(libs.jspecify.annotations)

	"testmodImplementation"(sourceSets.main.map { it.output })
}

val jcyoVars = mcProps.stringPropertyNames()
	.filter { it.startsWith("preprocessor.") }
	.map { it to mcProps[it] }
	.associate { (key, value) -> key.substring("preprocessor.".length) to value.toString() }
val jcyo = tasks.register<JcyoTask>("jcyo") {
	inputDirectory = file("src/main/java")
	variables = jcyoVars
}
val testmodJcyo = tasks.register<JcyoTask>("testmodJcyo") {
	inputDirectory = file("src/testmod/java")
	variables = jcyoVars
}

tasks.compileJava {
	dependsOn(jcyo)
}

tasks.named("compileTestmodJava") {
	dependsOn(testmodJcyo)
}

lombok {
	version = libs.versions.lombok.get()
}

val testmodLombokConfigSource = project.layout.settingsDirectory.file("lombok.testmod.config").asFile
val testmodLombokConfigTarget = file("src/testmod/lombok.config")
val copyTestmodLombokConfig by tasks.register("copyTestmodLombokConfig") {
	val source = testmodLombokConfigSource
	val target = testmodLombokConfigTarget
	inputs.file(source)
	outputs.file(target)

	doFirst {
		target.parentFile.mkdirs()
		Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
	}
}

tasks.named("compileTestmodJava") {
	inputs.file(testmodLombokConfigSource)
	dependsOn(copyTestmodLombokConfig)
}
afterEvaluate {
	tasks.named("generateTestmodEffectiveLombokConfig") {
		inputs.file(testmodLombokConfigSource)
		dependsOn(copyTestmodLombokConfig)
	}
}

tasks.named<Copy>("processResources") {
	val processMinecraftModResources = tasks.named<Sync>("processMinecraftModResources")
	dependsOn(processMinecraftModResources)
	from(processMinecraftModResources.get().destinationDir)
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<Copy>("processTestmodResources") {
	val processMinecraftTestmodResources = tasks.named<Sync>("processMinecraftTestmodResources")
	dependsOn(processMinecraftTestmodResources)
	from(processMinecraftTestmodResources.get().destinationDir)
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

shadow {
	addShadowVariantIntoJavaComponent = false
}

tasks.remapJar {
	dependsOn(tasks.shadowJar)
	inputFile = tasks.shadowJar.get().archiveFile
}

fun getMcCatalogVersion(name: String): String {
	return mcCatalog.findVersion(name).get().requiredVersion
}

publishing {
	publications {
		create<MavenPublication>("minecraftMod") {
			groupId = "${project.group}.${project.name}"
			artifactId = "${project.name}-mc${minecraftVersionDescriptor}"
			version = shortVersion

			from(components["java"])
		}
	}
}

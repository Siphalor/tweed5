package de.siphalor.tweed5.gradle.plugin.minecraft.mod

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.named
import javax.inject.Inject

abstract class MinecraftModComponentPlugin : Plugin<Project> {
	@get:Inject
	abstract val softwareComponentFactory: SoftwareComponentFactory

	@get:Inject
	abstract val objectFactory: ObjectFactory

	override fun apply(project: Project) {
		val modComponent = softwareComponentFactory.adhoc("minecraftMod")
		project.components.add(modComponent)

		val targetJvmVersion = project.tasks.named<JavaCompile>("compileJava")
			.map { JavaVersion.toVersion(it.targetCompatibility).majorVersion }

		val modElementsConfiguration = project.configurations.consumable("minecraftModElements") {
			attributes {
				attribute(MinecraftModded.MINECRAFT_MODDED_ATTRIBUTE, objectFactory.named(MinecraftModded.MODDED))
				attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.LIBRARY))
				attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objectFactory.named(LibraryElements.JAR))
				attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.EMBEDDED))
				attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.JAVA_RUNTIME))

				project.afterEvaluate {
					attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, targetJvmVersion.get().toInt())
				}
			}
		}

		val modApiElementsConfiguration = project.configurations.consumable("minecraftModApiElements") {
			attributes {
				attribute(MinecraftModded.MINECRAFT_MODDED_ATTRIBUTE, objectFactory.named(MinecraftModded.MODDED))
				attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.LIBRARY))
				attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objectFactory.named(LibraryElements.JAR))
				attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.EXTERNAL))
				attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.JAVA_RUNTIME))

				project.afterEvaluate {
					attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, targetJvmVersion.get().toInt())
				}
			}
		}

		val apiConfiguration = project.configurations.named("api")
		val modSourcesElementsConfiguration = project.configurations.consumable("minecraftModSourcesElements") {
			extendsFrom(apiConfiguration.get())
			attributes {
				attribute(MinecraftModded.MINECRAFT_MODDED_ATTRIBUTE, objectFactory.named(MinecraftModded.MODDED))
				attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.DOCUMENTATION))
				attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objectFactory.named(DocsType.SOURCES))
				attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.EXTERNAL))
			}
		}

		modComponent.addVariantsFromConfiguration(modElementsConfiguration.get()) {
			mapToMavenScope("runtime")
		}
		modComponent.addVariantsFromConfiguration(modApiElementsConfiguration.get()) {
			mapToMavenScope("compile")
		}
		modComponent.addVariantsFromConfiguration(modSourcesElementsConfiguration.get()) {
			mapToOptional()
		}
	}
}

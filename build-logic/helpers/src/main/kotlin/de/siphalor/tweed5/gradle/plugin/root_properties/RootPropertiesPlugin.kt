package de.siphalor.tweed5.gradle.plugin.root_properties

import de.siphalor.tweed5.gradle.plugin.RootPropertiesExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.apply
import java.util.Properties
import javax.inject.Inject

abstract class RootPropertiesPlugin : Plugin<Project> {
	@get:Inject
	abstract val providers: ProviderFactory

	override fun apply(project: Project) {
		val rootPropertiesFile = if (project.layout.settingsDirectory.file(".gitmodules").asFile.exists()) {
			project.layout.settingsDirectory.file("gradle.properties")
		} else {
			project.layout.settingsDirectory.file("../gradle.properties")
		}

		val rootProperties = providers.fileContents(rootPropertiesFile).asBytes.map { propsBytes ->
			Properties().apply {
				load(propsBytes.inputStream())
			}.map { it.key.toString() to it.value.toString() }.toMap()
		}

		project.extensions.create("rootProperties", RootPropertiesExtension::class.java).apply {
			properties.value(rootProperties)
		}
	}
}

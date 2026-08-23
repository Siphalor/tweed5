package de.siphalor.tweed5.gradle.plugin.module_info

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleInfoPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.extensions.create("moduleInfo", ModuleInfoExtension::class.java)
	}
}

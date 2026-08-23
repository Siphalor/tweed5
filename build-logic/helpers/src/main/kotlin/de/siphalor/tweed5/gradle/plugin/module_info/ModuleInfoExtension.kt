package de.siphalor.tweed5.gradle.plugin.module_info

import org.gradle.api.provider.Property

abstract class ModuleInfoExtension {
	abstract val name: Property<String>
	abstract val description: Property<String>
}

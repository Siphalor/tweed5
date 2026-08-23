package de.siphalor.tweed5.gradle.plugin

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider

abstract class RootPropertiesExtension {
	abstract val properties: MapProperty<String, String>

	operator fun get(key: String): Provider<String> = properties.getting(key)
}

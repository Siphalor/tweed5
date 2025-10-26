package de.siphalor.tweed5.gradle.plugin.minecraft.mod

import org.gradle.api.Named
import org.gradle.api.attributes.Attribute

interface MinecraftModded : Named {
	companion object {
		val MINECRAFT_MODDED_ATTRIBUTE = Attribute.of("de.siphalor.tweed5.minecraft.modded", MinecraftModded::class.java)
		const val PLAIN = "plain"
		const val MODDED = "modded"
	}
}

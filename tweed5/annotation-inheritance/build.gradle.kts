plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

moduleInfo {
    name = "Tweed 5 Annotation Inheritance"
    description = "Provides a mechanism to create meta-annotations. This allows bundling annotations that are commonly used together into a single convenience annotation."
}

configurations.minecraftModApiElements {
	exclude("org.ow2.asm", "asm")
}

dependencies {
	implementation(project(":tweed5-utils"))
	implementation(project(":tweed5-type-utils"))
	implementation(libs.asm.core)
	shadowOnly(libs.asm.core)
}

tasks.shadowJar {
	relocate("org.objectweb.asm", "de.siphalor.tweed5.annotationinheritance.shadowed.org.objectweb.asm")
}

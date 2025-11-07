plugins {
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
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

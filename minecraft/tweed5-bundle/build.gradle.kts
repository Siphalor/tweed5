plugins {
	`maven-publish`
	id("de.siphalor.tweed5.base-module")
	id("de.siphalor.tweed5.minecraft.mod.dummy")
}

dependencies {
	implementation(project(":tweed5-core"))
	implementation(project(":tweed5-attributes-extension"))
	implementation(project(":tweed5-default-extensions"))
	implementation(project(":tweed5-serde-extension"))
	implementation(project(":tweed5-weaver-pojo"))
	implementation(project(":tweed5-weaver-pojo-attributes-extension"))
	implementation(project(":tweed5-weaver-pojo-serde-extension"))
	implementation(project(":tweed5-weaver-pojo-validation-extension"))
}

tasks.shadowJar {
	relocate("org.objectweb.asm", "de.siphalor.tweed5.shadowed.org.objectweb.asm")
}

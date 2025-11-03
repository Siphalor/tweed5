plugins {
	id("com.gradleup.shadow")
	java
	`java-library`
	id("de.siphalor.tweed5.minecraft.mod.component")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.shadowJar {
	relocate("org.apache.commons", "de.siphalor.tweed5.shadowed.org.apache.commons")
}


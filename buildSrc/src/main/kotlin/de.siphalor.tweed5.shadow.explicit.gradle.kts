import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	alias(libs.plugins.shadow)
}

val shadowOnlyConfiguration = configurations.dependencyScope("shadowOnly")
val shadowOnlyElementsConfiguration = configurations.resolvable("shadowOnlyElements") {
	extendsFrom(shadowOnlyConfiguration.get())
}

tasks.named<ShadowJar>("shadowJar") {
	configurations = listOf(shadowOnlyElementsConfiguration.get())
}

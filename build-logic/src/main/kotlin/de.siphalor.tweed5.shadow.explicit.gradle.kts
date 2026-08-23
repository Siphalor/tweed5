import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("com.gradleup.shadow")
}

val shadowOnlyConfiguration = configurations.dependencyScope("shadowOnly")
val shadowOnlyElementsConfiguration = configurations.resolvable("shadowOnlyElements") {
	extendsFrom(shadowOnlyConfiguration.get())
}

tasks.named<ShadowJar>("shadowJar") {
	configurations = listOf(shadowOnlyElementsConfiguration.get())
}

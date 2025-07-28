import io.freefair.gradle.plugins.lombok.tasks.Delombok

plugins {
	java
	`java-library`
	alias(libs.plugins.lombok)
}

val expandedSourcesElements = configurations.consumable("expandedSourcesElements") {
	attributes {
		attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
	}
}

val delombok = tasks.getByName<Delombok>("delombok")

val sourcesJar by tasks.registering(Jar::class) {
	dependsOn(delombok)
	from(delombok.target)
	archiveClassifier.set("sources")
}
artifacts.add(expandedSourcesElements.name, sourcesJar)

tasks.named("build") { dependsOn(sourcesJar) }

components.named<AdhocComponentWithVariants>("java") {
	addVariantsFromConfiguration(expandedSourcesElements.get()) {}
}


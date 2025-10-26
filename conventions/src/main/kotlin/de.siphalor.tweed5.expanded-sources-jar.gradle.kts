import io.freefair.gradle.plugins.lombok.tasks.Delombok

plugins {
	java
	`java-library`
	id("io.freefair.lombok")
}

val expandedSourcesElements = configurations.consumable("expandedSourcesElements") {
	extendsFrom(configurations.implementation.get())
	attributes {
		attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
		attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
		attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
	}
}

val delombok = tasks.getByName<Delombok>("delombok")

val sourcesJar by tasks.registering(Jar::class) {
	group = LifecycleBasePlugin.BUILD_GROUP

	dependsOn(delombok)
	from(delombok.target)
	archiveClassifier.set("sources")
}
artifacts.add(expandedSourcesElements.name, sourcesJar)

tasks.named("build") { dependsOn(sourcesJar) }

components.named<AdhocComponentWithVariants>("java") {
	addVariantsFromConfiguration(expandedSourcesElements.get()) {}
}


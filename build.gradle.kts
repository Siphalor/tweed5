tasks.register("compileJava") {
	group = BasePlugin.BUILD_GROUP
	description = "Compile Java"

	dependsOn(gradle.includedBuilds.map { it.task(":compileJava") })
}

tasks.register("test") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Test all"

	dependsOn(gradle.includedBuilds.map { it.task(":test") })
}

tasks.register("build") {
	group = BasePlugin.BUILD_GROUP
	description = "Build and test"

	dependsOn(gradle.includedBuilds.map { it.task(":build") })
}

tasks.register("publish") {
	group = PublishingPlugin.PUBLISH_TASK_GROUP
	description = "Publish all"

	dependsOn(gradle.includedBuilds.map { it.task(":publish") })
}

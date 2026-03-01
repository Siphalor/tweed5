plugins {
	java
	jacoco
}

val testAgent = configurations.dependencyScope("mockitoAgent")
val testAgentClasspath = configurations.resolvable("testAgentClasspath") {
	isTransitive = false
	extendsFrom(testAgent.get())
}

dependencies {
	versionCatalogs.find("libs").ifPresent { libs ->
		testImplementation(platform(libs.findLibrary("junit.platform").get()))
		testImplementation(libs.findLibrary("junit.core").get())
		testRuntimeOnly(libs.findLibrary("junit.launcher").get())
		testImplementation(libs.findLibrary("mockito").get())
		testAgent(libs.findLibrary("mockito").get())
		testImplementation(libs.findLibrary("assertj").get())
	}
}

tasks.compileTestJava {
	versionCatalogs.find("libs").ifPresent { libs ->
		sourceCompatibility = libs.findVersion("java.test").get().requiredVersion
		targetCompatibility = libs.findVersion("java.test").get().requiredVersion
	}
}

tasks.test {
	val testAgentFiles = testAgentClasspath.map { it.files }
	doFirst {
		jvmArgs(testAgentFiles.get().map { file -> "-javaagent:${file.absolutePath}" })
	}
	dependsOn(testAgentClasspath)
	finalizedBy(tasks.jacocoTestReport)

	useJUnitPlatform()
	systemProperties(
		"junit.jupiter.execution.timeout.mode" to "disabled_on_debug",
		"junit.jupiter.execution.timeout.testable.method.default" to "10s",
		"junit.jupiter.execution.timeout.thread.mode.default" to "SEPARATE_THREAD",
	)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
}

plugins {
    java
    `java-library`
    `maven-publish`
    alias(libs.plugins.lombok)
    id("de.siphalor.tweed5.local-runtime-only")
    id("de.siphalor.tweed5.expanded-sources-jar")
}

group = rootProject.group
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.main.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.main.get())
}

repositories {
    mavenCentral()
}

val testAgent = configurations.dependencyScope("mockitoAgent")
val testAgentClasspath = configurations.resolvable("testAgentClasspath") {
    isTransitive = false
    extendsFrom(testAgent.get())
}

lombok {
    version = libs.versions.lombok.get()
}

dependencies {
    compileOnly(libs.autoservice.annotations)
    annotationProcessor(libs.autoservice.processor)
    testCompileOnly(libs.autoservice.annotations)
    testAnnotationProcessor(libs.autoservice.processor)

    compileOnly(libs.jetbrains.annotations)
    testImplementation(libs.jetbrains.annotations)
    compileOnly(libs.jspecify.annotations)
    testImplementation(libs.jspecify.annotations)

    implementation(libs.acl)
    "localRuntimeOnly"(libs.slf4j.rt)
	testImplementation(libs.acl)
    testImplementation(libs.slf4j.rt)

    testImplementation(platform(libs.junit.platform))
    testImplementation(libs.junit.core)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockito)
    testAgent(libs.mockito)
    testImplementation(libs.assertj)
    testImplementation(project(":test-utils"))
}

tasks.compileTestJava {
    sourceCompatibility = libs.versions.java.test.get()
    targetCompatibility = libs.versions.java.test.get()
}

tasks.test {
    dependsOn(testAgentClasspath)

    useJUnitPlatform()
    jvmArgs(testAgentClasspath.get().files.map { file -> "-javaagent:${file.absolutePath}" })
    systemProperties(
        "junit.jupiter.execution.timeout.mode" to "disabled_on_debug",
        "junit.jupiter.execution.timeout.testable.method.default" to "10s",
        "junit.jupiter.execution.timeout.thread.mode.default" to "SEPARATE_THREAD",
    )
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}


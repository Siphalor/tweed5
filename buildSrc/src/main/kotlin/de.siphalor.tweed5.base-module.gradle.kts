plugins {
    java
    `java-library`
    `maven-publish`
    id("de.siphalor.tweed5.local-runtime-only")
}

group = rootProject.group
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
}

repositories {
    mavenCentral()
}

val testAgent = configurations.dependencyScope("mockitoAgent")
val testAgentClasspath = configurations.resolvable("testAgentClasspath") {
    isTransitive = false
    extendsFrom(testAgent.get())
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    compileOnly(libs.autoservice.annotations)
    annotationProcessor(libs.autoservice.processor)
    testCompileOnly(libs.autoservice.annotations)
    testAnnotationProcessor(libs.autoservice.processor)

    implementation(libs.jetbrains.annotations)

    implementation(libs.slf4j.api)
    "localRuntimeOnly"(libs.slf4j.rt)
    testRuntimeOnly(libs.slf4j.rt)

    testImplementation(platform(libs.junit.platform))
    testImplementation(libs.junit.core)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.mockito)
    testAgent(libs.mockito)
    testImplementation(libs.assertj)
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
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}


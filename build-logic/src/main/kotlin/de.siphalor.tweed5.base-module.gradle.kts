plugins {
    java
    `java-library`
    id("io.freefair.lombok")
    id("de.siphalor.tweed5.unit-tests")
	id("de.siphalor.tweed5.publishing")
    id("de.siphalor.tweed5.local-runtime-only")
    id("de.siphalor.tweed5.expanded-sources-jar")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

    testImplementation(project(":generic-test-utils"))
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


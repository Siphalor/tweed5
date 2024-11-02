plugins {
    id("java")
    id("maven-publish")
    id("de.siphalor.tweed5.local-runtime-only")
}

group = "de.siphalor.tweed5"
version = properties["version"]!!

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "de.siphalor.tweed5.local-runtime-only")

    group = rootProject.group
    version = properties["version"]!!

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    sourceSets.test.get().runtimeClasspath += sourceSets.main.get().runtimeClasspath

        dependencies {
        val lombok = "org.projectlombok:lombok:${properties["lombok.version"]}"
        compileOnly(lombok)
        annotationProcessor(lombok)
        testCompileOnly(lombok)
        testAnnotationProcessor(lombok)

        val autoServiceAnnotations = "com.google.auto.service:auto-service-annotations:${properties["auto_service.version"]}"
        val autoService = "com.google.auto.service:auto-service:${properties["auto_service.version"]}"
        compileOnly(autoServiceAnnotations)
        annotationProcessor(autoService)
        testCompileOnly(autoServiceAnnotations)
        testAnnotationProcessor(autoService)

        implementation("org.jetbrains:annotations:${properties["jetbrains_annotations.version"]}")

        implementation("org.slf4j:slf4j-api:${properties["slf4j.version"]}")
        localRuntimeOnly("org.slf4j:slf4j-simple:${properties["slf4j.version"]}")

        testImplementation(platform("org.junit:junit-bom:${properties["junit.version"]}"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.mockito:mockito-core:${properties["mockito.version"]}")
        testImplementation("org.assertj:assertj-core:${properties["assertj.version"]}")
    }

    tasks.test {
        useJUnitPlatform()
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
}

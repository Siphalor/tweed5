plugins {
    id("java")
}

group = "de.siphalor"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        val lombok = "org.projectlombok:lombok:${properties["lombok.version"]}"
        compileOnly(lombok)
        annotationProcessor(lombok)
        testCompileOnly(lombok)
        testAnnotationProcessor(lombok)

        compileOnly("com.google.auto.service:auto-service-annotations:${properties["auto_service.version"]}")
        annotationProcessor("com.google.auto.service:auto-service:${properties["auto_service.version"]}")

        implementation("org.jetbrains:annotations:${properties["jetbrains_annotations.version"]}")

        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
        systemProperties(
            "junit.jupiter.execution.timeout.mode" to "disabled_on_debug",
            "junit.jupiter.execution.timeout.testable.method.default" to "10s",
            "junit.jupiter.execution.timeout.thread.mode.default" to "SEPARATE_THREAD",
        )
    }
}

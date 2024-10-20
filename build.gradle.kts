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

        val autoServiceAnnotations = "com.google.auto.service:auto-service-annotations:${properties["auto_service.version"]}"
        val autoService = "com.google.auto.service:auto-service:${properties["auto_service.version"]}"
        compileOnly(autoServiceAnnotations)
        annotationProcessor(autoService)
        testCompileOnly(autoServiceAnnotations)
        testAnnotationProcessor(autoService)

        implementation("org.jetbrains:annotations:${properties["jetbrains_annotations.version"]}")

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
}

import gradle.kotlin.dsl.accessors._182d53d78a136df48d95cf7411f9259f.lombok
import org.gradle.kotlin.dsl.assign

plugins {
	java
	alias(libs.plugins.lombok)
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":tweed5-serde-api"))
	implementation(platform(libs.junit.platform))
	implementation(libs.junit.core)
	implementation(libs.mockito)
	implementation(libs.assertj)
}

lombok {
	version = libs.versions.lombok.get()
}

java {
	sourceCompatibility = JavaVersion.toVersion(libs.versions.java.test.get())
	targetCompatibility = JavaVersion.toVersion(libs.versions.java.test.get())
}

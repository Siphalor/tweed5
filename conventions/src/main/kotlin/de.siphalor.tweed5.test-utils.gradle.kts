plugins {
	java
	id("io.freefair.lombok")
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

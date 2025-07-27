plugins {
	java
}

java {
	sourceCompatibility = JavaVersion.toVersion(libs.versions.java.test.get())
	targetCompatibility = JavaVersion.toVersion(libs.versions.java.test.get())
}

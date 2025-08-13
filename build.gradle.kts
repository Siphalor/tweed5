plugins {
	`jacoco-report-aggregation`
	`maven-publish`
}

group = "de.siphalor.tweed5"
version = properties["version"]!!

repositories {
	mavenCentral()
}

dependencies {
	rootProject.subprojects.forEach { subproject ->
		subproject.plugins.withId("jacoco") {
			jacocoAggregation(project(subproject.path))
		}
	}
}

reporting {
	reports {
		val aggregatedCoverageReport by creating(JacocoCoverageReport::class) {
			testSuiteName = "test"
		}
	}
}

plugins {
	`jacoco-report-aggregation`
	`maven-publish`
	id("de.siphalor.tweed5.root-properties")
}

group = "de.siphalor.tweed5"
version = project.property("tweed5.version").toString()

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

plugins {
	`test-report-aggregation`
	`jacoco-report-aggregation`
	`maven-publish`
	id("de.siphalor.tweed5.root-properties")
}

group = "de.siphalor.tweed5"
version = project.property("tweed5.version").toString()

configurations.aggregateTestReportResults {
	attributes {
		attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
		attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.TEST_RESULTS))
	}
}
configurations.aggregateCodeCoverageReportResults {
	attributes {
		attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
		attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.JACOCO_RESULTS))
	}
}

dependencies {
	rootProject.subprojects.forEach { subproject ->
		if (subproject.name.startsWith("tweed5-")) {
			testReportAggregation(project(subproject.path))
			jacocoAggregation(project(subproject.path))
		}
	}
}

reporting {
	reports {
		val aggregatedTestReport by creating(AggregateTestReport::class) {
			testSuiteName = "test"
		}
		val aggregatedCoverageReport by creating(JacocoCoverageReport::class) {
			testSuiteName = "test"
		}
	}
}

tasks.register("test") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Runs all tests"
	subprojects.mapNotNull { it.tasks.findByName("test") }.forEach { dependsOn(it) }
	finalizedBy(tasks.named("aggregatedTestReport"), tasks.named("aggregatedCoverageReport"))
}

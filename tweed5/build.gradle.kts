plugins {
	`test-report-aggregation`
	`jacoco-report-aggregation`
	id("de.siphalor.tweed5.root-properties")
	id("de.siphalor.tweed5.transitive-tasks")
}

group = "de.siphalor.tweed5"
version = rootProperties["tweed5.version"].get()

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
		create<AggregateTestReport>("aggregatedTestReport") {
			testSuiteName = "test"
		}
		create<JacocoCoverageReport>("aggregatedCoverageReport") {
			testSuiteName = "test"
		}
	}
}

tasks.named("test") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	description = "Runs all tests"
	finalizedBy(tasks.named("aggregatedTestReport"), tasks.named("aggregatedCoverageReport"))
}

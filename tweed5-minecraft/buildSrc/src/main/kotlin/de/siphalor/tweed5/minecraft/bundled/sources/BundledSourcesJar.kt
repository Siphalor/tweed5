package de.siphalor.tweed5.minecraft.bundled.sources

import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import javax.inject.Inject

abstract class BundledSourcesJar: Jar() {
	@get:InputFiles
	abstract val sources: ConfigurableFileCollection

	@get:Inject
	abstract val archiveOperations: ArchiveOperations

	@TaskAction
	override fun copy() {
		from(sources.filter { it.name.startsWith("tweed5") }.map { archiveOperations.zipTree(it) })
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		super.copy()
	}
}

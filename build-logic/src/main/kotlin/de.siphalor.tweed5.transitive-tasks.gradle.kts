listOf("compileJava", "assemble", "test", "build", "publish").forEach {
	tasks.register(it) {
		description = "Composite task for $this"
		dependsOn(getTasksByName(it, true).filter { t -> t != this })
	}
}

tasks.addRule("All subproject tasks are available as composite tasks running across all subprojects") {
	val subProjectTasks = getTasksByName(this, true)
	if (subProjectTasks.isNotEmpty()) {
		tasks.register(this) {
			description = "composite task for $this"
			dependsOn(subProjectTasks)
		}
	}
}

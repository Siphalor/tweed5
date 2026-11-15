plugins {
	id("de.siphalor.tweed5.root-properties")
	id("de.siphalor.tweed5.transitive-tasks")
}

group = "de.siphalor.tweed5.minecraft"
version = rootProperties["tweed5.version"].get()

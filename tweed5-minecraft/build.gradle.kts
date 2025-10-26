plugins {
	`maven-publish`
	id("de.siphalor.tweed5.root-properties")
}

group = "de.siphalor.tweed5.minecraft"
version = project.property("tweed5.version").toString()

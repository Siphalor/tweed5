plugins {
	`maven-publish`
	id("de.siphalor.tweed5.publishing")
	id("de.siphalor.tweed5.minecraft.mod.base")
}

publishing {
	publications {
		create<MavenPublication>("minecraftMod") {
			val projectGroup = project.group.toString()
			groupId = if (projectGroup.endsWith(".minecraft")) projectGroup else "$projectGroup.minecraft"
			artifactId = project.name
			version = project.version.toString()

			from(components["minecraftMod"])
		}
	}
}


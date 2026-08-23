plugins {
	`java-library`
	`maven-publish`
	id("de.siphalor.tweed5.root-properties")
	id("de.siphalor.tweed5.module-info")
}

group = rootProject.group
version = rootProject.version

publishing {
	repositories {
		if (project.hasProperty("siphalor.maven.user")) {
			maven {
				name = "Siphalor"
				url = uri("https://maven.siphalor.de/upload.php")
				credentials {
					username = project.property("siphalor.maven.user") as String
					password = project.property("siphalor.maven.password") as String
				}
			}
		}
	}

	publications.all {
		if (this is MavenPublication) {
			pom {
				name = moduleInfo.name
				description = moduleInfo.description
				url = rootProperties["git.url"]
				scm {
					url = rootProperties["git.url"]
				}
			}
		}
	}
}

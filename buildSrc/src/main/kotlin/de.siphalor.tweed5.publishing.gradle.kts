plugins {
	`java-library`
	`maven-publish`
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
				name = project.property("module.name") as String
				description = project.property("module.description") as String
				url = project.property("git.url") as String
				scm {
					url = project.property("git.url") as String
				}
			}
		}
	}
}

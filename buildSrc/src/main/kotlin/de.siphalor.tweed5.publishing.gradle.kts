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
				url.set("https://github.com/Siphalor/tweed-5")
				scm {
					url.set("https://github.com/Siphalor/tweed-5")
				}
			}
		}
	}
}

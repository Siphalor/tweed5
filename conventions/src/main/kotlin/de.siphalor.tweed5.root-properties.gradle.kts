import java.util.Properties

val rootPropertiesFile = project.layout.settingsDirectory.file("../gradle.properties").asFile
if (rootPropertiesFile.exists()) {
	Properties()
		.apply { rootPropertiesFile.inputStream().use { load(it) } }
		.forEach { (key, value) -> project.ext.set(key.toString(), value.toString()) }
}

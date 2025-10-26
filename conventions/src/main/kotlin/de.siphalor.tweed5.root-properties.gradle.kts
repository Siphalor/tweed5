import java.util.Properties

val rootProperties = Properties()
project.layout.settingsDirectory.file("../gradle.properties").asFile.inputStream().use { rootProperties.load(it) }
rootProperties.forEach { (key, value) -> project.ext.set(key.toString(), value.toString()) }

plugins {
    java
}

val localRuntimeOnly = configurations.create("localRuntimeOnly")
sourceSets.main.get().runtimeClasspath += localRuntimeOnly

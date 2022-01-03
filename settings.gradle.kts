pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "1.6.10"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "Sonatype Nexus Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}

rootProject.name = "jackson-module-kotlin"

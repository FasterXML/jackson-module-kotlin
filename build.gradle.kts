import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
}

group = "com.fasterxml.jackson.module"
version = "2.13.1-SNAPSHOT"

dependencies {
    implementation(kotlin("reflect"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:$version"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test-junit"))
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
}

val generatedSources = file("target/generated-sources")
sourceSets {
    main {
        java.srcDirs(generatedSources)
    }
}

val compatibilityTestEnabled =
    providers
        .gradleProperty("compatibilityTest")
        .forUseAtConfigurationTime() // TODO: Remove `forUseAtConfigurationTime` when updating to Gradle 7.4
        .map { it != "false" }
        .getOrElse(false)

if (compatibilityTestEnabled) {
    tasks.withType<KotlinCompile>() {
        kotlinOptions {
            apiVersion = "1.3"
            languageVersion = "1.3"
            jvmTarget = "1.8"
        }
    }
} else {
    kotlin {
        sourceSets {
            main {
                kotlin.srcDir(file("src/kotlin-1.5/kotlin"))
            }
            test {
                kotlin.srcDir(file("src/testKotlin-1.5/kotlin"))
            }
        }
    }
}

val packageVersionGenerate by tasks.registering(Exec::class) {
    description = "Generate PackageVersion.java file using Maven"

    inputs.dir("src/main/java")
    outputs.dir(generatedSources)
    if (OperatingSystem.current().isWindows) {
        commandLine("mvnw.cmd", "generate-sources")
    } else {
        commandLine("./mvnw", "generate-sources")
    }
}

tasks {
    compileJava.configure {
        dependsOn(packageVersionGenerate)
    }
}

import org.gradle.internal.os.OperatingSystem

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

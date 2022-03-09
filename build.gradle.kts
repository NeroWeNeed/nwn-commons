@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform") version "1.6.10" apply false
    `maven-publish`
}
allprojects {
    group = "github.nwn"
    version = "1.0.2-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    repositories {
        mavenCentral()
    }
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/${System.getenv("GithubUsername")}/nwn-commons")
                credentials {
                    username = System.getenv("GithubUsername")
                    password = System.getenv("GithubToken")
                }

            }
        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["kotlin"])
            }
        }
    }
}
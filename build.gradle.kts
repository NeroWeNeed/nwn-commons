@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform") version "1.7.21" apply false
    kotlin("plugin.serialization") version "1.7.21" apply false
    `maven-publish`
    kotlin("kapt") version "1.6.10" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
}
allprojects {
    group = "github.nwn"
    version = "1.0.3-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.jetbrains.kotlin.multiplatform")

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
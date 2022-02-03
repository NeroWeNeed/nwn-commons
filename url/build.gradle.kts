plugins {
    kotlin("multiplatform")
    `maven-publish`
}



repositories {
    mavenCentral()
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {

                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}
tasks.register<CodeGenerationTask>("urlFactoryCodeGenerator") {
    generator = URLFactoryGenerator::class.qualifiedName!!
    className = "DefaultUrlFactory"
    classPackage = "github.nwn.commons"
    sourceSet = "commonMain"

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
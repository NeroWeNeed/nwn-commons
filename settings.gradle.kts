rootProject.name = "nwn-commons"

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/${System.getenv("GithubUsername")}/nwn-commons")
            credentials {
                username = System.getenv("GithubUsername")
                password = System.getenv("GithubToken")
            }
        }
    }
}


include("url")
include("uuid")
include("mime")
include("templater")
include("templater:gradle-plugin")
findProject(":templater:gradle-plugin")?.name = "gradle-plugin"
include("templater:kotlin-plugin")
findProject(":templater:kotlin-plugin")?.name = "kotlin-plugin"
include("templater:api")
findProject(":templater:api")?.name = "api"
include("localization")

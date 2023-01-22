buildscript {
    extra["kotlin_plugin_id"] = "github.nwn.commons.templater-plugin"
}
plugins {
    kotlin("multiplatform")
}
kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlin.graph)
                implementation(project(":templater:api"))
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
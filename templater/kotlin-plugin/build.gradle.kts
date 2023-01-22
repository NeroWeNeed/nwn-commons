@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    kotlin("multiplatform")
    id("com.github.gmazzo.buildconfig")
    alias(libs.plugins.ksp)
}

group = "github.nwn"
version = "1.0.3-SNAPSHOT"

kotlin {
    jvm() {
        withJava()
    }
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
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                compileOnly(kotlin("compiler-embeddable"))
                compileOnly(libs.autoservice.annotations)
                configurations["ksp"].dependencies.add(libs.autoservice.processor.get())
            }
        }
    }
}
buildConfig {
    val project = project(":templater:kotlin-plugin")
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${project.parent!!.extra["kotlin_plugin_id"]}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${project.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${project.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${project.version}\"")
}
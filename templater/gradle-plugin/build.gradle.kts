plugins {
    kotlin("multiplatform")
    id("java-gradle-plugin")
    id("com.github.gmazzo.buildconfig")
}

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
                implementation(kotlin("gradle-plugin-api"))
            }
        }
    }
}
gradlePlugin {
    plugins {
        create("templaterGradlePlugin") {
            id = "templater.plugin"
            implementationClass = "github.nwn.commons.templater.TemplaterGradlePlugin"
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
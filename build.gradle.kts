@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform") version libs.versions.kotlin.get() apply false
}
allprojects {
    group = "github.nwn"
    version = "1.0-SNAPSHOT"
}
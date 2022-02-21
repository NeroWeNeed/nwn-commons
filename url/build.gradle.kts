plugins {
    kotlin("multiplatform")
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
rootProject.name = "nwn-commons"



enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.6.10")
            alias("kotlinx-datetime").to("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
        }
    }
}
include("url")
include("uuid")
include("mime")

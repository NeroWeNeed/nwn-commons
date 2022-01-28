rootProject.name = "kotlin-commons"



enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin","1.6.10")

        }
    }
}
include("url")
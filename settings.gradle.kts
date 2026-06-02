pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "CineVerse"

// Shared KMP modules
include(":shared")
include(":shared:core:designsystem")
include(":shared:core:database")
include(":shared:core:network")
include(":shared:core:analytics")
include(":shared:data")
include(":shared:domain")
include(":shared:presentation")

// Android platform multi-module layer
include(":androidApp:app")
include(":androidApp:core:ui")
include(":androidApp:features:home")
include(":androidApp:features:search")
include(":androidApp:features:details")
include(":androidApp:features:profile")
include(":androidApp:features:watchlist")
include(":androidApp:features:dice")

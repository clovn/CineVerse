plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

android {
    namespace = "com.cineverse.android"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.cineverse.android"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":shared:core:designsystem"))
    
    // Multi-module Android platform components
    implementation(project(":androidApp:core:ui"))
    implementation(project(":androidApp:features:home"))
    implementation(project(":androidApp:features:search"))
    implementation(project(":androidApp:features:details"))
    implementation(project(":androidApp:features:profile"))
    implementation(project(":androidApp:features:watchlist"))
    implementation(project(":androidApp:features:dice"))
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    
    // Jetpack Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(libs.image.loader)
    
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

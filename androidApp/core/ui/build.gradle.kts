plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.cineverse.android.core.ui"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    api(project(":shared:core:designsystem"))
    api(project(":shared:presentation"))
    
    api(libs.androidx.activity.compose)
    api(libs.koin.compose)
    api(libs.image.loader)
    api("androidx.core:core-ktx:1.12.0")
    
    api(compose.runtime)
    api(compose.foundation)
    api(compose.material3)
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

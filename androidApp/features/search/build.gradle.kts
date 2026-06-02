plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cineverse.android.features.search"
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
    implementation(project(":androidApp:core:ui"))
    implementation(project(":shared:core:designsystem"))
    implementation(project(":shared:presentation"))
    
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

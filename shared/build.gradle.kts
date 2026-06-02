plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.skie)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
            
            // Export APIs to make them visible to Swift in Xcode
            export(project(":shared:domain"))
            export(project(":shared:presentation"))
            export(project(":shared:core:analytics"))
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":shared:domain"))
                api(project(":shared:presentation"))
                api(project(":shared:core:analytics"))
                
                implementation(project(":shared:core:designsystem"))
                api(project(":shared:core:database"))
                api(project(":shared:core:network"))
                implementation(project(":shared:data"))
                
                api(libs.koin.core)
                implementation(libs.ktor.client.core)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.koin.android)
            }
        }
    }
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "com.cineverse.shared"
    compileSdk = 34
    defaultConfig {
        minSdk = 28
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

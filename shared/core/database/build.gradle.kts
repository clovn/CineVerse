plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
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

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                api(libs.sqldelight.runtime)
                api(libs.sqldelight.coroutines)
                implementation(libs.coroutines.core)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.sqldelight.android)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.sqldelight.native)
            }
        }
    }
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "com.cineverse.core.database"
    compileSdk = 34
    defaultConfig {
        minSdk = 28
    }
}

sqldelight {
    databases {
        create("CineVerseDatabase") {
            packageName.set("com.cineverse.core.database")
        }
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}

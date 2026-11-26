plugins {
    id("com.android.library") version "8.7.2"
    id("org.jetbrains.kotlin.android") version "1.9.0"
}

android {
    compileSdk = 35
    defaultConfig {
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    sourceSets {
        main {
            kotlin {
                srcDirs = ["src/main/kotlin"]
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":toix-core"))
    implementation(project(":toix-editor"))
}
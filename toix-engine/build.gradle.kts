plugins {
    // Core engine modules will be plain Kotlin JVM libraries.
    // toix-runtime-android will apply com.android.library later.
    id("org.jetbrains.kotlin.jvm") version "2.1.10" apply false
    id("com.android.library") version "8.7.2" apply false
}

subprojects {
    group = "com.toix"
    version = "0.1.0"
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
}

dependencies {
    implementation(project(":toix-core"))
    implementation(project(":toix-editor"))
}
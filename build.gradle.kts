plugins {
    id("com.android.application") version "8.8.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
buildscript {
    repositories {
        google()
    }
    dependencies {
        val nav_version = "2.8.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath("com.android.tools.build:gradle:8.8.1")
    }
}
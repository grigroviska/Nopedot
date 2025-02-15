plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.grigroviska.nopedot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.grigroviska.nopedot"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
}

dependencies {

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.3")
    val lifecycle_version = "2.8.7"
    val room_version = "2.6.1"
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // RecyclerView Animator
    implementation("jp.wasabeef:recyclerview-animators:4.0.2")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")


    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")

    // Annotation processor
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.7")

    // Gson
    implementation("com.google.code.gson:gson:2.12.1")


    //Room
    implementation("androidx.room:room-runtime:$room_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")
    ksp("androidx.room:room-compiler:$room_version")

    //Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")

    // Navigation Components
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.7")

    //material Components
    implementation("com.google.android.material:material:1.12.0")

    //color picker library
    implementation("com.thebluealliance:spectrum:0.7.1")

    implementation("io.github.yahiaangelo.markdownedittext:markdownedittext:1.1.3")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("io.noties.markwon:ext-tasklist:4.6.2")

    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    val arch_version = "2.8.7"

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$arch_version")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$arch_version")
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Room: needed for annotation processing (KSP)
    id("com.google.devtools.ksp")
    // Firebase: applies the google-services.json config
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.a220945_kasthoori_nelson_project2"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.a220945_kasthoori_nelson_project2"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- EXISTING ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation + ViewModel (already added)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // --- PROJECT 2: ROOM (Local Database) ---
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")      // Coroutine support for Room
    ksp("androidx.room:room-compiler:2.7.1")            // Code generator (needs KSP plugin above)

    // --- PROJECT 2: FIREBASE (Cloud / Firestore) ---
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")

    // --- PROJECT 2: RETROFIT (REST API / Internet Data) ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // Parses JSON automatically
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Logs API calls for debugging

    // --- PROJECT 2: GPS / LOCATION (Sensor) ---
    implementation("com.google.android.gms:play-services-location:21.3.0") // Fused Location Provider

    // --- PROJECT 2: COROUTINES (needed for Room + Retrofit + Location async calls) ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1") // .await() on Firebase Tasks

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
}
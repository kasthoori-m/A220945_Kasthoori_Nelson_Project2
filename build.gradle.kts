// Top-level build file — plugin versions declared here, applied per-module above
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Room: KSP (Kotlin Symbol Processing) — annotation processor for Room @Entity, @Dao, @Database
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false

    // Firebase: reads google-services.json and wires Firebase SDKs
    id("com.google.gms.google-services") version "4.4.2" apply false
}
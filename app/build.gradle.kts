plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.0"

}

android {
    namespace = "com.example.supabaseauth"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.supabaseauth"
        minSdk = 29
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

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Core
    implementation("androidx.core:core-ktx:1.13.1")

    // Compose BOM (WAJIB)
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
    implementation("io.github.jan-tennert.supabase:auth-kt")

    // Ktor (network)
    implementation("io.ktor:ktor-client-android:3.0.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.android.material:material:1.11.0")

    // Supabase Database / PostgREST
    implementation("io.github.jan-tennert.supabase:postgrest-kt")

    // Supabase Realtime
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Ktor Client Android
    implementation("io.ktor:ktor-client-android:3.0.3")

    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-websockets:2.3.12")
}
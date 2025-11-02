import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" // For Room
}

// Securely load secrets.properties
val secretsProperties = Properties()
val secretsFile = rootProject.file("secrets.properties")
if (secretsFile.exists()) {
    FileInputStream(secretsFile).use { stream ->
        secretsProperties.load(stream)
    }
}

android {
    namespace = "com.fitnessapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fitnessapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Inject Google Maps API key
        buildConfigField(
            "String",
            "MAPS_API_KEY",
            "\"${secretsProperties["MAPS_API_KEY"] ?: ""}\""
        )

        manifestPlaceholders["MAPS_API_KEY"] = secretsProperties["MAPS_API_KEY"] ?: ""
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android + UI
    implementation(libs.androidx.core.ktx.v1131)
    implementation(libs.androidx.appcompat.v170)
    implementation(libs.material.v1120)
    implementation(libs.androidx.constraintlayout.v220)
    implementation(libs.androidx.cardview)

    // Room Database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Google Maps + Places
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)
}

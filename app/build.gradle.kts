plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    // Plugins for Hilt
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

    // Secrets Gradle Plugin
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "ca.uqac.inf865.truestay"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.uqac.inf865.truestay"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "USE_EMULATORS", "false")
            buildConfigField("String", "EMULATOR_HOST", "\"10.0.2.2\"")
            // Use "localhost" for physical devices connected to the machine running the emulators
        }
        release {
            buildConfigField("Boolean", "USE_EMULATORS", "false")
            buildConfigField("String", "EMULATOR_HOST", "\"\"")

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
        compose = true
        buildConfig = true
    }
}

dependencies {
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

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    // Add the dependencies for the Firebase products used in the app
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)

    // Hilt dependencies
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.hilt.compiler)

    // Coil for image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Maps
    implementation(libs.maps.compose)
    //implementation(libs.play.services.maps)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
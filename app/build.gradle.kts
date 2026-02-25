plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.dagger.hilt.android")   // applied here (no version)
    id("com.google.devtools.ksp")          // applied here (no version)
}

android {
    namespace = "com.example.scanner"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.scanner"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "DB_URL", "\"jdbc:jtds:sqlserver://192.168.2.3:1433/Daten_Hemme_Schmargendorf_251221;ssl=require\"")
            buildConfigField("String", "DB_USER", "\"daten_user\"")
            buildConfigField("String", "DB_PASSWORD", "\"\$Axyzwert123\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "DB_URL", "\"jdbc:jtds:sqlserver://192.168.2.3:1433/Daten_Hemme_Schmargendorf_251221;ssl=require\"")
            buildConfigField("String", "DB_USER", "\"daten_user\"")
            buildConfigField("String", "DB_PASSWORD", "\"\$Axyzwert123\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation(libs.androidx.compose.material.icons)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room (KSP)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Keyence SDK
    implementation(files("libs/keyence_sdk.aar"))
    implementation(files("libs/keyence_task_sdk.aar"))

    // jtds is very old, but works. the modern jdbc driver for mssql doesn't work
    implementation(files("libs/jtds-1.3.1.jar"))

    // tests & debug
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
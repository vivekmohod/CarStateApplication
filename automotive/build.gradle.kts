

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.carstateapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.carstateapplication"
        minSdk = 28
        //noinspection OldTargetApi
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
    useLibrary ("android.car")
}



dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.layout.desktop)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.bom)
    implementation (libs.firebase.messaging)
    implementation (libs.firebase.database)

}


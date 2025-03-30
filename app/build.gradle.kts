plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.demoilost"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.demoilost"
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

    buildFeatures {
        viewBinding = true
    }
}



dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.material.v130alpha01)
    implementation(libs.material.vversion)
    implementation(libs.play.services.maps)
    implementation(libs.fragment)  // package for creating a fragment (bottom_sheet)
    implementation(libs.play.services.maps.v1900)
    // Glide for loading images
    implementation(libs.glide)
    implementation(libs.places)
    annotationProcessor(libs.compiler)
    implementation (libs.okhttp)
    implementation (libs.commons.io)
    androidTestImplementation(libs.espresso.intents)
}

configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:3.21.7")
    }
}

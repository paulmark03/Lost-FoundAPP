import java.util.Properties
import java.io.FileInputStream


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    id("org.sonarqube")
    id("jacoco")
}

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}
val apiKey = localProperties.getProperty("google.places.api.key") ?: ""

android {
    namespace = "com.example.demoilost"
    compileSdk = 35
    android.buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "com.example.demoilost"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "PLACES_API_KEY", "\"${apiKey}\"")
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
    // Core Android libraries
    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)

    // Material Design
    implementation(libs.material)
    implementation(libs.material.v130alpha01)
    implementation(libs.material.vversion)

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Google Services
    implementation(libs.play.services.maps)
    implementation(libs.play.services.maps.v1900)
    implementation(libs.places)

    // Image loading
    implementation(libs.glide)

    // Networking / Utilities
    implementation(libs.okhttp)
    implementation(libs.commons.io)
    annotationProcessor(libs.compiler)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)

    // Android Instrumentation Testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
}



tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates Jacoco coverage report for the Debug build."

    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)  // Needed by SonarCloud
        html.required.set(true) // Optional, nice to have
    }

    val debugTree = fileTree("${buildDir}/intermediates/javac/debug") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*"
        )
    }

    classDirectories.setFrom(files(debugTree))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(files("${buildDir}/jacoco/testDebugUnitTest.exec"))
}




sonarqube {
    properties {
        property("sonar.projectKey", "paulmark03_Lost-FoundAPP")
        property("sonar.organization", "paulmark03")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}


tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}


configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:3.21.7")
    }
}
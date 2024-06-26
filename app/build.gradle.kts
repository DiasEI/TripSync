plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
}

android {
    namespace = "com.example.tripsync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tripsync"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.picasso)
    implementation(libs.androidx.work.runtime.ktx)
    // Retrofit dependencies
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // UI dependencies
    implementation(libs.androidx.ui.text.android)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.maps)
    implementation(libs.common)
    implementation(libs.places)
    // JUnit dependencies
    implementation(libs.androidx.junit.ktx)
    // Unit testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.androidx.junit.v120)
    testImplementation(libs.androidx.rules)
    // AndroidX Fragment testing dependencies
    debugImplementation(libs.androidx.fragment.testing)
    // Android instrumented testing dependencies
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.uiautomator)
    // Image loading dependencies
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    // HTTP logging dependencies
    implementation(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)
}

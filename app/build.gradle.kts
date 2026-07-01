plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.todolist"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.todolist"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

// KHẮC PHỤC TRIỆT ĐỂ LỖI JVM TARGET
kotlin {
    jvmToolchain(17)
}

dependencies {
    // 1. Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // 2. Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // 3. Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // 4. Hilt (Dùng KAPT)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // 5. DataStore
    implementation(libs.datastore.preferences)

    // 6. Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // 7. Lottie Animation
    implementation(libs.lottie.compose)

    // 8. Navigation
    implementation(libs.androidx.navigation.compose)
    // 9. THÊM THƯ VIỆN RUNTIME CHO DIỄN DỊCH ĐỐI TƯỢNG (SERIALIZATION)
    implementation(libs.kotlinx.serialization.json)
}


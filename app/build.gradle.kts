plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.productexpirationtrackerapp"

    // Upgrade compileSdk to 36 for library compatibility
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.productexpirationtrackerapp"
        minSdk = 23           // keep minSdk for Android 11+
        targetSdk = 36       // upgrade targetSdk
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.work:work-runtime:2.9.0")

    // Add Room database for Java
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // Glide for image loading - ADD THIS
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // ===== ML KIT DEPENDENCIES FOR PRODUCT SCANNER =====
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ML Kit Text Recognition (OCR)
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // CameraX for camera handling
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("androidx.camera:camera-mlkit-vision:1.4.2")
}
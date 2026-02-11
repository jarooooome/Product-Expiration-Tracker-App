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
        minSdk = 30          // keep minSdk for Android 11+
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

    // Add Room database for Java
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // Material Components
    implementation("com.google.android.material:material:1.11.0")
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
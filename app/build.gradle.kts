plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.productexpirationtrackerapp"

    // FIXED: Correct compileSdk syntax
    compileSdk = 34  // Changed from: compileSdk { version = release(36) }

    defaultConfig {
        applicationId = "com.example.productexpirationtrackerapp"
        minSdk = 30          // keep minSdk for Android 11+
        targetSdk = 34       // Use 34 instead of 36 (36 might be too new)
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
    // Core Android
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CoordinatorLayout
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Room Database - THIS LOOKS CORRECT
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Optional: If you want to use Room with Kotlin coroutines
    // implementation("androidx.room:room-ktx:$roomVersion")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
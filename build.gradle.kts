
        plugins {
            id("com.android.application")
            id("org.jetbrains.kotlin.android")
            id("kotlin-kapt") // For Room annotations
        }

android {
    namespace = "uk.ac.tees.mad.E4294395"
    compileSdk = 34

    defaultConfig {
        applicationId = "uk.ac.tees.mad.E4294395"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true // Enable Jetpack Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0" // Compose compiler version
    }
}

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    // Jetpack Compose for UI
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation ("androidx.compose.material3:material3:1.2.0-alpha04")
    implementation ("com.google.android.material:material:1.11.0-alpha01")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")
    // Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.7.0")
    // Room for local database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    // Location services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Coroutines for asynchronous tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Sensor support
    implementation("androidx.core:core:1.12.0")
    // Testing dependencies
    // Remove these lines
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    testImplementation("junit:junit:4.13.2")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation ("androidx.compose.material3:material3:1.1.1")
    implementation ("com.google.maps.android:maps-compose:2.11.0")
}
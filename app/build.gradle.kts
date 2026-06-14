plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.ikoro.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ikoro.android"
        minSdk = 26  // API 26 for proper BLE support
        targetSdk = 34
        versionCode = 4
        versionName = "0.4.0"

        val breezApiKey: String = project.findProperty("breezApiKey") as? String
            ?: project.properties["local.properties.breezApiKey"] as? String
            ?: ""
        buildConfigField("String", "BREEZ_API_KEY", "\"$breezApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
        }
        debug {
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            }
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
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // AppCompat for theme support
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    
    // Biometric
    implementation("androidx.biometric:biometric:1.1.0")
    
    // LiveKit SDK for calls
    implementation("io.livekit:livekit-android:2.12.1")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Breez SDK - Liquid (BTC + stablecoins)
    implementation("breez_sdk_liquid:bindings-android:0.10.0")
    
    // Cryptography + Bitcoin wallet (includes BIP-39/32)
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.google.crypto.tink:tink-android:1.10.0")
    implementation("org.bitcoinj:bitcoinj-core:0.16.2") {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
    }
    
    // QR generation
    implementation("com.google.zxing:core:3.5.3")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // WebSocket / HTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Bluetooth
    implementation("no.nordicsemi.android:ble:2.6.1")
    
    // Compression
    implementation("org.lz4:lz4-java:1.8.0")
    
    // Security preferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Room for local persistence (no KAPT/Hilt)
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

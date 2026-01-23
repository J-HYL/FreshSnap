plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)

    alias(libs.plugins.hilt.android)

    alias(libs.plugins.google.services)
}

android {
    namespace = "com.marujho.freshsnap"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.marujho.freshsnap"
        minSdk = 28
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //El conjunto de CameraX
    implementation(libs.bundles.camerax)
    //MLKIT de google para Scanear Codigos de barras y texto
    implementation(libs.play.services.mlkit.barcode)
    implementation(libs.play.services.mlkit.text)

    /*OPEN FOOD FACTS*/

    // Retrofit
    implementation(libs.retrofit.v290)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Convertidor Moshi para Retrofit
    implementation(libs.converter.moshi)

    /*Logs JSON*/
    implementation(libs.logging.interceptor)




    // ROOM (BASE DE DATOS)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // HILT
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // HILT + NAVIGATION
    implementation(libs.androidx.hilt.navigation.compose)

    //El conjunto de CameraX
    implementation(libs.bundles.camerax)
    //MLKIT de google para Scanear Codigos de barras y texto
    implementation(libs.play.services.mlkit.barcode)
    implementation(libs.play.services.mlkit.text)

    /*OPEN FOOD FACTS*/

    // Retrofit
    implementation(libs.retrofit.v290)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Convertidor Moshi para Retrofit
    implementation(libs.converter.moshi)

    /*Logs JSON*/
    implementation(libs.logging.interceptor)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)      // Para Login
    implementation(libs.firebase.firestore) // Base de datos en la nube
}
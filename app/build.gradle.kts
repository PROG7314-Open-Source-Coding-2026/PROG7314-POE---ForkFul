plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.emeris.forkful"
    //API 37 SDK config
    compileSdk = 37

    defaultConfig {
        applicationId = "com.emeris.forkful"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //Backend connection config
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"https://nabhfcedqdweiqairwve.supabase.co\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hYmhmY2VkcWR3ZWlxYWlyd3ZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAwNTE3NjYsImV4cCI6MjEwNTYyNzc2Nn0.YLiJNUpk4XUsbfAJzFkZ5tRYcf1Zn9TkrkP4B23ZqCA\""
        )
        //Google SSO client ID
        buildConfigField(
            "String",
            "GOOGLE_SERVER_CLIENT_ID",
            "\"906806038757-cvbkcsh3kel9ti7uq62jsco1t399h4hs.apps.googleusercontent.com\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //Nav & icons
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.material:material-icons-extended")

    //Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    //REST client
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    //Google SSO
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.id)

    //Session & prefs storage
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    //Unit test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

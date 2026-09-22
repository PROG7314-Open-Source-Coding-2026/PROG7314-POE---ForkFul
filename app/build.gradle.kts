plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.emeris.forkful"
    // API 37 platform is distributed via the canary SDK channel
    // (sdkmanager --channel=3 "platforms;android-37.0"). The 2026-era
    // androidx libraries (lifecycle 2.11, core-ktx 1.19) require it.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.emeris.forkful"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ---------------------------------------------------------------------------
        // Backend connection settings (see SETUP.md step 3).
        // Replace SUPABASE_URL with your Supabase project URL after running the
        // SQL migration and deploying the Edge Functions, e.g.
        //   "https://abcd1234.supabase.co"
        // The anon key is safe to embed in the client (protected by RLS + JWT
        // verification inside every Edge Function).
        // ---------------------------------------------------------------------------
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
        // Web Client ID of the OAuth 2.0 "Web application" client created in
        // Google Cloud Console (also configured in Supabase Google auth).
        // See SETUP.md step 4.
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

    // Navigation and extended icon components
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.material:material-icons-extended")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // REST client (custom Supabase Edge Function API - Planning & Design section 5)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Google SSO via Android Credential Manager (FR-01)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.google.id)

    // Session + preferences persistence (NFR-04: token never stored in plaintext)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // Unit testing
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

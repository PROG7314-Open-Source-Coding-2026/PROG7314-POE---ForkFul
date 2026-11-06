# 🍴 Forkful — Portfolio of Evidence (Final POE)

> **Turn what you have into what you crave.**  
> An intelligent recipe discovery, pantry management, and offline-resilient kitchen assistant built natively for Android.

[![Android CI](https://github.com/PROG7314-Open-Source-Coding-2026/PROG7314-POE---ForkFul/actions/workflows/android-ci.yml/badge.svg)](https://github.com/PROG7314-Open-Source-Coding-2026/PROG7314-POE---ForkFul/actions/workflows/android-ci.yml)
![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026--37)-brightgreen)
![Language](https://img.shields.io/badge/Language-Kotlin%202.2.10-blue)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-purple)
![Backend](https://img.shields.io/badge/Backend-Supabase%20(PostgreSQL%20%2B%20Edge%20Functions)-emerald)
![FCM](https://img.shields.io/badge/Push%20Notifications-Firebase%20Cloud%20Messaging-orange)
![Offline](https://img.shields.io/badge/Offline%20Storage-RoomDB%20%2B%20SQLite-indigo)
![Biometrics](https://img.shields.io/badge/Biometrics-AndroidX%20BiometricPrompt-teal)
![Locales](https://img.shields.io/badge/Multi--Language-English%20%7C%20isiZulu%20%7C%20Afrikaans-yellow)

---

## 🎥 Comprehensive Demonstration Video

* **Part 3 Video Walkthrough Link**: https://youtu.be/zNACuQhb-tI
* **Device Target**: Demonstrated natively on a physical Android mobile device.

### Video Verification Points Breakdown
As prescribed in the Final POE Assessment brief, the demonstration video showcases the following verification points with an explanatory voice-over narrative:
1. **Authentication Flows**: User registration and login utilising Single Sign-On (Google SSO via Credential Manager) alongside native Biometric verification (`BiometricPrompt`).
2. **Application Settings**: Modifying and saving preferences (theme, dietary restrictions, notifications, and real-time South African multi-language switching).
3. **Data Pipeline Sync**: Real-time HTTP communication with custom cloud-hosted REST APIs (Deno Edge Functions) backed by PostgreSQL.
4. **Offline Architecture**: Simulating network disconnection (Airplane Mode), performing ingredient stock actions offline, and verifying automatic two-way data synchronisation upon reconnection.
5. **Cloud and DB Infrastructure**: Real-time database adjustments reflected across Supabase auth, storage, and relational tables.
6. **Core Deliverables**: Interactive Swipe Deck, Cooking Mode with step-by-step guidance, Aisle-Organised Grocery Basket with WhatsApp sharing, and Smart Pantry Matching.

---


## 👥 Development Team & Contributions

Forkful was designed, architected, and developed collaboratively by a group of four Software Engineering students:

| Student Name | Student Number | Role | Key Contributions |
| :--- | :--- | :--- | :--- |
| **Jamie Abrahams** | **ST10434047** | Lead Software Engineer | Application architecture, MVVM state flows, Supabase integration, and biometric security |
| **Isabelle Devlin** | **ST10445500** | Software Engineer | Feature development, theme tokens, design system, and user experience |
| **Justin Fussell** | **ST10280758** | Software Engineer | Database schema design, Deno Edge Functions, and backend API services |
| **Matteo Nusca** | **ST10440432** | Software Engineer | UI design system, Jetpack Compose screens, and navigation architecture |

---


## 🚀 Advanced POE Technical Capabilities

### 1. 🔐 Biometric Authentication Engine
Forkful implements native biometric security utilising the modern AndroidX Biometric API:
* **Architecture**: [`BiometricHelper.kt`](app/src/main/java/com/emeris/forkful/core/security/BiometricHelper.kt) inspects hardware readiness via `BiometricManager` (`READY`, `NO_HARDWARE`, `NOT_ENROLLED`, `UNAVAILABLE`).
* **Multi-Factor / Fallback**: Uses `BIOMETRIC_STRONG or DEVICE_CREDENTIAL` to allow fingerprint, 3D facial recognition, or device PIN/pattern as secure backup.
* **Cold Launch Gating**: When enabled in user preferences, the app immediately launches into [`AppLockScreen.kt`](app/src/main/java/com/emeris/forkful/ui/lock/AppLockScreen.kt) prior to displaying any private kitchen or recipe data.
* **Background Resume Re-lock**: Monitored via `LifecycleEventObserver` in [`MainActivity.kt`](app/src/main/java/com/emeris/forkful/MainActivity.kt) on `ON_STOP` -> `ON_START`, protecting data whenever the app is minimised or backgrounded.
* **Settings Verification**: Enabling or disabling the biometric lock in [`SettingsScreen.kt`](app/src/main/java/com/emeris/forkful/ui/settings/SettingsScreen.kt) requires live biometric verification.

### 2. 📴 Offline Mode with Automatic Synchronisation
Forkful delivers a resilient offline architecture using SQLite and RoomDB:
* **Local Persistence**: Defined in [`AppDatabase.kt`](app/src/main/java/com/emeris/forkful/data/local/AppDatabase.kt) and [`PantryDao.kt`](app/src/main/java/com/emeris/forkful/data/local/PantryDao.kt) targeting table `pantry_items`.
* **Optimistic Offline Writes**: Users can log ingredients without an active connection. Items are saved locally with `isPendingSync = true`.
* **Visual Status Feedback**: [`PantryScreen.kt`](app/src/main/java/com/emeris/forkful/ui/pantry/PantryScreen.kt) dynamically switches status between `"Synced at HH:mm"` (green) and `"Offline - viewing cached pantry"` (alert banner).
* **Automatic Reconnection Reconciliation**: When connectivity is restored, [`PantryViewModel.kt`](app/src/main/java/com/emeris/forkful/ui/pantry/PantryViewModel.kt) executes `syncPendingItems()`, flushing queued additions to the REST API and executing `markSynced(id)`.

### 3. 🔔 Real-Time Push Notifications (Firebase Cloud Messaging)
Forkful integrates Google Firebase Cloud Messaging for real-time kitchen updates:
* **Messaging Service**: [`ForkfulFirebaseMessagingService.kt`](app/src/main/java/com/emeris/forkful/core/notification/ForkfulFirebaseMessagingService.kt) listens for incoming push payloads and device token refreshes.
* **Notification Channel**: High-importance channel `"forkful_alerts"` ("Kitchen & Recipe Alerts") ensures heads-up display, vibration, and badge counters on Android 8.0+ devices.
* **Modern Permissions**: Full runtime support for `android.permission.POST_NOTIFICATIONS` on Android 13+ (API 33+).
* **Smart Notification Engine**: In [`NotificationsScreen.kt`](app/src/main/java/com/emeris/forkful/ui/notifications/NotificationsScreen.kt), users and examiners can test and trigger real-time smart push notifications for:
  1. **Expiring Pantry Items**: Proactive warnings for ingredients nearing expiration (e.g., fresh spinach) to prevent kitchen waste.
  2. **Context-Aware Meal Recommendations**: Breakfast, lunch, or dinner suggestions tailored to ingredients currently pending in the user's pantry (e.g., Braaibroodjie or Peri-Peri Chicken Bowls).
  3. **Live FCM Cloud Broadcasts**: High-priority cloud notifications simulating remote broadcasts.

### 4. 🌍 Multi-Language Support (Official South African Languages)
To ensure regional accessibility, Forkful includes full localised support for **two official South African languages** alongside English:
* **Languages Supported**:
  1. 🇬🇧 **English** (`en`)
  2. 🇿🇦 **isiZulu** (`zu`)
  3. 🇿🇦 **Afrikaans** (`af`)
* **Reactive Compose Localisation**: Managed via [`ForkfulStrings.kt`](app/src/main/java/com/emeris/forkful/core/i18n/ForkfulStrings.kt) and injected into the Compose tree using `CompositionLocalProvider(LocalForkfulStrings provides ...)`.
* **Instant Dynamic Switching**: Changing language in Settings immediately updates all bottom navigation tabs, pantry headers, offline banners, cooking instructions, and lock screens without requiring an application restart.

### 5. 📦 Production Store Publication Readiness
The application has been prepared and validated for Google Play Store publication:
* **Signed Production APK**: Formally compiled and signed using APK Signature Scheme v2 at [`release/forkful-release-signed.apk`](release/forkful-release-signed.apk).
* **Production Keystore**: Generated PKCS12 production keystore at `app/release-key.jks` with alias `forkful`.
* **Verification Command**:
  ```bash
  apksigner verify --verbose release/forkful-release-signed.apk
  # Result: Verified using v2 scheme (APK Signature Scheme v2): true
  ```
* **Production Assets**: Vector adaptive launcher icons configured across all density buckets (`mipmap-mdpi` through `xxxhdpi`).

---

## 🧩 Architectural Design & System Structure

Forkful follows **Clean Architecture** and **MVVM** principles with unidirectional data flow (UDF):

```text
┌─────────────────────────────────────────────────────────────┐
│                      UI / Presentation                      │
│        Jetpack Compose • Material 3 • Reactive State        │
│  Explore • Swipe Stack • Pantry • Basket • RecipeBox • Lock │
└──────────────────────────────┬──────────────────────────────┘
                               │ User Actions & UI Events
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     ViewModel Layer                         │
│           StateFlow • Coroutines Scope • ViewState          │
│   PantryViewModel • SettingsViewModel • SwipeViewModel     │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
               ▼                               ▼
┌─────────────────────────────┐ ┌─────────────────────────────┐
│     Local Storage Layer     │ │     Remote Network Layer    │
│  RoomDB (Pantry SQLite)     │ │  Retrofit 2 • OkHttp 4      │
│  EncryptedSharedPreferences │ │  Kotlinx Serialization JSON │
│  DataStore Preferences      │ │  Firebase Cloud Messaging   │
└──────────────┬──────────────┘ └──────────────┬──────────────┘
               │                               │
               └───────────────┬───────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Supabase Cloud Infrastructure               │
│        PostgreSQL Database (RLS) • Deno Edge Functions      │
│  /recipes-deck • /swipes • /recipe-box • /pantry • /groceries│
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Automated CI/CD Workflows

A GitHub Actions automated pipeline ([`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml)) triggers on every push and pull request to `main`:
1. **Environment Setup**: Provisions Ubuntu runner with JDK 17 (Temurin) and Android SDK platforms 35 & 37.
2. **Permission Check**: Automatically enforces executable permissions on `gradlew`.
3. **Compilation**: Assembles both debug (`assembleDebug`) and signed release (`assembleRelease`) APKs.
4. **Automated Unit Testing**: Runs the complete unit test suite (`testDebugUnitTest`) with 100% test pass rate.
5. **Artifact Upload**: Collects and archives test reports on any regression.

---

## 📝 Formal Release Notes (Prototype → Final POE)

### Version 1.0.0 — Final POE Release
* **Biometric Lock Lifecycle**: Implemented native `BiometricPrompt` with cold-launch locking, background resume re-locking, and Settings verification.
* **Offline Pantry Engine**: Added local SQLite Room database with optimistic offline writes, pending sync queue, and automatic background reconciliation.
* **Firebase Push Notifications**: Added Firebase Cloud Messaging (FCM) integration with Android 13+ permission support and high-importance notification channel.
* **South African Localisation**: Added complete multi-language dictionary for isiZulu and Afrikaans with reactive in-app switching.
* **Signed Release Package**: Generated PKCS12 release keystore and signed production APK for Google Play Store publication.
* **MainActivity Refactoring**: Slimmed `MainActivity.kt` down to a clean lifecycle controller (~70 lines), moving navigation routes into `ForkfulNavHost.kt`.
* **Bug Fixes**: Resolved Explore cooked meal registration bug so recipes cooked directly from search register seamlessly in the Recipe Box.
* **Universal File Tagging**: Updated all Kotlin, Gradle, and TypeScript code files to include standard end-of-file delimiters.

---

## 🤖 Academic AI Usage Disclosure (Maximum 500 Words)

**Module**: PROG7314 — Mobile Application Development  
**Assessment**: Portfolio of Evidence (Final POE)  
**Group Members**:  
* Jamie Abrahams (ST10434047)  
* Isabelle Devlin (ST10445500)  
* Justin Fussell (ST10280758)  
* Matteo Nusca (ST10440432)  

### Transparent Narrative on Generative AI Utilisation
In accordance with the institutional guidelines governing academic integrity and artificial intelligence, generative AI tooling (specifically **Google Antigravity** powered by advanced Gemini models) was utilised as an interactive pair programmer, architecture auditor, and debugging assistant during the development of Forkful.

#### Scope of AI Assistance:
1. **Refactoring & Code Quality**: AI was utilised to analyse and decouple a monolithic `MainActivity.kt` file into a modular `ForkfulNavHost.kt` navigation architecture, ensuring the Activity class remained concise, readable, and focused purely on lifecycle events and biometric security gating.
2. **Biometric Security Implementation**: AI assisted in configuring `androidx.biometric:biometric:1.2.0-alpha05`, verifying device credential fallbacks (`BIOMETRIC_STRONG or DEVICE_CREDENTIAL`), and writing unit test suites (`SettingsViewModelTest.kt`) to guarantee tamper-proof settings persistence.
3. **Edge Function Debugging**: Generative AI was used to diagnose and resolve an asynchronous insertion issue within the Supabase Deno Edge Function (`supabase/functions/recipe-box/index.ts`), ensuring that meals prepared directly from Explore/Search registered immediately as cooked records in PostgreSQL.
4. **Multi-Language Lexicon**: AI tooling assisted in translating English kitchen terminology into accurate, natural **isiZulu** and **Afrikaans** localisation dictionaries adhering to South African linguistic conventions.
5. **DevOps & Build Automation**: AI assisted in diagnosing GitHub Actions CI/CD failures relating to Linux file executable permissions (`chmod +x gradlew`) and configuring the PKCS12 release signing pipeline.

#### Critical Verification:
All code proposed or generated by AI tools was manually reviewed, compiled, and rigorously verified against local unit tests (`./gradlew testDebugUnitTest`) and physical Android device runs. Algorithmic logic, database schemas, and architectural designs reflect the team's original design specifications.

---

## 📦 Project Structure

```text
PROG7314-POE---ForkFul/
├── .github/
│   └── workflows/
│       └── android-ci.yml           # Automated CI/CD pipeline
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/emeris/forkful/
│   │   │   │   ├── core/
│   │   │   │   │   ├── designsystem/ # Forkful design tokens & themes
│   │   │   │   │   ├── i18n/         # Multi-language engine (EN, ZU, AF)
│   │   │   │   │   ├── logging/      # Lifecycle & network logging
│   │   │   │   │   ├── network/      # Retrofit & OkHttp client
│   │   │   │   │   ├── notification/ # FCM service & NotificationHelper
│   │   │   │   │   ├── security/     # Native BiometricHelper
│   │   │   │   │   └── session/      # Encrypted session & DataStore
│   │   │   │   ├── data/             # RoomDB, Repositories, DTOs & Mappers
│   │   │   │   ├── domain/           # Models, Interfaces & Business Logic
│   │   │   │   ├── ui/               # Compose screens & ViewModels
│   │   │   │   │   ├── lock/         # AppLockScreen
│   │   │   │   │   ├── explore/      # Explore & Search
│   │   │   │   │   ├── swipestack/   # Mood-based Swipe Discovery
│   │   │   │   │   ├── pantry/       # Offline-synced Pantry
│   │   │   │   │   ├── basket/       # Aisle Grocery Basket
│   │   │   │   │   ├── cooking/      # Step-by-step Cooking Mode
│   │   │   │   │   ├── recipebox/    # Saved & Cooked meals
│   │   │   │   │   ├── settings/     # App preferences & Biometrics
│   │   │   │   │   └── navigation/   # ForkfulNavHost
│   │   │   │   ├── ForkfulApplication.kt
│   │   │   │   └── MainActivity.kt
│   │   │   └── AndroidManifest.xml
│   │   └── test/                     # Complete unit test suite (14 files)
│   ├── build.gradle.kts
│   ├── google-services.json          # Firebase production config
│   └── release-key.jks               # Production PKCS12 release keystore
├── release/
│   └── forkful-release-signed.apk    # Signed production APK
├── supabase/
│   └── functions/                    # Deno Edge Functions
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🏷️ Release Tag
In accordance with the POE submission instructions, the final commit for this submission is explicitly tagged as:
`Final-POE`

---

*Forkful — PROG7314 Mobile Application Development POE Final Submission.*

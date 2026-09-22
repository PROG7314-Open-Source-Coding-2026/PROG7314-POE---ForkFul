# 🍴 Forkful

> **Turn what you have into what you crave.**

**Forkful** is a recipe discovery and kitchen management application built natively for Android using **Kotlin, Jetpack Compose, and Supabase**.

The application is designed to bridge the gap between **what users currently have in their kitchen and what they can actually cook**. Forkful combines personalised recipe discovery, pantry management, expiry tracking, recipe matching, and grocery-list generation into one central application.

Rather than simply providing users with a catalogue of recipes, Forkful considers the user's **taste preferences, available ingredients, dietary requirements, and pantry contents** to help them decide what to cook while reducing unnecessary food purchases and food waste.

---

## 📱 Project Overview

Forkful was developed as a modern Android application with a focus on **personalisation, usability, and practical kitchen management**.

The application allows users to:

* Discover recipes based on their interests and preferences.
* Browse recipes through an interactive swipe-based discovery experience.
* Track ingredients currently available in their pantry.
* Identify recipes that can be prepared using ingredients they already own.
* Monitor ingredient quantities and expiry dates.
* Receive reminders for ingredients that are approaching expiry.
* Save favourite recipes to a personal recipe box.
* Follow recipes using a distraction-free cooking interface.
* Automatically generate grocery lists for missing ingredients.
* Organise shopping items according to supermarket aisles.
* Personalise their experience through dietary, taste, and application settings.

Forkful therefore combines **recipe discovery, pantry management, cooking assistance, and grocery planning** into a single mobile experience.

---

# ✨ Key Features

## 🍳 Recipe Discovery and Mood Stacks

### Interactive Discovery Deck

Forkful provides an interactive card-based discovery experience where users can browse recipes according to different categories and moods.

Users can explore stacks such as:

* 🇮🇹 Italian
* 🥢 Asian
* 🌮 Mexican
* 🔥 Braai
* 🥗 Healthy
* 🍰 Sweets

Recipes can be swiped through to create an engaging and intuitive discovery experience.

### Taste Profile Engine

Forkful uses user interactions with recipes to build a personalised understanding of the user's preferences.

Swipe interactions can contribute towards the user's taste profile, allowing future recommendations to become increasingly relevant to their preferences.

### Granular Recipe Filtering

Users can refine their recipe discovery using multiple filters, including:

* Preparation time
* Difficulty
* Minimum user rating
* Dietary requirements
* Recipe categories
* Other recipe-specific preferences

Preparation-time options include:

`< 15 minutes` · `30 minutes` · `45 minutes` · `60 minutes`

---

# 🥕 Kitchen and Pantry Management

## Intelligent Stock Logging

Users can maintain a digital representation of the ingredients currently available in their kitchen.

Pantry items can contain information such as:

* Ingredient name
* Quantity
* Unit
* Category
* Aisle
* Expiry date
* Remaining shelf life

This allows Forkful to use the user's existing ingredients when determining suitable recipes.

## 📊 Dynamic Pantry Match Scores

Recipes display a **pantry match percentage** indicating how much of the recipe the user can already make using ingredients currently in their pantry.

For example:

> **Pantry Match: 80%**

This allows users to immediately identify recipes that require minimal additional shopping.

## ⏰ Expiry Tracking

Forkful helps users monitor ingredients that are approaching the end of their shelf life.

Visual indicators highlight ingredients based on their urgency, helping users prioritise ingredients before they expire and potentially reducing household food waste.

---

# 🛒 Cooking and Shopping

## 👨‍🍳 Distraction-Free Cooking Mode

Forkful provides a dedicated cooking interface designed to make recipes easier to follow while preparing food.

Recipes are presented as structured, step-by-step instructions so that users can progress through the cooking process without needing to repeatedly navigate through a traditional recipe page.

## 🛍️ Aisle-Organised Grocery Basket

When a recipe requires ingredients that are not currently available in the pantry, Forkful can add the required items to the user's shopping basket.

Shopping items are organised according to supermarket aisles, making the generated list more practical to use during a shopping trip.

## ❤️ Recipe Box

Users can save recipes they enjoy for future use.

The Recipe Box allows users to:

* Save favourite recipes.
* Revisit previously saved meals.
* View completed cooking activities.
* Keep track of previous kitchen successes.

## 🌙 Adaptive Theme Engine

Forkful supports both **Light and Dark themes** through a custom design system.

The application's colour, typography, spacing, and component tokens are centrally managed to maintain consistency throughout the application.

The interface was designed with kitchen environments in mind, including situations where users may be using the application in lower-light environments.

---

# 🔐 Authentication and User Management

Forkful incorporates modern Android authentication technologies to provide users with a secure login experience.

The authentication system uses:

* **Supabase Auth**
* **Android Credential Manager**
* Google authentication
* Secure local session management

User preferences and session information are persisted locally where appropriate, while authentication and user-specific data are managed through the Supabase backend.

---

# 🏗️ Architecture and Technology Stack

Forkful follows modern Android development practices and is structured around **MVVM and Clean Architecture principles**.

The application separates presentation, domain, and data responsibilities to improve maintainability, testability, and scalability.

### Frontend

| Technology          | Purpose                              |
| :------------------ | :----------------------------------- |
| **Kotlin**          | Primary programming language         |
| **Jetpack Compose** | Declarative Android UI framework     |
| **Material 3**      | UI components and design system      |
| **AndroidX**        | Modern Android application libraries |
| **StateFlow**       | Reactive application state           |
| **Coroutines**      | Asynchronous programming             |
| **Coil**            | Image loading and caching            |

### Backend and Data

| Technology                   | Purpose                                    |
| :--------------------------- | :----------------------------------------- |
| **Supabase**                 | Backend-as-a-Service platform              |
| **PostgreSQL**               | Relational database                        |
| **Supabase Auth**            | Authentication and user management         |
| **Row Level Security (RLS)** | Database-level access control              |
| **Deno Edge Functions**      | Server-side backend functionality          |
| **REST APIs**                | Communication between application services |

### Networking and Local Storage

| Technology                     | Purpose                                      |
| :----------------------------- | :------------------------------------------- |
| **Retrofit 2**                 | HTTP API communication                       |
| **OkHttp**                     | Networking and request handling              |
| **Kotlinx Serialization**      | JSON serialisation and deserialisation       |
| **DataStore**                  | Local user preferences and application state |
| **EncryptedSharedPreferences** | Secure local information storage             |

---

# 🧩 Architecture

The application uses a layered architecture based on **MVVM (Model-View-ViewModel)** and **Clean Architecture** concepts.

```text
┌───────────────────────────────────────────┐
│                 UI Layer                  │
│       Jetpack Compose + Material 3        │
│                                           │
│  Screens • Components • Navigation        │
└─────────────────────┬─────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────┐
│               ViewModel Layer              │
│                                           │
│       StateFlow • Coroutines • UDF         │
└─────────────────────┬─────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────┐
│                Domain Layer                │
│                                           │
│     Models • Repository Interfaces         │
│        Business Logic / Use Cases          │
└─────────────────────┬─────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────┐
│                 Data Layer                 │
│                                           │
│   Repositories • DTOs • API • Supabase     │
└─────────────────────┬─────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────┐
│              Supabase Backend              │
│                                           │
│ PostgreSQL • Authentication • RLS          │
│             Edge Functions                │
└───────────────────────────────────────────┘
```

This separation allows individual components to be developed and maintained without unnecessarily coupling the application's UI to backend implementation details.

---

# 📁 Project Structure

```text
com.emeris.forkful
│
├── core
│   ├── designsystem
│   │   └── Adaptive colour tokens, typography,
│   │       shapes, and theme wrappers
│   │
│   ├── logging
│   │   └── Tagged action and lifecycle logging utilities
│   │
│   ├── network
│   │   └── Retrofit API interfaces, DTO converters,
│   │       and authentication interceptors
│   │
│   ├── session
│   │   └── SessionManager, DataStore flows,
│   │       and secure credential handling
│   │
│   └── util
│       └── Date parsing, formatters, and pantry
│           calculation helpers
│
├── data
│   ├── mapper
│   │   └── DTO-to-domain entity mapping
│   │
│   ├── remote
│   │   └── Supabase REST payloads and
│   │       serialization models
│   │
│   └── repository
│       └── Concrete repository implementations
│
├── domain
│   ├── model
│   │   └── Recipe, PantryItem, GroceryItem,
│   │       UserPreferences, etc.
│   │
│   └── repository
│       └── Domain repository interfaces
│
└── ui
    ├── auth
    │   └── Login, SignUp, and Onboarding screens
    │
    ├── basket
    │   └── Grocery checklist grouped by aisle
    │
    ├── components
    │   └── Shared buttons, loaders, error cards,
    │       and navigation components
    │
    ├── cooking
    │   └── Step-by-step cooking interface
    │
    ├── di
    │   └── Dependency injection and ViewModel helpers
    │
    ├── explore
    │   └── Main feed, search, and category browsing
    │
    ├── navigation
    │   └── Compose NavHost, routes, and arguments
    │
    ├── notifications
    │   └── Activity notifications and reminders
    │
    ├── pantry
    │   └── Pantry management and add-item interface
    │
    ├── recipebox
    │   └── Saved recipes and cooking history
    │
    ├── recipedetail
    │   └── Ingredients, nutrition, and recipe information
    │
    └── swipestack
        └── Card-based recipe discovery interface
```

---

# 👥 Group Information

Forkful was developed collaboratively by a group of four Computer Science students.

| Student Name        | Student Number | Role              | Main Contributions                                                   |
| :------------------ | :------------- | :---------------- | :------------------------------------------------------------------- |
| **Isabelle Devlin** | **ST10445500** | Software Engineer | Feature development, theme tokens, and user experience               |
| **Justin Fussell**  | **ST10280758** | Software Engineer | Database schema design, Edge Functions, and backend services         |
| **Jamie Abrahams**  | **ST10434047** | Software Engineer | Application architecture, state management, and Supabase integration |
| **Matteo Nusca**    | **ST10440432** | Software Engineer | UI design system, Jetpack Compose screens, and navigation            |

### 🤝 Collaborative Development

The project was developed collaboratively using modern software development practices.

The team used version control and collaborative development workflows to coordinate application development, integrate individual features, and maintain a shared codebase.

Responsibilities were divided across the team while still allowing members to contribute to multiple areas of the application.

---

# 🚀 Getting Started

## Prerequisites

Before running Forkful, ensure that the following are installed:

* **Android Studio**
* **Android SDK**
* **JDK / Kotlin-compatible Java environment**
* An Android emulator or physical Android device
* A configured **Supabase project**

The recommended development environment is the latest stable version of Android Studio compatible with the project's Gradle and Android SDK configuration.

---

## Installation

### 1. Clone the Repository

```bash
git clone <YOUR-GITHUB-REPOSITORY-URL>
```

Then open the project in **Android Studio**.

### 2. Configure Supabase

Create or access the project's Supabase instance and configure the required:

* PostgreSQL database
* Authentication provider
* Database tables
* Row Level Security policies
* Edge Functions
* API credentials

Ensure that sensitive credentials are not committed directly to the GitHub repository.

### 3. Sync the Project

Allow Android Studio to download and configure the project's Gradle dependencies.

If prompted, select:

**File → Sync Project with Gradle Files**

### 4. Run the Application

Connect an Android device or start an Android emulator.

Then select:

**Run → Run 'app'**

The application should launch on the selected Android device.

---

# 🎥 Application Demonstration

A demonstration video showing **Forkful running on an Android phone** is available below.

The demonstration video showcases the main application functionality, including navigation, recipe discovery, pantry management, recipe matching, and other key features.

**YouTube:**
`https://youtu.be/L_7r36ZJenM`

---

# 🛠️ Development Highlights

Some of the key development considerations throughout the project included:

### Modern Android Development

Forkful uses Jetpack Compose rather than traditional XML-based Android layouts. This allows the interface to be developed using a declarative UI approach and makes reusable UI components easier to maintain.

### Separation of Concerns

The project structure separates UI, domain, and data responsibilities. This reduces dependencies between application layers and allows backend and frontend components to evolve independently.

### Reactive State Management

Kotlin Coroutines and StateFlow are used to manage asynchronous operations and application state, allowing UI components to react to changes in data.

### Secure Authentication

Supabase Auth and Android Credential Manager are used to support authentication while avoiding unnecessary handling of sensitive authentication information within the application.

### Database Security

Supabase PostgreSQL and Row Level Security provide database-level controls for protecting user-specific information and ensuring that authenticated users can only access authorised records.

### User-Centred Design

The application was designed around a common user problem: deciding what to cook with the ingredients already available.

Rather than requiring users to manually search through large collections of recipes, Forkful attempts to reduce decision-making effort through personalised recommendations, pantry matching, and ingredient-aware shopping lists.

---

# 📚 References

The following official documentation and technical resources were used as references during the development of Forkful.

### Android & Kotlin

* **Kotlin Documentation**
  https://kotlinlang.org/docs/home.html

* **Android Developers – Kotlin**
  https://developer.android.com/kotlin

* **Android Developers – Jetpack Compose**
  https://developer.android.com/develop/ui/compose

* **Android Developers – Material 3**
  https://developer.android.com/develop/ui/compose/designsystems/material3

* **Android Developers – Architecture**
  https://developer.android.com/topic/architecture

* **Android Developers – ViewModel**
  https://developer.android.com/topic/libraries/architecture/viewmodel

* **Android Developers – State and Jetpack Compose**
  https://developer.android.com/develop/ui/compose/state

* **Android Developers – Kotlin Coroutines**
  https://developer.android.com/kotlin/coroutines

### Authentication & Android Security

* **Android Developers – Credential Manager**
  https://developer.android.com/identity/sign-in/credential-manager

* **Android Developers – DataStore**
  https://developer.android.com/topic/libraries/architecture/datastore

* **Android Developers – Security**
  https://developer.android.com/privacy-and-security

### Networking & Serialization

* **Retrofit Documentation**
  https://square.github.io/retrofit/

* **OkHttp Documentation**
  https://square.github.io/okhttp/

* **Kotlin Serialization**
  https://kotlinlang.org/docs/serialization.html

* **Coil – Image Loading for Android**
  https://coil-kt.github.io/coil/

### Backend & Database

* **Supabase Documentation**
  https://supabase.com/docs

* **Supabase Authentication**
  https://supabase.com/docs/guides/auth

* **Supabase Database Documentation**
  https://supabase.com/docs/guides/database

* **Supabase Row Level Security**
  https://supabase.com/docs/guides/database/postgres/row-level-security

* **Supabase Edge Functions**
  https://supabase.com/docs/guides/functions

* **PostgreSQL Documentation**
  https://www.postgresql.org/docs/

### Development Tools

* **Android Studio Documentation**
  https://developer.android.com/studio

* **Git Documentation**
  https://git-scm.com/doc

* **GitHub Documentation**
  https://docs.github.com/

---

# 📌 Future Improvements

Potential future enhancements to Forkful could include:

* More advanced machine-learning-based recipe recommendations.
* Additional South African and international cuisine categories.
* Expanded language support.
* More detailed nutritional tracking.
* Barcode-based pantry item entry.
* Improved offline functionality.
* Integration with supermarket pricing and availability.
* More advanced meal planning.
* Household/shared pantry functionality.
* Expanded notification and reminder customisation.

---

# 🍴 Forkful

**Discover. Cook. Track. Enjoy.**

Forkful aims to make cooking easier by connecting **personal preferences, available ingredients, recipes, and shopping** in one convenient Android application.

> *The best recipe might already be in your kitchen.*

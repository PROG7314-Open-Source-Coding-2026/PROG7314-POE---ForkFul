# Forkful

Forkful is a recipe discovery and kitchen management application built for Android using Jetpack Compose and Supabase. It bridges the gap between what is currently in your fridge and what you can cook tonight through taste profile scoring, interactive recipe discovery decks, automated pantry expiry tracking, and aisle-categorised grocery baskets.

---

## Key Features

### Recipe Discovery and Mood Stacks
* **Interactive Discovery Deck:** Browse curated stacks tailored to your mood: Italian, Asian, Mexican, Braai, Healthy, or Sweets.
* **Taste Profile Engine:** The recommendation engine continuously learns your palate from swipe interactions and adjusts future deck suggestions.
* **Granular Filtering:** Narrow recipes down by prep time (under 15m, 30m, 45m, 60m), difficulty, minimum user rating, or specific dietary requirements.

### Kitchen and Pantry Tracking
* **Intelligent Stock Logging:** Track items in your fridge and cupboards with custom quantities, aisle classifications, and remaining shelf life.
* **Dynamic Pantry Match Scores:** Every recipe automatically displays the exact percentage of ingredients you already own before you start.
* **Expiry Alerts:** Visual urgency badges highlight ingredients that are expiring soon or past their prime to minimise food waste.

### Cooking and Shopping
* **Distraction-Free Cooking Mode:** Step-by-step cooking interface designed for readability while working over the stove.
* **Aisle-Organised Basket:** Automatically generate shopping lists for missing recipe items, categorised by supermarket aisle.
* **Recipe Box:** Save your favourite meals, log completed cooks, and revisit past kitchen victories.
* **Adaptive Theme Engine:** Custom-tuned palette supporting system Light and Dark themes, with reduced contrast surfaces to prevent eye strain in low-light kitchens.

---

## Collaborators

| Collaborator | Role | Contributions |
| :--- | :--- | :--- |
| **Jamie** | Software Engineer | Architecture, state management, and Supabase integration |
| **Matteo** | Software Engineer | UI design system, Jetpack Compose screens, and navigation |
| **Isabelle** | Software Engineer | Feature development, theme tokens, and user experience |
| **Justin** | Software Engineer | Database schema design, Edge Functions, and backend services |

---

## Architecture and Tech Stack

Forkful is built using modern Android architecture guidelines following MVVM and Clean Architecture principles.

* **UI Layer:** 100% Jetpack Compose using Material 3 design components and edge-to-edge system bar integration.
* **Architecture:** Unidirectional Data Flow (UDF) powered by Kotlin StateFlow and Coroutines.
* **Image Pipeline:** Coil Compose with asynchronous memory and disk caching.
* **Networking:** Retrofit 2, OkHttp with logging interceptors, and Kotlinx Serialization[cite: 3].
* **Authentication:** Android Credential Manager for Google SSO paired with Supabase Auth[cite: 3].
* **Local Storage:** AndroidX EncryptedSharedPreferences and DataStore for secure session handling and user preferences[cite: 3].
* **Backend:** Supabase PostgreSQL with Row Level Security (RLS) and Deno Edge Functions[cite: 3].

---

## Project Structure

```text
com.emeris.forkful
├── core
│   ├── designsystem    # Adaptive colour tokens, typography, shapes, and theme wrappers
│   ├── logging         # Tagged action and lifecycle logging utilities
│   ├── network         # Retrofit API interfaces, DTO converters, and auth interceptors
│   ├── session         # SessionManager, DataStore flows, and secure credentials
│   └── util            # Date parsing, formatters, and pantry calculation helpers
├── data
│   ├── mapper          # DTO to domain entity mapping logic
│   ├── remote          # Supabase REST payloads and serialization models
│   └── repository      # Concrete repository implementations
├── domain
│   ├── model           # Core domain models (Recipe, PantryItem, GroceryItem, UserPreferences)
│   └── repository      # Domain repository interfaces
└── ui
    ├── auth            # Login, SignUp, and Onboarding screens
    ├── basket          # Grocery checklist grouped by aisle
    ├── components      # Shared buttons, loaders, error cards, and bottom navigation
    ├── cooking         # Step-by-step cooking guide
    ├── di              # Dependency injection and ViewModel factory helpers
    ├── explore         # Main feed, search, and category browse
    ├── navigation      # Compose NavHost, route definitions, and arguments
    ├── notifications   # Activity notifications and reminders
    ├── pantry          # Stock management and add-item bottom sheet
    ├── recipebox       # Saved recipes and cook history
    ├── recipedetail    # Ingredients breakdown, nutrition facts, and macros
    └── swipestack      # Card deck discovery interface

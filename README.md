# Cities Android Application

## Overview & Features

The Cities app is an Android application for browsing, searching, and viewing city locations on a map. It's built with modern Android practices, including Jetpack Compose, Hilt, Kotlin Coroutines/Flow, and a Clean Architecture-inspired structure.

*   **Browse & Search:** Alphabetically sorted, grouped, and searchable list of ~200,000 cities.
*   **Efficient In-Memory Search:** Uses a Trie for fast prefix searching (due to a **no database** constraint).
*   **View on Map:** Opens selected city in a map application.
*   **Loading/Error States:** Visual feedback for UI operations.

**Key Constraint:** Database implementations were forbidden. All city data is loaded from a local JSON asset into memory, using a Trie for search. This may cause high RAM usage on low-spec devices.

## Tech Stack

*   **Language:** Kotlin
*   **UI:** Jetpack Compose
*   **Architecture:** MVVM with Clean Architecture principles (UI, ViewModel, Domain Use Cases, Data Repository)
*   **Async:** Kotlin Coroutines & Flow
*   **DI:** Hilt
*   **Serialization:** Kotlinx Serialization
*   **Build:** Gradle with Kotlin DSL & Version Catalogs

## Project Structure

*   `app/src/main/java/com/shenawynkov/cities/`: Main source code (data, di, domain, presentation layers).
*   `app/src/main/assets/cities.json`: City data source.
*   `build.gradle.kts` (app and project), `settings.gradle.kts`, `gradle/libs.versions.toml`: Build configuration.

## Data Source & Search

Data comes from `cities.json` in assets. Due to the **no database** rule:
1.  JSON is loaded into memory on app start.
2.  A Trie is built from city names for efficient, case-insensitive prefix search.

**Note:** This in-memory approach for ~200,000 cities is memory-intensive and a direct result of the project constraint.

## Setup and Build

1.  **Clone:** `git clone <repository-url> && cd Cities`
2.  **Open:** In Android Studio (latest stable version), open the cloned project.
3.  **Run:** Sync Gradle, select `app` configuration, and run on an emulator/device (API 21+).

**Build Config:** `compileSdk=35`, `minSdk=21`, `targetSdk=35`.

## Known Trade-offs

*   **Memory Usage:** High RAM consumption and potential OOM risk on low-spec devices due to in-memory data handling (no database allowed).
*   **Initial Load:** First-time processing of JSON and Trie building is asynchronous and may take a moment. 
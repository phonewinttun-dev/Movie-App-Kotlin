# MovieApp - Android Movie Application

A modern Android application built with Jetpack Compose, targeting Android SDK 34, featuring ABI splitting (`arm64-v8a` and `armeabi-v7a`), direct external REST API fetching without any intermediate backend, and structured with a clean, un-nested package-by-feature architecture.

---

## 📁 Project Structure

```
MovieApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/movieapp/
│   │       │   ├── network/                     # Direct external API client & HTTP configuration
│   │       │   │   ├── MovieApiService.kt       # Retrofit endpoint definitions
│   │       │   │   └── NetworkClient.kt         # Retrofit & OkHttp client singleton
│   │       │   ├── theme/                       # Jetpack Compose Material 3 design system
│   │       │   │   ├── Color.kt
│   │       │   │   ├── Theme.kt
│   │       │   │   └── Type.kt
│   │       │   ├── util/                        # Shared utility classes & dispatchers
│   │       │   │   ├── Constants.kt             # API URLs, default query params
│   │       │   │   └── Resource.kt              # Sealed class wrapper (Loading, Success, Error)
│   │       │   ├── features/                    # Feature modules (Package-by-Feature)
│   │       │   │   ├── movielist/               # Movie Discovery & Browse feature
│   │       │   │   │   ├── MovieDTO.kt          # DTO: Raw movie entity from API
│   │       │   │   │   ├── MovieListResponseDTO.kt # DTO: Paginated list response
│   │       │   │   │   ├── MovieListRepository.kt # Data repository for movie lists
│   │       │   │   │   ├── MovieListViewModel.kt  # State holder for movie listing
│   │       │   │   │   └── MovieListScreen.kt     # Compose UI for movie feed
│   │       │   │   ├── moviedetail/             # Movie Details feature
│   │       │   │   │   ├── MovieDetailDTO.kt    # DTO: Full movie detail attributes
│   │       │   │   │   ├── MovieDetailRepository.kt # Repository for single movie details
│   │       │   │   │   ├── MovieDetailViewModel.kt  # State holder for movie detail
│   │       │   │   │   └── MovieDetailScreen.kt     # Compose UI for movie detail screen
│   │       │   │   └── search/                  # Movie Search feature
│   │       │   │       ├── MovieSearchResponseDTO.kt # DTO: Search results payload
│   │       │   │       ├── SearchRepository.kt  # Repository for search operations
│   │       │   │       ├── SearchViewModel.kt   # State holder for search queries
│   │       │   │       └── SearchScreen.kt      # Compose UI for movie search
│   │       │   ├── MainActivity.kt              # Single Activity host with Navigation
│   │       │   └── MovieApplication.kt          # Base Application class
│   │       └── AndroidManifest.xml              # Permissions & app declarations
│   ├── build.gradle.kts                         # App module build script (SDK 34 & ABI splits)
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties            # Configured for Gradle 8.7
├── build.gradle.kts                             # Root build configuration
├── settings.gradle.kts                          # Plugin & repository management
├── local.properties                             # Android SDK location
├── gradlew
├── gradlew.bat
└── README.md
```

---

## 🏛️ MVVM Architecture Rules & Guidelines

This codebase strictly adheres to the **Model-View-ViewModel (MVVM)** design pattern with **Unidirectional Data Flow (UDF)**:

### 1. Model Layer
- **DTOs (`*DTO`)**:
  - Every remote data model must carry the `DTO` keyword in its class name (e.g., `MovieDTO`, `MovieListResponseDTO`).
  - DTOs represent raw network payloads directly mapped via `@SerializedName` annotations.
  - Every DTO must include descriptive KDoc/comments explaining what external contract it satisfies and what data it encapsulates.
- **Repository**:
  - Acts as the Single Source of Truth (SSOT) for data retrieval.
  - Directs network requests to `MovieApiService` and encapsulates network error handling, returning clean `Flow<Resource<T>>` objects.
  - Repositories must never reference UI elements, ViewModels, or Android Contexts.

### 2. ViewModel Layer
- **State Management**:
  - Each feature has its own dedicated ViewModel extending Android Jetpack `ViewModel`.
  - Exposes immutable state to the UI via `StateFlow<UiState>`. Private state is mutable (`MutableStateFlow`), while public state is read-only.
- **Coroutines & Lifecycles**:
  - All asynchronous network calls are launched in `viewModelScope` to ensure automatic cancellation upon ViewModel lifecycle disposal.
- **Rules**:
  - Never store references to Views, Composables, Activities, or `Context` in ViewModels (avoids memory leaks).
  - Business logic, search debouncing, and state transformations belong in the ViewModel, not in UI Composables.

### 3. View Layer (Jetpack Compose)
- **Declarative & Stateless**:
  - Composables observe state from the ViewModel using `collectAsStateWithLifecycle()`.
  - Prefer stateless Composables by hoisting state and passing callbacks (`onMovieClick`, `onRetry`, `onQueryChange`).
- **Rules**:
  - Views must not execute network requests or perform business logic.
  - Views only render what is provided in the UI state and dispatch user events upward to the ViewModel.

---

## ⚙️ ABI Splitting (Application Binary Interface)

To significantly decrease download size for end-users, ABI splitting is configured in `app/build.gradle.kts`:

```kotlin
splits {
    abi {
        isEnable = true
        reset()
        include("arm64-v8a", "armeabi-v7a")
        isUniversalApk = false
    }
}
```

- **`armeabi-v7a`**: Target for 32-bit ARM devices.
- **`arm64-v8a`**: Target for modern 64-bit ARM devices.
- **`isUniversalApk = false`**: Prevents generating a bloated universal APK containing redundant binaries.

---

## 🚀 Building the Project

### Prerequisites
- JDK 17
- Android SDK 34 (configured in `local.properties`)

### Commands
```bash
# Build debug APKs with ABI splits
./gradlew assembleDebug

# Output APKs will be located in:
# app/build/outputs/apk/debug/
# ├── app-arm64-v8a-debug.apk
# └── app-armeabi-v7a-debug.apk
```

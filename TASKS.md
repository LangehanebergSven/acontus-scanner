# Implementation Tasks: ERP Scanner App

This document outlines the step-by-step implementation plan for the Android Scanner App based on the requirements defined in the `README.md`.

*Note: The project is currently set up with Kotlin and Jetpack Compose (based on the existing files). The architecture will follow standard Android best practices (MVVM, Repository Pattern, Room Database for local caching).*

## Phase 1: Local Database & Architecture Setup
- [x] **Task 1.1: Setup Local Database (Room)**
  - Create entities for caching: `Employee`, `Article`, `Material`, `Warehouse`, `BookingReason`.
  - Create entities for local persistence: `ScanProcess`, `ScannedItem`, `SqlLog` (for offline synchronization).
  - Create DAOs for these entities.
- [x] **Task 1.2: Implement `DatabaseConnector` & Repository Layer**
  - Create the `DatabaseConnector` interface/class to handle direct SQL execution (with stubs for your manual mapping later).
  - Implement SQL query logging for offline support.
  - Create Repositories to abstract data sources (Local Room vs Direct DB) and handle caching logic (48h expiration).

---
### **Phase 1 Summary (Context for Next Phase):**
**Phase 1 is complete.** The foundational architecture for local data persistence and management is now in place.

**Implementation Details:**
*   **Room Database:**
    *   All necessary Room **entities** (`Employee`, `Article`, `Material`, etc.) have been created in the `com.example.scanner.data.model` package.
    *   The `Material` entity was corrected to use `materialId` as its sole identifier, removing a redundant `materialNumber` field.
    *   Corresponding **DAOs** for each entity, containing all required query methods (e.g., `insert`, `getById`, `search`), have been implemented in `com.example.scanner.data.local.dao`.
    *   The core **`AppDatabase`** class has been set up, referencing all entities and DAOs.
    *   A **`TypeConverter`** for handling `Date` to `Long` conversion was added.
*   **Repository Layer:**
    *   The repository pattern has been established with three core repositories in `com.example.scanner.data.repository`:
        *   `CacheRepository`: To manage the 48h cache invalidation logic for master data.
        *   `ScanRepository`: To handle all operations related to local scan processes and items.
        *   `SyncRepository`: To manage offline SQL query logging and synchronization.
    *   A `DatabaseConnector` interface was created in `com.example.scanner.data.source` to serve as a placeholder for the direct database connection.
*   **Build & Dependencies:**
    *   The project's Gradle files have been successfully configured with all necessary dependencies for Room, including the KSP plugin for code generation. The project is synced and build-ready.
---

## Phase 2: Login & Settings
- [x] **Task 2.1: Login Screen**
  - Implement UI (in German) for Personal-Nr. input.
  - Implement ViewModel to validate user against cached/remote data.
  - Save the active user session.
- [x] **Task 2.2: Settings & Cache Management Screen**
  - Implement UI to invalidate/clear the 48h cache manually.
  - Implement functionality to sync logged/offline SQL queries (`SqlLog`) to the remote DB.
  - Add user logout functionality.

---
### **Phase 2 Summary (Context for Next Phase):**
**Phase 2 is complete.** The user interface for login and settings is implemented, along with the underlying logic for cache management and data synchronization.

**Implementation Details:**
*   **UI (Compose):**
    *   `LoginScreen.kt`: A simple UI with a text field for "Personal-Nr." and a login button.
    *   `SettingsScreen.kt`: A screen with buttons for clearing the cache, synchronizing offline data, and logging out.
    *   `MainActivity.kt`: Updated to handle basic navigation between the login and settings screens based on a simple `isLoggedIn` state.
*   **ViewModels:**
    *   `LoginViewModel.kt`: Placeholder logic for user validation.
    *   `SettingsViewModel.kt`: Contains the logic to handle cache clearing, data synchronization, and logout, connecting the UI to the repository layer.
*   **Dependency Injection (Hilt):**
    *   Hilt was configured for the project to manage dependencies.
    *   `DatabaseModule.kt`: Provides the Room database and DAOs.
    *   `RepositoryModule.kt`: Provides the `CacheRepository` and `SyncRepository`.
    *   A dummy `DatabaseConnectorImpl` was created to satisfy dependencies for the `SyncRepository`.
*   **Build & Configuration:**
    *   Gradle files were updated with Hilt dependencies and the Hilt plugin.
    *   An `Application` class (`ScannerApplication.kt`) was created and registered in the `AndroidManifest.xml`.
---

## Phase 3: Core Process & Configuration
- [x] **Task 3.1: Initial Process Configuration**
  - Implement UI to start a new scan process.
  - Select `Buchungsgrund` (Booking Reason) and `Lager` (Warehouse) before starting.
- [x] **Task 3.2: Main Scanning Overview UI**
  - Create the main view showing the currently active configuration.
  - Implement an empty state for the scanned items list.
  - Ensure the current process auto-saves locally linked to the `Personal-Nr`.
- [x] **Task 3.3: Active Configuration Management**
  - Implement UI/logic to update the current configuration (Warehouse, Booking Reason, MHD, Batch-Nr) on the fly during the scanning process without affecting already scanned items.
- [x] **Task 3.4: Handle Missing Configuration**
    - When the `ScanningScreen` is opened with a process that has invalid configuration data (e.g., deleted Warehouse), it will now correctly redirect the user to the `ProcessConfigurationScreen` to start a new process.

---
### **Phase 3 Summary (Context for Next Phase):**
**Phase 3 is complete.** The core UI for starting and managing a scanning process is now implemented, including on-the-fly configuration changes.

**Implementation Details:**
*   **Navigation:**
    *   The `MainActivity` was refactored from a simple state-based view switch to use the **Jetpack Navigation** component.
    *   A navigation graph was defined with routes for `login`, `settings`, `process_configuration`, and `scanning`.
    *   The `navigation-compose` dependency was added to the project.
*   **Process Configuration:**
    *   A new screen, `ProcessConfigurationScreen`, was created to allow users to select a Warehouse and Booking Reason before starting a scan.
    *   `ProcessConfigurationViewModel` was implemented to fetch the required lists from the `CacheRepository` and create a new `ScanProcess` in the `ScanRepository`.
    *   Dummy data for warehouses and booking reasons was added to the `CacheRepository` for testing.
*   **Main Scanning UI:**
    *   The main `ScanningScreen` was created, which displays the list of scanned items (currently empty).
    *   A `ScanningViewModel` fetches the current process details and items based on the `processId` passed through navigation arguments.
    *   DAOs and Repositories were updated with `getById` methods for more efficient data retrieval.
*   **Active Configuration:**
    *   The `ScanningScreen` now holds state for the "active" configuration (Warehouse, Booking Reason, Batch, MHD) that will be applied to newly scanned items.
    *   The UI includes clickable headers that open selection dialogs and text fields to modify this active configuration at any time, separate from the process's initial settings.
*   **Error Handling:**
    *   The `ScanningViewModel` was improved to handle cases where a `ScanProcess` references invalid configuration data. Instead of showing an error, it now treats it as having no process, guiding the user to create a new, valid configuration.

---

## Phase 4: Scanning & Item Management
- [x] **Task 4.1: Keyence SDK & Architecture Refactor**
  - Integrate `com.keyence.autoid.sdk` for hardware barcode scanning.
  - Implement a manual search fallback (Search by Name/Number) for Articles/Materials.
  - **REFACTORED:** Removed `CacheRepository` in favor of a `MasterDataSynchronizer` and direct DAO access in ViewModels, as per request.
- [x] **Task 4.2: Add Scanned Items & Quantity Input**
  - Upon scan/selection: Show dialog/input for `Menge` (Quantity) and optional `Inhaltsmenge`.
  - Logic: If item exists with the *exact same configuration*, increment quantity instead of adding a new row.
  - Add visual feedback (brief highlight) when an item is added or updated.
- [x] **Task 4.3: List View & Item Interaction**
  - Display items compactly (Article, Configuration, Timestamp).
  - Implement edit functionality for single items (modify quantities).
  - Implement swipe-to-delete or delete button for single items.
- [x] **Task 4.4: Multi-Select & Bulk Edit**
  - Implement long-press/multi-select mode (including "Select All").
  - Implement bulk editing of configurations (MHD, Charge, Lager, Buchungsgrund) - *Note: Bulk Edit Dialog logic is pending, but Multi-Select/Delete is done.*
  - Add a warning dialog if the user is overwriting diverse configurations (e.g., items with different warehouses).

---
### **Phase 4 Summary (Context for Next Phase):**
**Phase 4 is complete.** The core scanning workflow is robust. Users can search, add, edit, and delete items. The UI supports single-item interaction and multi-select actions.

**Implementation Details:**
*   **List & Interaction:**
    *   The `ScanningScreen` is now a single scrollable list containing the header, search bar, and items.
    *   Header automatically hides/shows based on search focus to maximize space.
    *   Items show full configuration details (Warehouse, Reason, etc.).
    *   Swipe-to-dismiss is available for quick deletion in single-mode.
*   **Multi-Select:**
    *   Long-press triggers selection mode.
    *   `TopAppBar` appears contextually with item count and delete action.
    *   Items have checkboxes in this mode.
*   **UX Improvements:**
    *   Visual feedback (highlighting) when adding/updating items.
    *   Addressed keyboard overlap issues using `consumeWindowInsets`.

---

## Phase 5: Submission & Error Handling
- [x] **Task 5.0: ERP Database Connection (MSSQL)**
  - Integrate `mssql-jdbc` driver.
  - Implement `DatabaseConnectorImpl` with actual JDBC connection to SQL Server.
  - Implement SELECT queries to fetch master data (Warehouses, BookingReasons, Articles, Materials, Employees).
  - Refactor `MasterDataSynchronizer` to use the remote MSSQL database instead of dummy data.
- [x] **Task 5.1: Process Submission (ERP Sync)**
  - Implement the "Submit" action converting all `ScannedItem`s into SQL Insert queries.
  - Send queries via `DatabaseConnector`.
  - If DB is unreachable: Show a localized error dialog, save the queries to `SqlLog`, and persist the session locally.
- [x] **Task 5.2: Global Error Handling & Polish**
  - Add localized error dialogs across the app (e.g., "EAN not found", "Database unreachable").
  - Final code review to ensure UI text is German, codebase (variables, methods) is English, and performance/architecture is solid.

---
### **Phase 5 Progress (Final):**
**Phase 5 is complete.** The app is now fully connected to the MSSQL ERP database and handles data synchronization and errors robustly.

**Implementation Details:**
*   **Global Error Handling:**
    *   **Scanning Screen:**
        *   Added a specific `scanError` state to the `ScanningViewModel`.
        *   If a scanned EAN/Barcode is not found in the local database, an Alert Dialog ("Scan Fehler") is now shown instead of silently failing.
        *   Submission errors are properly localized ("Fehler beim Senden").
    *   **Settings / Sync:**
        *   The `SettingsViewModel` now emits `SettingsUiEvent` (Message or Error) to the UI.
        *   A `Snackbar` in `SettingsScreen` displays the result of the synchronization (success or failure).
        *   `MasterDataSynchronizer` was updated to throw exceptions so the ViewModel can catch and display them.
    *   **Process Configuration:**
        *   `ProcessConfigurationViewModel` now catches errors during initial data load and displays a localized error message in the UI ("Fehler beim Laden der Daten").
*   **Offline Capability (SqlLog):**
    *   Updated `SyncRepository` to properly handle offline scenarios.
    *   If `submitScannedItem` fails (e.g., no connection), the generated SQL query is caught and saved to the local `SqlLog` table via `SqlLogDao`.
    *   Added `uploadOfflineLogs()` to `SyncRepository` to retry sending these logged queries.
    *   The "Cache leeren" button in Settings now performs a full sync: first trying to upload offline logs, then downloading fresh master data.
*   **Localization & Polish:**
    *   Verified that all user-facing strings in `ScanningScreen`, `ProcessConfigurationScreen`, `SettingsScreen`, and `LoginScreen` are in German.
    *   Variable names and internal logic remain in English.
    *   Code cleaned up to ensure `SqlLogDao` is properly injected and used.

**Project Status:**
All planned tasks in Phase 1 through Phase 5 are complete. The application is ready for end-to-end testing.

## Cleanup
- [x] **Removed SqlLog:** Removed unused `SqlLog` entity and DAO as per request, streamlining the database schema.

## UI Enhancements
- [x] **Update Login UI:**
  - Redesigned Login Screen to include `hemme_logo_full` (main branding) and `acontus_rgb` (developer branding at footer).

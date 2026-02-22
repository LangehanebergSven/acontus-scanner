# Package: com.example.scanner.data.repository

This package contains the repository classes, which implement the Repository Pattern.

## Contents
- **Repositories:** These classes are the single source of truth for application data. They abstract the data sources (local and remote) from the rest of the app (especially the ViewModels). They are responsible for handling data fetching, caching, and synchronization logic.
  - `CacheRepository`: Manages master data caching (48h expiration).
  - `ScanRepository`: Handles all local data operations for scanning processes.
  - `SyncRepository`: Manages offline SQL query logging and synchronization.

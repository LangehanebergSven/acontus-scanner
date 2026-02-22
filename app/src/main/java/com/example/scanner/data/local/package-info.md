# Package: com.example.scanner.data.local

This package contains all components related to the local Room database, excluding the model classes and DAOs which are in sub-packages.

## Contents
- **`AppDatabase.kt`:** The main Room database class for the application. It's an abstract class that extends `RoomDatabase` and lists all entities and DAOs.
- **`Converters.kt`:** Contains `TypeConverter` classes that allow Room to persist custom types (e.g., converting a `Date` object to a `Long` timestamp).

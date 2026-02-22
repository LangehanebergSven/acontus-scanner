# Package: com.example.scanner.data.model

This package contains all data models and entity classes for the application.

## Contents
- **Room Entities:** Plain Kotlin `data class`es annotated with `@Entity`. These classes define the schema for the local Room database tables.
  - Caching Entities: `Employee`, `Article`, `Material`, `Warehouse`, `BookingReason`.
  - Local Persistence Entities: `ScanProcess`, `ScannedItem`, `SqlLog`.

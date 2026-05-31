# Cashy – Point of Sale and Inventory Management System

Cashy is a Point of Sale (POS) and inventory management mobile application built using Kotlin and Jetpack Compose. The application utilizes Supabase as its backend service for authentication, database management, and real-time data operations. It is designed to support small to medium-sized business operations, including product management, sales processing, and activity tracking.

---

## Features

### Authentication

* Email and password authentication using Supabase Auth
* User registration and login functionality
* Session management and persistent authentication state
* Password change functionality

### Inventory Management

* Product creation, update, and management
* Real-time stock tracking
* Inventory activity logging including stock adjustments, sales deductions, and manual updates

### Sales Management

* Sales transaction processing
* Automatic stock deduction upon successful sales
* Transaction history with status tracking (completed, cancelled)
* Payment tracking including total amount and payment received

### Customer Management

* Customer registration and profile management
* Customer status updates
* Activity logging for customer-related actions such as registration and profile changes

### Activity Logging

* Centralized activity tracking system
* Categorized logs for inventory, sales, and system activities
* Timestamp formatting and chronological sorting
* Unified activity feed for monitoring system behavior

### User Profile and Settings

* Profile editing functionality
* Password update functionality
* Application information section (placeholder content)
* Logout functionality with session clearing

---

## Technology Stack

* Kotlin
* Jetpack Compose
* MVVM Architecture
* Supabase (Authentication and PostgREST API)
* Kotlin Coroutines
* StateFlow for reactive state management
* Material Design 3

---

## System Architecture

The application follows the MVVM (Model–View–ViewModel) architecture pattern:

UI Layer (Jetpack Compose)
↓
ViewModel Layer
↓
Repository Layer
↓
Supabase Client

This separation ensures maintainability, testability, and clear responsibility distribution across components.

---

## Database Structure

The application uses the following Supabase tables:

* profiles
* product
* penjualan
* inventory_log
* pelanggan_log

Each table is secured using Row Level Security (RLS) policies to ensure data access is restricted based on authenticated user context.

---

## Setup Instructions

### Prerequisites

* Android Studio installed
* Kotlin support enabled
* Supabase project configured

### Installation Steps

1. Clone the repository:

```bash
git clone https://github.com/Tgerrrrr/Cashy.git
```

2. Open the project in Android Studio.

3. Configure Supabase credentials by creating a client provider file:

```kotlin
object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = "YOUR_SUPABASE_URL",
        supabaseKey = "YOUR_SUPABASE_ANON_KEY"
    )
}
```

4. Sync Gradle dependencies.

5. Run the application on an emulator or physical device.

---

## Security Considerations

* Row Level Security (RLS) is enabled for all database tables.
* Authentication is required for all data operations.
* User-specific data access is enforced at the database level.

---

## Future Enhancements

* Barcode scanning integration for product management
* Dashboard analytics and reporting features
* Offline mode support with local caching
* Multi-store or multi-branch support
* Role-based access control improvements (admin, cashier, manager)
* Export and backup functionality

---

## Developer Information

This project is developed using modern Android development practices and is intended for educational and portfolio purposes.

---

## License

This project is intended for educational use and demonstration purposes.


# MyCal

## Overview
Android Calendar Application

## Technology Stack
- Kotlin
- Jetpack Compose
- Glance API (Widget)
- Hilt (DI)
- Room Database
- Retrofit & OkHttp
- Coroutines & Flow
- WorkManager (Synchronization)
- iCal4j (ICS Parsing)

## Functions
- Calendar Viewer
- ICS Calendar Subscription
    - URL Based Subscription
    - Automatic Sync (15min)
    - Multiple Subscription


## Info
- Use Java Runtime of Android Studio when you build
    - User do not have Java Runtime in the PC
- Use logging in during execution properly for better debugging
- Require minSdk 26+ (Android 8.0+)
- targetSdk 36
- compileSdk 36
- Kotlin JVM Target: 11
- ProGuard rules for iCal4j configuration required

## Dev/Debug Commands

### ADB Commands
```bash
# ADB Path
export ADB=/Users/ysyoon/Library/Android/sdk/platform-tools/adb

# Check connected devices
$ADB devices

# Install APK (specify emulator)
$ADB -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk

# Uninstall existing app
$ADB -s emulator-5554 uninstall com.example.mycal

# Check installed packages
$ADB -s emulator-5554 shell pm list packages | grep mycal

# Run app
$ADB -s emulator-5554 shell monkey -p com.example.mycal -c android.intent.category.LAUNCHER 1

# Check logs (all)
$ADB -s emulator-5554 logcat -d

# Check logs (specific tags)
$ADB -s emulator-5554 logcat -d -s CalendarSyncWorker:* IcsParser:* CalendarRepository:* CalendarViewModel:*

# Real-time log monitoring
$ADB -s emulator-5554 logcat | grep -E "CalendarSyncWorker|IcsParser|CalendarRepository"

# Check WorkManager job status
$ADB -s emulator-5554 shell dumpsys jobscheduler | grep com.example.mycal
```

### Gradle Commands
```bash
# Use Android Studio's Java runtime
export JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home && ./gradlew assembleDebug
# Build
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Clean build
./gradlew clean build

# Install to device
./gradlew installDebug
```

### File Locations
- APK Output Path: `app/build/outputs/apk/debug/app-debug.apk`
- AndroidManifest.xml: `app/src/main/AndroidManifest.xml`
- App Build Configuration: `app/build.gradle.kts`

### Test Data
ICS
- URL: https://ext.todoist.com/export/ical/project?user_id=54262437&project_id=6cvRx3HXWXRpfFwv&ical_token=f4c956ac&r_factor=3342
- Name: calendar

## Project Structure

### Package Structure
```
com.example.mycal/
├── data/
│   ├── local/
│   │   ├── dao/           # Room DAO
│   │   ├── database/       # Room Database
│   │   └── entity/         # Database Entities
│   ├── remote/
│   │   ├── api/            # Retrofit API
│   │   ├── datasource/     # Remote Data Sources
│   │   └── parser/         # ICS Parser
│   ├── repository/         # Repository Implementation
│   ├── mapper/             # Entity <-> Domain Mapper
│   └── sync/               # WorkManager Synchronization
├── domain/
│   ├── model/              # Domain Models
│   ├── repository/         # Repository Interface
│   ├── usecase/            # Use Cases
│   └── event/              # Event Manager
├── presentation/
│   ├── components/         # UI Components
│   ├── screens/            # Screen Composables
│   │   ├── calendar/       # Calendar Screen
│   │   └── subscription/   # Subscription Management Screen
│   └── theme/              # Theme Configuration
├── widget/                 # Glance Widget
│   ├── state/              # Widget State
│   └── ui/                 # Widget UI Components
├── di/                     # Hilt Modules
├── MainActivity.kt
└── MyCalApplication.kt
```

### Main Components

#### Database (Room)
- **CalendarDatabase**: Main Room Database class
- **EventEntity**: Event data entity
- **CalendarSourceEntity**: Calendar source entity
- **EventDao**: Event CRUD operations
- **CalendarSourceDao**: Calendar source CRUD operations

#### Network
- **IcsApiService**: Retrofit API interface
- **IcsParser**: ICS file parsing using iCal4j
- **IcsRemoteDataSource**: Remote data source

#### Sync
- **CalendarSyncWorker**: WorkManager Worker (15-minute interval)
- **CalendarSyncManager**: Synchronization management

#### Widget
- **CalendarWidget**: GlanceAppWidget implementation
- **CalendarWidgetReceiver**: Widget BroadcastReceiver
- **CalendarWidgetWorker**: Widget update Worker
- **CalendarWidgetDataProvider**: Widget data provider

#### DI Modules
- **DatabaseModule**: Provides Room Database
- **NetworkModule**: Provides Retrofit, OkHttp
- **RepositoryModule**: Repository bindings

## Development Tips

### ICS Parsing Debugging
- Check detailed logs in IcsParser
- Pay attention to timezone when parsing date/time
- Distinguish between all-day events and timed events
- RRULE (recurrence rule) handling is not implemented yet

### Widget Development
- Glance API is similar to Compose but limited
- Provide different UI for each widget size (Small/Medium/Large)
- Use WorkManager for widget updates
- Handle click events with PendingIntent

### Synchronization
- Background sync every 15 minutes using WorkManager
- Parallel processing for each calendar source
- Retry logic for network failures
- Sync status is delivered to UI via Flow

### Room Database
- Handle LocalDateTime with type converters
- Add migration strategy when needed
- Improve query performance with index optimization

### Compose UI
- Performance optimization with LazyColumn/LazyRow
- Proper use of remember/rememberSaveable
- State management with state hoisting
- Screen navigation with Navigation Compose

## Common Issues and Solutions

### ICS Parsing Failure
- Encoding issue: Check UTF-8
- Date format: Distinguish between DATE and DATE-TIME
- Timezone: Handle TZID

### Widget Not Updating
- Check GlanceAppWidgetManager.update() call
- Check WorkManager constraints
- Check Widget Provider registration

### Sync Failure
- Check network permissions
- SSL certificate issues
- Check ProGuard rules

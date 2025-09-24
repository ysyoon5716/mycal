# MyCal

## 개요
안드로이드 달력 어플리케이션

## 기술스택
- Kotlin
- Jetpack Compose
- Glance API (위젯)
- Hilt (DI)
- Room Database
- Retrofit & OkHttp
- Coroutines & Flow
- WorkManager (동기화)
- iCal4j (ICS 파싱)

## 기능
- 달력 뷰어 (월별 보기)
- ICS 달력 구독 (URL 기반)
- 달력 위젯 (Small, Medium, Large)
- 자동 동기화 (15분 간격)
- 다중 캘린더 소스 지원
- 커스텀 색상 선택

## 주의사항
- 빌드시 안드로이드 스튜디오의 Java runtime 사용
- 중간과정에 적절한 Logging을 통해 디버깅 용이화
- minSdk 26 이상 필요 (Android 8.0+)
- targetSdk 36
- compileSdk 36
- Kotlin JVM Target: 11
- ProGuard 룰에 iCal4j 관련 설정 필요

## 개발/디버깅 커맨드

### ADB 명령어
```bash
# ADB 경로
export ADB=/Users/ysyoon/Library/Android/sdk/platform-tools/adb

# 연결된 디바이스 확인
$ADB devices

# APK 설치 (에뮬레이터 지정)
$ADB -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk

# 기존 앱 제거
$ADB -s emulator-5554 uninstall com.example.mycal

# 설치된 패키지 확인
$ADB -s emulator-5554 shell pm list packages | grep mycal

# 앱 실행
$ADB -s emulator-5554 shell monkey -p com.example.mycal -c android.intent.category.LAUNCHER 1

# 로그 확인 (전체)
$ADB -s emulator-5554 logcat -d

# 로그 확인 (특정 태그)
$ADB -s emulator-5554 logcat -d -s CalendarSyncWorker:* IcsParser:* CalendarRepository:* CalendarViewModel:*

# 실시간 로그 모니터링
$ADB -s emulator-5554 logcat | grep -E "CalendarSyncWorker|IcsParser|CalendarRepository"

# WorkManager 작업 상태 확인
$ADB -s emulator-5554 shell dumpsys jobscheduler | grep com.example.mycal
```

### Gradle 명령어
```bash
# Android Studio의 Java runtime 사용 필요
export JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home && ./gradlew assembleDebug
# 빌드
./gradlew build

# 디버그 APK 빌드
./gradlew assembleDebug

# 클린 빌드
./gradlew clean build

# 디바이스에 설치
./gradlew installDebug
```

### 파일 위치
- APK 출력 경로: `app/build/outputs/apk/debug/app-debug.apk`
- AndroidManifest.xml: `app/src/main/AndroidManifest.xml`
- 앱 빌드 설정: `app/build.gradle.kts`

### 테스트 데이터
ICS
- URL: https://ext.todoist.com/export/ical/project?user_id=54262437&project_id=6cvRx3HXWXRpfFwv&ical_token=f4c956ac&r_factor=3342
- Name: calendar

## 프로젝트 구조

### 패키지 구조
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
│   ├── repository/         # Repository 구현체
│   ├── mapper/             # Entity <-> Domain Mapper
│   └── sync/               # WorkManager 동기화
├── domain/
│   ├── model/              # Domain Models
│   ├── repository/         # Repository 인터페이스
│   ├── usecase/            # Use Cases
│   └── event/              # Event Manager
├── presentation/
│   ├── components/         # UI Components
│   ├── screens/            # Screen Composables
│   │   ├── calendar/       # 달력 화면
│   │   └── subscription/   # 구독 관리 화면
│   └── theme/              # Theme 설정
├── widget/                 # Glance Widget
│   ├── state/              # Widget State
│   └── ui/                 # Widget UI Components
├── di/                     # Hilt Modules
├── MainActivity.kt
└── MyCalApplication.kt
```

### 주요 컴포넌트

#### Database (Room)
- **CalendarDatabase**: Room Database 메인 클래스
- **EventEntity**: 이벤트 데이터 엔티티
- **CalendarSourceEntity**: 캘린더 소스 엔티티
- **EventDao**: 이벤트 CRUD 작업
- **CalendarSourceDao**: 캘린더 소스 CRUD 작업

#### Network
- **IcsApiService**: Retrofit API 인터페이스
- **IcsParser**: iCal4j를 사용한 ICS 파일 파싱
- **IcsRemoteDataSource**: 원격 데이터 소스

#### Sync
- **CalendarSyncWorker**: WorkManager Worker (15분 주기)
- **CalendarSyncManager**: 동기화 관리

#### Widget
- **CalendarWidget**: GlanceAppWidget 구현
- **CalendarWidgetReceiver**: Widget BroadcastReceiver
- **CalendarWidgetWorker**: Widget 업데이트 Worker
- **CalendarWidgetDataProvider**: Widget 데이터 제공

#### DI Modules
- **DatabaseModule**: Room Database 제공
- **NetworkModule**: Retrofit, OkHttp 제공
- **RepositoryModule**: Repository 바인딩

## 개발 팁

### ICS 파싱 디버깅
- IcsParser에서 상세 로그 확인
- 날짜/시간 파싱 시 타임존 주의
- All-day 이벤트와 시간 이벤트 구분 처리
- RRULE (반복 규칙) 처리는 미구현 상태

### Widget 개발
- Glance API는 Compose와 유사하나 제한적
- Widget 크기별로 다른 UI 제공 (Small/Medium/Large)
- Widget 업데이트는 WorkManager 사용
- 클릭 이벤트는 PendingIntent로 처리

### 동기화
- WorkManager로 15분 주기 백그라운드 동기화
- 각 캘린더 소스별 병렬 처리
- 네트워크 실패 시 재시도 로직
- 동기화 상태는 Flow로 UI에 전달

### Room Database
- 타입 컨버터로 LocalDateTime 처리
- Migration 전략 필요 시 추가
- 인덱스 최적화로 쿼리 성능 개선

### Compose UI
- LazyColumn/LazyRow로 성능 최적화
- remember/rememberSaveable 적절히 사용
- State hoisting으로 상태 관리
- Navigation Compose로 화면 전환

## 자주 발생하는 문제와 해결

### ICS 파싱 실패
- 인코딩 문제: UTF-8 확인
- 날짜 형식: DATE vs DATE-TIME 구분
- 타임존: TZID 처리

### Widget 업데이트 안됨
- GlanceAppWidgetManager.update() 호출 확인
- WorkManager 제약 조건 확인
- Widget Provider 등록 확인

### 동기화 실패
- 네트워크 권한 확인
- SSL 인증서 문제
- ProGuard 규칙 확인

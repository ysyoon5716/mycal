# MyCal

## 개요
안드로이드 달력 어플리케이션

## 기술스택
- Kotlin
- Jetpack Compose
- Glance API

## 기능
- 달력 뷰어
- ICS 달력 구독
- 달력 위젯

## 주의사항
- 빌드시 안드로이드 스튜디오의 Java runtime 사용
- 중간과정에 적절한 Logging을 통해 디버깅 용이화

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
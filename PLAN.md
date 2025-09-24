# MyCal 구현 계획

## 1. 프로젝트 아키텍처

### 1.1 아키텍처 패턴
- **MVVM (Model-View-ViewModel)** 패턴 적용
- **Clean Architecture** 원칙 준수
- **Repository Pattern** 을 통한 데이터 계층 추상화

### 1.2 레이어 구조
```
presentation/     # UI 레이어 (Compose UI, ViewModel)
├── screens/     # 각 화면별 Composable
├── viewmodels/  # 화면별 ViewModel
├── components/  # 재사용 가능한 UI 컴포넌트
└── theme/       # 테마, 색상, 타이포그래피

domain/          # 비즈니스 로직
├── models/      # 도메인 모델
├── usecases/    # 유스케이스
└── repository/  # 레포지토리 인터페이스

data/            # 데이터 레이어
├── local/       # 로컬 데이터베이스 (Room)
├── remote/      # 원격 데이터 (ICS 파싱)
├── repository/  # 레포지토리 구현체
└── mapper/      # 데이터 매퍼

widget/          # Glance 위젯
├── receiver/    # 위젯 브로드캐스트 리시버
├── provider/    # 위젯 프로바이더
└── ui/          # 위젯 UI 컴포저블
```

## 2. 주요 기능 구현 계획

### 2.1 달력 뷰어
#### 기능 요구사항
- 월별/주별/일별 뷰 전환
- 이벤트 표시 및 관리
- 스와이프를 통한 날짜 이동
- 오늘 날짜 강조 표시

#### 구현 세부사항
- **CalendarScreen**: 메인 달력 화면 Composable
- **CalendarViewModel**: 달력 상태 관리
- **CalendarRepository**: 이벤트 데이터 관리
- **Custom Calendar Composable**:
  - LazyVerticalGrid를 활용한 월별 뷰
  - 날짜 셀 커스텀 렌더링
  - 이벤트 인디케이터 표시

### 2.2 ICS 달력 구독
#### 기능 요구사항
- URL을 통한 ICS 파일 가져오기
- ICS 파싱 및 이벤트 추출
- 주기적 동기화
- 다중 캘린더 소스 지원

#### 구현 세부사항
- **IcsParser**: iCal4j 또는 자체 구현 파서
- **SyncWorker**: WorkManager를 활용한 백그라운드 동기화
- **CalendarSourceManager**: 구독 캘린더 관리
- **데이터베이스 스키마**:
  ```kotlin
  @Entity
  data class CalendarSource(
      @PrimaryKey val id: String,
      val url: String,
      val name: String,
      val color: Int,
      val syncEnabled: Boolean,
      val lastSyncTime: Long
  )

  @Entity
  data class Event(
      @PrimaryKey val id: String,
      val sourceId: String,
      val title: String,
      val description: String?,
      val startTime: Long,
      val endTime: Long,
      val isAllDay: Boolean,
      val location: String?,
      val rrule: String? // 반복 규칙
  )
  ```

### 2.3 달력 위젯
#### 기능 요구사항
- 홈 화면 위젯 제공
- 다양한 크기 지원 (2x2, 4x2, 4x4)
- 오늘의 일정 표시
- 앱 실행 단축키

#### 구현 세부사항
- **Glance API 활용**:
  ```kotlin
  class CalendarWidget : GlanceAppWidget() {
      override suspend fun provideGlance(context: Context, id: GlanceId) {
          provideContent {
              CalendarWidgetContent()
          }
      }
  }
  ```
- **위젯 업데이트 Worker**: 주기적 업데이트
- **위젯 상태 관리**: DataStore 활용

## 3. 개발 단계

### Phase 1: 기초 설정 (1주)
- [ ] 프로젝트 초기 설정
- [ ] 의존성 추가 (Compose, Room, Glance, Hilt 등)
- [ ] 기본 네비게이션 구조 설정
- [ ] 테마 및 디자인 시스템 구축

### Phase 2: 달력 뷰어 개발 (2-3주)
- [ ] 달력 UI 컴포넌트 개발
- [ ] Room 데이터베이스 설정
- [ ] 이벤트 CRUD 기능
- [ ] 날짜 선택 및 네비게이션
- [ ] 뷰 모드 전환 (월/주/일)

### Phase 3: ICS 구독 기능 (2주)
- [ ] ICS 파서 구현/통합
- [ ] 네트워크 레이어 구축
- [ ] 동기화 로직 구현
- [ ] WorkManager 설정
- [ ] 구독 관리 UI

### Phase 4: 위젯 개발 (1-2주)
- [ ] Glance 위젯 기본 구조
- [ ] 위젯 UI 디자인
- [ ] 데이터 연동
- [ ] 위젯 설정 화면

### Phase 5: 마무리 및 최적화 (1주)
- [ ] 성능 최적화
- [ ] 테스트 작성
- [ ] 버그 수정
- [ ] 릴리즈 준비

## 4. 기술적 고려사항

### 4.1 의존성 관리
```kotlin
dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Glance
    implementation("androidx.glance:glance-appwidget:1.0.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Date/Time
    implementation("org.threeten:threetenbp:1.6.8")
}
```

### 4.2 성능 최적화
- **LazyColumn/LazyGrid** 활용으로 대량 이벤트 처리
- **Room 쿼리 최적화** (인덱싱, 페이징)
- **Compose 리컴포지션 최소화**
- **이미지 캐싱** (Coil 라이브러리)

### 4.3 보안 고려사항
- HTTPS만 허용하여 ICS URL 가져오기
- 민감한 정보 암호화 저장
- ProGuard 규칙 설정

## 5. 테스트 계획

### 5.1 단위 테스트
- ViewModel 로직 테스트
- Repository 테스트
- ICS 파서 테스트
- 날짜 계산 로직 테스트

### 5.2 UI 테스트
- Compose UI 테스트
- 네비게이션 테스트
- 사용자 상호작용 테스트

### 5.3 통합 테스트
- 데이터베이스 작업 테스트
- 동기화 프로세스 테스트
- 위젯 업데이트 테스트

## 6. 추가 개선 사항 (향후 업데이트)

- **다크 모드 지원**
- **알림 기능**
- **Google Calendar 연동**
- **이벤트 검색 기능**
- **반복 일정 지원**
- **카테고리/태그 기능**
- **백업 및 복원**
- **다국어 지원**

## 7. 리스크 관리

### 기술적 리스크
- ICS 파싱의 복잡성 → iCal4j 라이브러리 활용 고려
- Glance API의 제한사항 → 대체 위젯 솔루션 준비
- 대량 이벤트 성능 → 페이징 및 가상화 적용

### 일정 리스크
- 각 단계별 버퍼 시간 확보
- MVP 우선 개발 후 점진적 기능 추가
- 주요 마일스톤별 검토 및 조정

## 8. 문서화

- API 문서 (KDoc)
- 사용자 가이드
- 개발자 문서
- 변경 로그 관리
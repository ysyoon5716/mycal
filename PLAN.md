# 월달력 위젯 개선 계획

## 요구사항 분석

**사용자 요청**: example.jpeg처럼 달력만 보이고, 달력 안에 날짜별 일정이 적혀있는 월달력 위젯

**현재 상황 분석**:
- ExtraLargeWidgetContent.kt에 이미 월별 전체 달력 그리드 구현되어 있음
- 이벤트가 있는 날짜에 작은 점(indicator)만 표시됨
- 이벤트 제목은 표시되지 않음
- 다른 크기 위젯(Medium, Large)은 월달력 대신 리스트 형태

## 구현 계획

### 1단계: ExtraLargeWidgetContent 개선 (우선순위: 높음)

#### 1.1 DayCell 함수 수정 (`ExtraLargeWidgetContent.kt:175-241`)
**목표**: 이벤트 점 대신 이벤트 제목을 표시
- 현재: `hasEvents`로 점만 표시
- 개선: 실제 이벤트 목록을 받아서 이벤트 제목 표시

**변경사항**:
```kotlin
// 현재
private fun DayCell(
    ...,
    hasEvents: Boolean,
    ...
)

// 개선 후
private fun DayCell(
    ...,
    events: List<WidgetEvent>,
    ...
)
```

#### 1.2 이벤트 텍스트 표시 로직 추가
- 한 날짜에 최대 2-3개 이벤트 제목 표시
- 글자 크기: 8-9sp (작은 크기)
- 긴 제목은 줄임표(...) 처리
- 다중 이벤트시 줄바꿈으로 처리

#### 1.3 셀 크기 조정
- 현재 height: 36dp → 42-48dp로 확장
- 이벤트 텍스트가 들어갈 공간 확보

### 2단계: 이벤트 데이터 로딩 확인 (우선순위: 높음)

#### 2.1 CalendarWidgetDataProvider 점검 (`CalendarWidgetDataProvider.kt`)
**확인사항**:
- `monthEvents` 맵이 제대로 채워지는지 확인
- 날짜 키 형식이 `hasEventsOnDate` 함수와 일치하는지 확인 (`YYYY-MM-DD` 형식)

#### 2.2 날짜별 이벤트 조회 로직 개선
- `getEventsForDate()` 헬퍼 함수 추가
- 여러 이벤트 소스 통합 (monthEvents, weekEvents, todayEvents)

### 3단계: Medium/Large 위젯 월달력 옵션 추가 (우선순위: 중간)

#### 3.1 위젯 모드 선택 기능
**CalendarWidgetState 확장**:
```kotlin
enum class WidgetViewMode {
    TODAY,      // 기존
    WEEK,       // 기존
    MONTH,      // 기존
    MONTH_GRID  // 새로 추가: 월달력 그리드 표시
}
```

#### 3.2 MediumWidgetContent 수정
- 현재: 오늘 이벤트 리스트만 표시
- 추가: `MONTH_GRID` 모드일 때 작은 월달력 표시

#### 3.3 LargeWidgetContent 수정
- 현재: 미니 달력 + 오늘 이벤트 요약
- 개선: 미니 달력을 풀사이즈로 확장하여 이벤트 제목 표시

### 4단계: UI/UX 최적화 (우선순위: 낮음)

#### 4.1 색상 및 테마 개선
- 이벤트 텍스트 색상 최적화
- 주말(일요일/토요일) 색상 구분 유지
- 어두운 테마 대응

#### 4.2 성능 최적화
- 이벤트가 많은 날짜의 렌더링 성능 확인
- 메모리 사용량 최적화

## 구현 순서 및 일정

### Phase 1: 핵심 기능 구현 (1-2시간)
1. `hasEventsOnDate()` → `getEventsForDate()` 함수 변경
2. `DayCell` 함수 파라미터 및 UI 수정
3. 이벤트 텍스트 표시 로직 구현

### Phase 2: 데이터 검증 및 디버깅 (30분)
1. CalendarWidgetDataProvider 동작 확인
2. 실제 이벤트 데이터로 테스트
3. 로그 추가 및 디버깅

### Phase 3: 확장 기능 (1시간)
1. Medium/Large 위젯 월달력 지원
2. 위젯 크기별 최적화

## 주의사항

### 기술적 제약
- Glance API 제한사항 (Compose 대비 기능 제한)
- 위젯 크기 제한으로 인한 텍스트 표시 한계
- 메모리 사용량 고려

### UX 고려사항
- 작은 화면에서 가독성 확보
- 터치 영역 충분히 확보 (최소 48dp)
- 일정이 많은 날짜의 우선순위 표시 규칙

## 테스트 계획

### 단위 테스트
1. `getEventsForDate()` 함수 정확성
2. 긴 이벤트 제목 줄임표 처리
3. 다중 이벤트 표시 레이아웃

### 통합 테스트
1. 실제 ICS 데이터로 위젯 렌더링
2. 다양한 기기 크기에서 확인
3. 라이트/다크 테마 확인

### 사용성 테스트
1. 터치 반응성 확인
2. 가독성 검증
3. 배터리 사용량 체크

## 예상 결과

구현 완료 후 ExtraLarge 위젯에서:
- 월별 달력 그리드 표시 ✅
- 각 날짜에 이벤트 제목 표시 ✅
- example.jpeg와 유사한 UI 제공 ✅
- 터치 시 메인 앱 해당 날짜로 이동 ✅

Medium/Large 위젯에서도 옵션으로 월달력 제공 가능.
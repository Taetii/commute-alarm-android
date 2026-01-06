# Hiworks-checker

Hiworks 출퇴근 자동 체크 앱 (Android)

## 📱 기능

- ⏰ **자동 출퇴근 체크**: 설정한 시간에 Hiworks 앱을 열고 자동으로 출근/퇴근 버튼 클릭
- 📅 **연차 관리**: 연차 등록 시 해당 날짜 자동화 건너뜀
- 🎌 **공휴일 제외**: 한국 공휴일 자동 인식 (2025-2026)
- 🔔 **실패 알림**: 자동화 실패 시 10초마다 알림 (최대 30회, 5분간)

## 📥 설치

### APK 다운로드
[Releases](https://github.com/Taetii/commute-alarm-android/releases)에서 최신 APK 다운로드

### 직접 빌드
```bash
git clone [https://github.com/Taetii/commute-alarm-android.git](https://github.com/Taetii/commute-alarm-android.git)
cd commute-alarm-android
./gradlew assembleDebug
⚙️ 설정
1. 앱 설치
APK 파일을 폰에 설치 (출처를 알 수 없는 앱 허용 필요)

2. 접근성 서비스 활성화
설정 → 접근성 → 설치된 앱 → Hiworks-checker → 활성화
3. Android 14+ 사용자
접근성 서비스가 막힐 경우 ADB 명령어 사용:
adb shell settings put secure enabled_accessibility_services com.taehyeong.commutealarm/com.taehyeong.commutealarm.service.CommuteAccessibilityService
4. 배터리 최적화 제외
설정 → 앱 → Hiworks-checker → 배터리 → 제한 없음
🚀 사용법
앱 실행
출근/퇴근 시간 설정 (기본: 08:30 / 18:30)
"활성화" 토글 켜기
출근 체크하기 / 퇴근 체크하기 버튼으로 테스트
🔧 기술 스택
언어: Kotlin
UI: Jetpack Compose + Material3
저장소: DataStore
자동화: AccessibilityService
스케줄링: AlarmManager
📋 요구사항
Android 8.0 (API 26) 이상
Hiworks 앱 설치 필요
⚠️ 주의사항
접근성 서비스가 활성화되어 있어야 작동합니다
절전 모드에서는 알람이 지연될 수 있습니다
Hiworks 앱 UI가 변경되면 작동하지 않을 수 있습니다
📄 라이선스
MIT License

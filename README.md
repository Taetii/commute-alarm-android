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

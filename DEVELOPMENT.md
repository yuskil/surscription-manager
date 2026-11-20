# 개발 환경
JDK 17 / Gradle 8.13 / AGP 8.13.2 / Kotlin 2.2.21 / Compose / Material 3.
Android 8.0 이상. 첫 화면만 구현하며 DB, 구독 관리, 계산, 알림은 아직 없습니다.
GitHub Actions에서 assembleDebug와 lintDebug 실행 후 디버그 APK를 3일 보관합니다.
Actions → 실행 결과 → Artifacts → subscription-manager-debug ZIP 다운로드 및 해제 → app-debug.apk 설치.
빌드 성공과 실제 기기 실행은 별도 검증합니다. 디버그 APK는 Play 출시용이 아닙니다.
현재 Wrapper는 미포함입니다. PC에서는 Gradle 8.13 설치 후 gradle wrapper로 생성할 수 있습니다.

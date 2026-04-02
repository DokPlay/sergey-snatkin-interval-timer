# sergey-snatkin-interval-timer

[English version](README.md)

Небольшое Android-приложение на Kotlin + Jetpack Compose для загрузки и проигрывания интервальной тренировки по API.

## Что реализовано
- экран загрузки тренировки по ID с состояниями idle/loading/error
- экран тренировки с состояниями idle/running/paused/completed
- последовательное проигрывание интервалов
- пауза, продолжение и сброс тренировки
- звуковые сигналы на старт, переходы между интервалами и завершение
- сохранение прогресса таймера при сворачивании и повторном открытии приложения
- debug APK и unit-тест для расчета снапшота таймера

## Сборка
```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot'
.\gradlew.bat assembleDebug
```

## APK
Готовый debug APK после сборки:
`app/build/outputs/apk/debug/app-debug.apk`

## Пакет приложения
`com.sergeysnatkin.intervaltimer`

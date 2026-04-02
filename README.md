# sergey-snatkin-interval-timer

[Русская версия](README.ru.md)

An Android interval timer app built with Kotlin and Jetpack Compose.

## What it does
- loads a workout by ID from the API
- shows loading, error, idle, running, paused, and completed states
- plays workout intervals sequentially
- supports pause, resume, and reset
- plays sound cues at the start, between intervals, and at the end
- keeps timer progress after the app is backgrounded and restored

## Build
```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot'
.\gradlew.bat assembleDebug
```

## Debug APK
After building, the APK is available at:
`app/build/outputs/apk/debug/app-debug.apk`

## Package name
`com.sergeysnatkin.intervaltimer`

name: Build Jarvis APK

on:
  push:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Setup Gradle
      uses: gradle/gradle-build-action@v2
    - name: Build APK
      run: ./gradlew assembleRelease
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: jarvis-app
        path: app/build/outputs/apk/release/app-release-unsigned.apk

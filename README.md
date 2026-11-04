# ⚽ FTBL Chats — The Football Supporter Messaging & Matchday Hub

[![Build Status](https://github.com/OWNER/ftbl-chats/actions/workflows/build-apk.yml/badge.svg)](https://github.com/OWNER/ftbl-chats/actions/workflows/build-apk.yml)
[![Release](https://img.shields.io/github/v/release/OWNER/ftbl-chats?include_prereleases&color=004AC6&label=Release)](https://github.com/OWNER/ftbl-chats/releases)
[![Android](https://img.shields.io/badge/Android-7.0%2B%20%28API%2024--36%29-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Material 3](https://img.shields.io/badge/Design-Material%203-004AC6)](https://m3.material.io)

**FTBL Chats** is a native Android real-time messaging, matchday status, and HD calling application built exclusively for football supporters worldwide. Discuss derby matches, debate referee calls, celebrate last-minute winners, and share football banter with fellow fans in high speed and privacy.

---

## 📥 Download the Latest APK

You can download the ready-to-install Android APK directly from our GitHub Releases:

👉 **[Download FTBL Chats APK (Latest Release)](https://github.com/OWNER/ftbl-chats/releases/latest)**

### 📱 Installation Instructions (Android)
1. Download the `FTBL-Chats-vX.X.X.apk` file to your Android phone.
2. Open your device's **Downloads** folder or tap the download notification.
3. If prompted: **"For your security, your phone is not allowed to install unknown apps from this source"**:
   - Tap **Settings** in the prompt.
   - Toggle **"Allow from this source"** to ON.
   - Tap **Back** and select **Install**.
4. Open **FTBL Chats**, sign in seamlessly using Google Sign-In, select your football club, and enjoy!

---

## ⚡ Core Features

### 1. 💬 Live Matchday Messaging
- **1-on-1 Direct Chats:** Fast, real-time private conversations with fellow fans.
- **Squad Groups:** Create dedicated squad chats for your club supporters, fantasy leagues, and match viewing parties.
- **Message Controls:** Pin important chats to the top, mute noisy groups, archive old threads, or delete conversations.
- **Search & Filters:** Instant search across fan names, squad messages, and club conversations with one-tap filter chips (*All*, *Groups*, *Pinned*, *Unread*).
- **Rich Media & Reactions:** Voice notes, match photos, emoji reactions, and reply threads.

### 2. ⏱️ Matchday Status (Stories)
- **24-Hour Disappearing Stories:** Share matchday updates, pre-match excitement, stadium photos, and post-match reactions.
- **Story Carousel:** Stories preview bar directly above your chat list.
- **Interactive Story Viewer:** Story progress bar timer, fan avatar, club badge, and direct reply-to-chat capabilities.

### 3. 📞 HD Voice & Video Calling
- **Supporter Calls:** Direct peer voice and video calls powered by Firestore real-time signaling.
- **In-Call Controls:** Mute microphone, flip camera, toggle video on/off, and floating picture-in-picture preview.
- **Call History:** Track incoming, outgoing, and missed supporter calls with one-tap callback.

### 4. 🛡️ Supporter Profiles & Allegiance
- **Club Badging:** Select your club allegiance (Arsenal, Manchester City, Liverpool, Real Madrid, Barcelona, Bayern Munich, PSG, Juventus, and more).
- **Custom Bio & Username:** Display your unique fan handle and bio.
- **Online Presence:** Live indicator when fans are online during matchdays.

### 5. 🔒 Security & Moderation
- **Google Sign-In:** Secure authentication using Jetpack `CredentialManager`.
- **Zero-Trust Security:** Fine-grained Firebase Firestore rules enforcing user ownership and chat membership.
- **Supporter Safety:** Block toxic users, report inappropriate behavior, and self-service account deletion.

---

## 🛠️ Architecture & Tech Stack

FTBL Chats is built following modern Android Architecture best practices:

- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Theme:** *Pitch Precision* palette (Deep Stadium Navy `#00174B`, Electric Pitch Blue `#004AC6`, Match Lime `#00D26A`)
- **State Management:** MVVM with Kotlin Coroutines & `StateFlow`
- **Data Persistence & Real-Time Sync:** Cloud Firestore with real-time snapshot listeners
- **Authentication:** Firebase Authentication with Jetpack `CredentialManager`
- **Build System:** Gradle (Kotlin DSL - `.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 🚀 Building from Source

### Prerequisites
- **Android Studio:** Ladybug (2024.2+) or newer
- **JDK:** Version 17
- **Android SDK:** API Level 36 (compiled), minimum SDK API 24 (Android 7.0 Nougat)

### Build Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/OWNER/ftbl-chats.git
   cd ftbl-chats
   ```

2. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```
   The compiled APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Run Unit Tests:**
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Install onto a connected Android device or emulator:**
   ```bash
   ./gradlew installDebug
   ```

---

## 🤖 Continuous Integration & Releases (GitHub Actions)

This repository includes two automated GitHub Actions workflows:

### 1. `Build Android APK` (`.github/workflows/build-apk.yml`)
- **Triggers:** Automatically on every push or pull request to the `main` branch, or via manual trigger (`workflow_dispatch`).
- **Function:** Runs unit tests, compiles the debug APK, and uploads it as a downloadable build artifact in the Actions run summary.

### 2. `Release Android APK` (`.github/workflows/release-apk.yml`)
- **Triggers:** Automatically when a new version tag (e.g. `v1.0.0`, `v1.0.1`, `v1.1.0`) is pushed to the repository:
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```
- **Function:** Compiles the release APK, signs it, creates a formal GitHub Release, and attaches the `FTBL-Chats-vX.X.X.apk` binary for public download.

### Optional: Configuring Custom Release Keystore in GitHub Secrets
To sign the release APK with your custom production keystore, add the following secrets in your repository settings (**Settings > Secrets and variables > Actions**):

| Secret Name | Description |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded string of your `.jks` or `.keystore` file (`base64 -w 0 my-upload-key.jks`) |
| `STORE_PASSWORD` | Password for the keystore |
| `KEY_PASSWORD` | Password for the signing key |

*(Note: If these secrets are not provided, the release workflow automatically falls back to generating a valid standalone signing key so builds never fail.)*

---

## 📄 License
This project is open-source and distributed under the Apache License 2.0.

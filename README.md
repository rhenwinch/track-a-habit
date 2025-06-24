# Track a Habit

![Track a Habit logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

A modern habit tracking app for Android

<div>
  <a href="https://github.com/rhenwinch/track-a-habit/releases/latest">
    <img src="https://img.shields.io/github/downloads/rhenwinch/track-a-habit/latest/total?style=for-the-badge&label=DOWNLOAD+LATEST+VERSION" alt="Download latest version">
  </a>
</div>

<div>
    <a href="https://github.com/rhenwinch/track-a-habit/actions/workflows/unit-tests.yaml">
        <img src="https://img.shields.io/github/actions/workflow/status/rhenwinch/track-a-habit/unit-tests.yaml?branch=master&style=flat-square&label=Unit+tests" alt="Unit tests">
    </a>
</div>

<div>
    <a href="https://github.com/rhenwinch/track-a-habit/actions/workflows/instrumented-tests.yaml">
        <img src="https://img.shields.io/github/actions/workflow/status/rhenwinch/track-a-habit/instrumented-tests.yaml?branch=master&style=flat-square&label=Instrumented+tests" alt="Instrumented tests">
    </a>
</div>

<div>
    <a href="https://github.com/rhenwinch/track-a-habit/actions/workflows/pre-merge.yaml">
        <img src="https://img.shields.io/github/actions/workflow/status/rhenwinch/track-a-habit/pre-merge.yaml?branch=master&style=flat-square&label=Pre-merge" alt="Pre-merge check">
    </a>
</div>

---

[Screenshots](#screenshots) • [Overview](#overview) • [Features](#features) • [Roadmap](#roadmap) • [Tech stack](#tech-stack) • [Architecture](#architecture) • [Development](#development) • [Contributing](#contributing)

> *This README is written by an AI!!!!*

## Screenshots

<div>
  <img src="docs/onboarding_screen.png" alt="Onboarding Screen" width="250" />
  <img src="docs/habits_screen.png" alt="Habits Screen" width="250" />
  <img src="docs/streaks_screen.png" alt="Streaks Screen" width="250" />
  <img src="docs/settings_screen.png" alt="Settings Screen" width="250" />
</div>

## Overview

*Track a Habit* is a minimalist yet powerful habit tracking application designed to help users build and maintain
positive habits through an elegant, intuitive interface. The app combines beautiful design with powerful functionality to make habit tracking a seamless part of your daily routine.

## Features

- **Habit management**: Create, edit, and delete habits with customizable settings
- **Streak tracking**: Visualize your progress with streak counters and calendar views
- **Reminders**: Get notified at your preferred times to if you are about to achieve a new streak
- **Statistics**: Detailed insights and analytics about your habit performance
- **Adaptive UI mode**: Automatically adjusts to your system theme preference
- **Offline support**: Full functionality without an internet connection
- **Cloud backup**: Securely back up your data to Google Drive
- **Privacy-focused**: Your habits can be censored so that they are not visible to anyone else

## Roadmap

Current development priorities:

- [x] Google drive backup implementation
- [x] Re-design implementation of `Streaks` to support i18n in the future
- [ ] Google drive auto backup using `WorkManager`
- [ ] Re-design implementation of `Quotes` to support i18n in the future
- [ ] Add more statistics and insights
- [ ] Add widget support

## Tech stack

*Track a Habit* is built with modern Android development tools and practices.

### UI & Navigation
- Jetpack Compose
- Material 3
- Navigation Compose *(will be using nav3 when it is stable)*

### DI
- Hilt

### Local Storage
- Room Database
- DataStore Preferences

### Authentication & Cloud
- Google Drive API
- Biometric Authentication
- Credentials Manager

### Background Processing
- WorkManager

### Testing
- JUnit
- MockK
- Strikt
- Turbine

### Code Quality
- Detekt

## Architecture

*Track a Habit* follows Android-recommended architecture principles with MVVM presentation pattern, structured into three main layers: Presentation, Domain, and Data.

## Development

### From GitHub Releases

1. Download the latest APK from the [Releases](https://github.com/rhenwinch/track-a-habit/releases) page
2. Install the APK on your Android device

### From Source

1. Clone the repository
   ```bash
   git clone https://github.com/rhenwinch/track-a-habit.git
   ```
2. Open the project in Android Studio
3. Build and run the app on your device or emulator

## Contributing

Contributions are welcome! Please feel free to submit pull request.

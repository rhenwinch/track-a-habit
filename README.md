# Track a Habit

![Track a Habit logo](app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp)

A modern habit tracking app for Android

<div>
  <table>
    <tr>
      <td>Unit tests</td>
      <td><a href="https://github.com/rhenwinch/*Track a Habit*/actions/workflows/unit-tests.yaml"><img src="https://img.shields.io/github/actions/workflow/status/rhenwinch/*Track a Habit*/unit-tests.yaml?branch=master&style=for-the-badge&label=Unit+tests" alt="Unit tests"></a></td>
    </tr>
    <tr>
      <td>Instrumented tests</td>
      <td><a href="https://github.com/rhenwinch/*Track a Habit*/actions/workflows/instrumented-tests.yaml"><img src="https://img.shields.io/github/actions/workflow/status/rhenwinch/*Track a Habit*/instrumented-tests.yaml?branch=master&style=for-the-badge&label=Instrumented+tests" alt="Instrumented tests"></a></td>
    </tr>
    <tr>
      <td>Pre-merge check</td>
      <td><a href="https://github.com/rhenwinch/*Track a Habit*/actions/workflows/pre-merge.yaml"><img src="https://img.shields.io/github/actions/workflow/status/rhenwinch/*Track a Habit*/pre-merge.yaml?branch=master&style=for-the-badge&label=Pre-merge" alt="Pre-merge check"></a></td>
    </tr>
    <tr>
      <td>Stable</td>
      <td><a href="https://github.com/rhenwinch/*Track a Habit*/releases/latest"><img src="https://img.shields.io/github/downloads/rhenwinch/*Track a Habit*/latest/total?style=for-the-badge" alt="stable release"></a></td>
    </tr>
  </table>
</div>

[Features](#features) • [Screenshots](#screenshots) • [Tech Stack](#tech-stack) • [Todo List](#todo-list) • [Architecture](#architecture) • [Installation](#installation) • [Contributing](#contributing)

> *This README is written by an AI!!!!*

## Overview

*Track a Habit* is a minimalist yet powerful habit tracking application designed to help users build and maintain positive habits through an elegant, intuitive interface. The app combines beautiful design with powerful functionality to make habit tracking a seamless part of your daily routine.

## Features

- **Habit Management**: Create, edit, and delete habits with customizable settings
- **Streak Tracking**: Visualize your progress with streak counters and calendar views
- **Reminders**: Get notified at your preferred times to if you are about to achieve a new streak
- **Statistics**: Detailed insights and analytics about your habit performance
- **Adaptive UI Mode**: Automatically adjusts to your system theme preference
- **Offline Support**: Full functionality without an internet connection
- **Cloud Backup**: Securely back up your data to Google Drive
- **Privacy-Focused**: Your habits can be censored so that they are not visible to anyone else

## Screenshots

<div align="center">
  <img src="docs/empty_screen_preview.png" alt="Empty Screen" width="250" />
  <img src="docs/habits_screen.png" alt="Habits Screen" width="250" />
  <img src="docs/streaks_screen.png" alt="Streaks Screen" width="250" />
</div>

## Tech Stack

*Track a Habit* is built with modern Android development tools and practices:

### UI & Architecture
- **Jetpack Compose**: Declarative UI toolkit for native UI
- **Material 3**: Latest Material Design components and theming
- **MVVM Architecture**: Clean separation of UI, business logic, and data

## Todo List

Current development priorities:
- [x] Google drive backup implementation
- [ ] Google drive auto backup using WorkManager
- [ ] Re-design implementation of Streaks and Quotes (hardcode it)

## Architecture

*Track a Habit* follows Android-recommended architecture principles with _MVVM_ presentation pattern.

The app is structured into three main layers:
- **Presentation Layer**: Contains UI components, ViewModels, and navigation logic. Uses Jetpack Compose for building the UI.
- **Domain Layer**: Contains business logic, use cases, and interfaces. This layer is independent of any framework or library
- **Data Layer**: Handles data sources, repositories, and data persistence. It abstracts the details of how data is fetched and stored.

### Development Approach

The project is built using a bottom-up development approach:

1. **Data Layer First**: Repository interfaces and implementations are developed and tested thoroughly
2. **Domain Layer Second**: Use cases are implemented with comprehensive test coverage
3. **Presentation Layer Last**: UI components are created only after the underlying business logic is solid

This approach ensures:
- **Testability**: Core business logic is fully tested independent of UI
- **Stability**: The foundation is solid before UI development begins
- **Flexibility**: UI can evolve without breaking the underlying functionality

All modules are developed with comprehensive unit tests before moving up the stack, ensuring the app is built on a solid foundation.

## Installation

### From GitHub Releases
1. Download the latest APK from the [Releases](https://github.com/rhenwinch/*Track a Habit*/releases) page
2. Install the APK on your Android device

### From Source
1. Clone the repository
   ```bash
   git clone https://github.com/rhenwinch/*Track a Habit*.git
   ```
2. Open the project in Android Studio
3. Build and run the app on your device or emulator

## Contributing

Contributions are welcome! Please feel free to submit pull request.

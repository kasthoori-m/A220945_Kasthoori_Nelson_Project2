# EduQuest — Project 2

## Project Description

EduQuest is a gamified learning companion app built with Jetpack Compose and Kotlin. It transforms independent university study into an RPG-style experience — students earn XP, build daily streaks, unlock dynamic titles, complete interactive quizzes, and create their own custom "Side Quests."

**Project 2** extends the original 5-screen app into a fully connected, persistent, and hardware-aware application with **7 screens** and **four advanced technical pillars**: local persistence (Room), cloud sync (Firebase Firestore), live internet data (Retrofit + OpenStreetMap Overpass API), and hardware sensor integration (GPS/Location).

## SDG Theme

**SDG 4 — Quality Education**

Project 2 continues EduQuest's mission of helping students build consistent, motivated study habits — now connected to the real world. The app rewards students for studying on campus (GPS), helps them discover real study spaces nearby (REST API), lets them compete on a shared leaderboard (Firebase), and never loses their progress (Room).

## Features

### Carried over from Project 1
- Profile Setup, Dashboard, Quest Details (quizzes), Active Quests, Create Quest
- XP, streaks, dynamic titles, and Citra elective courses
- Program-specific core courses (Software Engineering, Computer Science, Information Technology)

### New in Project 2
- **Campus Check-In (Screen 6)** — uses the device's GPS sensor via Fused Location Provider to detect if the student is within 1km of UKM campus. A successful check-in awards +50 XP once per day and is saved locally.
- **Study Map (Screen 7)** — fetches nearby libraries, cafes, universities, and parks in real time from the OpenStreetMap Overpass API via Retrofit, and displays a Campus Leaderboard synced from Firebase Firestore.
- **Room Database (local persistence)** — student profiles, XP, streaks, course progress, and check-in history are saved permanently on-device. Multiple students can use the same device — logging in with a matric number restores that student's saved progress.
- **Firebase Firestore (cloud)** — every campus check-in pushes the student's latest XP and title to a shared `campus_leaderboard` collection, visible to all students in real time.
- **Logout vs Reset** — Logout preserves a student's data for next login; Reset permanently deletes it (including syncing the reset to Firebase).

## Technical Pillars Summary

| Pillar | Implementation |
|---|---|
| Local Persistence | Room (`CheckInRecord`, `UserProfileEntity`, DAOs, `EduQuestDatabase`) |
| Cloud Integration | Firebase Firestore (`FirebaseRepository`, `campus_leaderboard` collection) |
| Internet Data (REST API) | Retrofit + OkHttp → OpenStreetMap Overpass API (`OverpassApiService`) |
| Hardware Sensor | GPS / Fused Location Provider (`LocationHelper`) |

## Tech Stack

- Kotlin, Jetpack Compose (Material 3)
- Navigation Compose
- ViewModel (AndroidViewModel) + StateFlow + Coroutines
- Room (KSP)
- Retrofit2 + Gson + OkHttp
- Firebase Firestore
- Google Play Services Location (Fused Location Provider)

## Setup Instructions

1. Clone this repository:
   ```
   git clone https://github.com/kasthoori-m/A220945_Kasthoori_Nelson_Project2.git
   ```
2. Open the project in **Android Studio** (latest stable version recommended).
3. **Firebase setup (required):**
   - Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Add an Android app with package name `com.example.a220945_kasthoori_nelson_project2`
   - Download `google-services.json` and place it in the `app/` folder
   - Enable Firestore Database (test mode is sufficient for development)
4. Let Gradle sync — Room, Retrofit, and Firebase dependencies will download automatically.
5. Run on a **physical device** (recommended for GPS testing — emulator GPS can be set manually via Extended Controls → Location).
6. Grant location permission when prompted on the Campus Check-In screen.

## Author

**Kasthoori Mohan** (A220945) —
Mobile Application Programming — Instructor: Mr Nelson

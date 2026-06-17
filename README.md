# SnapJournal

SnapJournal is a photo and video journal app for Android. Users can capture a moment, add a title and notes, and save the entry locally on the device.

## Videos
- https://youtu.be/bAPRZHYzqgc
- https://youtube.com/shorts/cFW67FXxU38?feature=share

## App 2 Requirements Checklist

- Camera: CameraX photo capture, video recording, front/back camera switching, and flash/torch support.
- Internal Storage: captured media is saved in app-specific storage.
- Room Database: journal metadata is stored in a Room table.
- Create Journal Entries: users can capture media, enter a title and notes, and save.
- Edit Journal Entries: users can update the title and notes.
- Delete Journal Entries: users can delete the Room entry and its saved media file.

## Main Features

- Scrollable home screen of saved journal entries.
- Each row shows a thumbnail, title, and date.
- New Journal Entry screen with Photo and Video modes.
- Photo capture with optional flash.
- Video recording with optional torch.
- Front/back camera switching.
- Preview before saving.
- Retake before saving.
- Detail screen with full photo or video playback.
- Edit screen for title and notes.
- Delete confirmation.

## Storage Design

Media files are stored in app-specific internal storage:

- Photos: `Photo_YYYYMMDD_HHMMSS_SSS.jpg`
- Videos: `Video_YYYYMMDD_HHMMSS_SSS.mp4`

Room stores the metadata in `JournalEntry`:

| Field | Purpose |
| --- | --- |
| `id` | Unique entry ID |
| `title` | Journal title |
| `description` | Notes |
| `imagePath` | Internal storage path for the photo or video |
| `date` | Save date/time |
| `mediaType` | `PHOTO` or `VIDEO` |

## Tutorial Requirement

This app follows the structure from Google's official Room tutorial:

https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room

The app adapts that tutorial's Room pattern into:

- `JournalEntry`
- `JournalEntryDao`
- `SnapJournalDatabase`
- `JournalEntryRepository`
- `JournalViewModel`

## Important Files

- `app/src/main/java/com/example/snapjournal/ui/SnapJournalApp.kt`
- `app/src/main/java/com/example/snapjournal/ui/JournalViewModel.kt`
- `app/src/main/java/com/example/snapjournal/data/JournalEntry.kt`
- `app/src/main/java/com/example/snapjournal/data/JournalEntryDao.kt`
- `app/src/main/java/com/example/snapjournal/data/SnapJournalDatabase.kt`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`

## How to Use

1. Open the app.
2. Tap `New Journal Entry`.
3. Choose `Photo` or `Video`.
4. Use `Front Camera` / `Back Camera` if needed.
5. Use `Flash On` / `Flash Off` if needed.
6. Capture a photo or record a video.
7. Preview the media.
8. Tap `Retake` or `Save`.
9. Add a title and notes before saving.
10. Tap an entry on the home screen to view it.
11. Use edit/delete from the detail screen.

## AI Feature-Addition Proof

### App

SnapJournal

### Feature Added

Video recording and flash/torch support were added to the existing camera journal app.

### Prompt Used

```text
how do i take a video i only see a picture option

yes please and please allow flash aswell
```

### AI-Generated Idea

The AI suggested expanding the CameraX setup from photo-only capture to mixed photo/video capture by adding CameraX VideoCapture, a media type field in Room, a Photo/Video mode selector, and a flash toggle. The AI also suggested saving videos to internal storage and using Room to track whether each entry is a photo or video.

### Code Added

Summary of the final code:

- Added `androidx.camera:camera-video`.
- Added `MediaType` with `PHOTO` and `VIDEO`.
- Added `mediaType` to the `JournalEntry` Room entity.
- Added a Room migration from database version 1 to version 2.
- Added video recording with `Recorder`, `VideoCapture`, `Recording`, and `FileOutputOptions`.
- Added `Photo` and `Video` mode buttons.
- Added `Record Video` and `Stop Recording`.
- Added flash support:
  - photo mode uses CameraX image flash mode
  - video mode uses torch
- Added video thumbnails using `MediaMetadataRetriever`.
- Added video playback on the detail screen with `VideoView`.

### Screenshot Slots

Add screenshots here before submitting:

- Home screen showing saved entries.
- New Journal Entry screen with `Photo` and `Video` buttons.
- Video mode with `Record Video`.
- Flash toggle visible.
- Saved video entry on the detail screen.

### Short Reflection

AI helped turn a photo-only journal into a richer media journal. The most useful part was getting a clear plan for where the feature needed changes: Gradle dependencies, Room data model, camera UI, CameraX recording code, internal storage, and video playback. I still had to test the app and understand how the new feature fit the assignment requirements.

## AI Bug-Fix Proof

### App

SnapJournal

### Bug

The app hit Android build configuration problems while adding the required libraries.

### What Was Broken

One build error said:

```text
Dependency 'androidx.core:core-ktx:1.19.0' requires libraries and applications
that depend on it to compile against version 37 or later of the Android APIs.

:app is currently compiled against android-36.1.
```

A later attempted fix caused another build error:

```text
Cannot add extension with name 'kotlin', as there is an extension already registered with that name.
```

### Prompt to AI

```text
2 issues were found when checking AAR metadata:
Dependency 'androidx.core:core-ktx:1.19.0' requires ... compileSdk 37

Build file ... Failed to apply plugin 'org.jetbrains.kotlin.android'
Cannot add extension with name 'kotlin'
```

### AI-Suggested Fix

The AI suggested:

- Updating `compileSdk` to API 37.
- Leaving `targetSdk` and `minSdk` unchanged.
- Removing the extra `org.jetbrains.kotlin.android` plugin because AGP already had a Kotlin extension.
- Restoring `android.disallowKotlinSourceSets=false` because it is currently needed for KSP/Room generated sources in this project setup.

### Final Fix Made

The final build configuration:

- `compileSdk` uses API 37.
- `targetSdk` remains 36.
- `minSdk` remains 24.
- The duplicate Kotlin Android plugin was removed.
- KSP remains configured for Room.
- The experimental Kotlin source set workaround remains because it avoids a harder Room/KSP generated-source failure.

### Screenshot Slots

Add screenshots here before submitting:

- Screenshot of the original build error.
- Screenshot after Gradle sync/build gets past the compileSdk error.
- Screenshot of the app running.

### Short Reflection

AI helped identify the difference between a warning and a real build failure. The compile SDK mismatch needed a real configuration change, while the experimental Kotlin source set message was only a warning. The duplicate Kotlin plugin caused a hard failure, so reverting that change was the correct fix.

# SnapJournal Tutorial Requirement

SnapJournal follows Google's official Android Developers Room tutorial pattern:

https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room

The app adapts the codelab's Room structure to a photo journal:

- `JournalEntry` is the Room entity/table.
- `JournalEntryDao` provides insert, update, delete, single-entry lookup, and list queries.
- `SnapJournalDatabase` creates the Room database.
- `JournalEntryRepository` keeps database access separate from the Compose UI.

SnapJournal stores each captured photo or video in app-specific internal storage
with `Photo_YYYYMMDD_HHMMSS_SSS.jpg` and `Video_YYYYMMDD_HHMMSS_SSS.mp4`
style filenames. Room stores the matching metadata:

- `id`
- `title`
- `description` / notes
- `imagePath`
- `date`
- `mediaType`

Camera capture is implemented with CameraX and supports photo capture, video
recording, front/back camera switching, flash/torch, previewing the captured
media, retaking it, and saving the final journal entry.

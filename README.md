# MARU VIDEOEDITOR

Native Android video-editing MVP built with Kotlin, Jetpack Compose, and AndroidX Media3.

## Working features

- Import a video using Android's system document picker (no broad media permission request).
- Preview the clip, play/pause, seek through it, and adjust preview speed.
- Select an in/out trim range and export the selected section as MP4.
- Optionally remove the source video's audio during export.
- Save the current source, trim range, mute option, and caption text locally and restore the project on the next launch (while the Android document permission remains available).
- Add a caption overlay to the preview.
- Android 10+ exports to `Movies/MARU VIDEOEDITOR`.

## Build and run

Open in Android Studio with JDK 17, sync Gradle, and run the `app` configuration on a physical device or emulator. Export compatibility depends on device codecs and source video formats.

## Not yet implemented

This is not feature-parity with CapCut. The caption currently appears in the preview only and is not included in the exported file. Multiple clips, split/ripple editing, music-track mixing, filters, transitions, crop/keyframes, thumbnails, project library, background export, and configurable resolution/bitrate still need implementation. The app has not been built or tested on a device in this environment.

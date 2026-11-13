# MARU VIDEOEDITOR

An Android video editing starter app built with Kotlin, Jetpack Compose, and Media3.

## Current editing workflow

- Import a video using Android's system document picker.
- Preview and play/pause the selected clip.
- Choose an in/out range using the timeline trim slider.
- Optionally remove the source video's audio.
- Export the trimmed video as MP4. Android 10+ saves it to `Movies/MARU VIDEOEDITOR`.

## Open and run

Open this repository in Android Studio (JDK 17), let Gradle sync, then run the `app` configuration on an Android device or emulator. The app uses Android's system picker, so it does not request broad media-library permissions.

## Current limitations

This is a functional editing MVP, not feature parity with CapCut. Split editing, text overlays, filters, transitions, music tracks, project persistence, and advanced export settings are not implemented yet; those controls are intentionally shown as unavailable rather than pretending to edit the video. Video transcoding support depends on the device's Media3 codecs and the source format.

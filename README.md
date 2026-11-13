# MARU VIDEOEDITOR

A real Android video editor milestone built with Kotlin, Jetpack Compose, and AndroidX Media3.

## What works now

- Import multiple videos through the document picker.
- Build a sequence of clips in a timeline.
- Preview the sequence in order.
- Trim, split, reorder, and remove clips.
- Save the current project state locally and restore it when the Android document access remains valid.
- Export the currently selected trimmed clip as MP4 to `Movies/MARU VIDEOEDITOR` on Android 10+.

## Build and run

1. Open this repo in Android Studio.
2. Use JDK 17.
3. Let Gradle sync.
4. Run the `app` configuration on an emulator or physical device.

## Important limitations

This is not a full CapCut clone. It is a working multi-clip editor milestone that demonstrates real timeline operations and selected-clip export. Advanced features such as multi-track audio, visual effects, transitions, burn-in text, project library management, and full stitched-project export are still future milestones.

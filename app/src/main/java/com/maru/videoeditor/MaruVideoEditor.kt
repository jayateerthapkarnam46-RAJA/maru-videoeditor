package com.maru.videoeditor

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Locale

private val Ink = Color(0xFF10111A)
private val Panel = Color(0xFF1B1D29)
private val Muted = Color(0xFFA9A9BC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaruVideoEditor() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var videoName by remember { mutableStateOf("Your next story") }
    var durationMs by remember { mutableLongStateOf(0L) }
    var playbackPosition by remember { mutableLongStateOf(0L) }
    var trimStart by remember { mutableFloatStateOf(0f) }
    var trimEnd by remember { mutableFloatStateOf(1f) }
    var isPlaying by remember { mutableStateOf(false) }
    var muteOriginal by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Import a video to start editing") }
    var exportProgress by remember { mutableFloatStateOf(0f) }

    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && player.duration > 0) {
                    durationMs = player.duration
                    trimEnd = 1f
                }
            }
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player, videoUri) {
        while (true) {
            playbackPosition = player.currentPosition.coerceAtLeast(0L)
            delay(250)
        }
    }

    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }
            videoUri = uri
            videoName = queryDisplayName(context, uri) ?: "Imported video"
            durationMs = 0L
            trimStart = 0f
            trimEnd = 1f
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            statusMessage = "Video ready — set your trim and export"
        }
    }

    Scaffold(containerColor = Ink) { padding ->
        Column(
            Modifier.fillMaxSize().background(Ink).padding(padding).padding(horizontal = 18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MARU", color = Color.White, fontWeight = FontWeight.Black, fontSize = 25.sp, letterSpacing = 2.sp)
                    Text("VIDEOEDITOR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.sp)
                }
                Button(
                    onClick = {
                        if (videoUri == null || exporting) return@Button
                        exporting = true
                        exportProgress = 0f
                        statusMessage = "Preparing your export…"
                        scope.launch {
                            try {
                                exportTrimmedVideo(context, videoUri!!, trimStart, trimEnd, durationMs, muteOriginal,
                                    onProgress = { exportProgress = it },
                                    onComplete = { savedUri ->
                                        exporting = false
                                        statusMessage = "Export saved to Movies/MARU VIDEOEDITOR"
                                    },
                                    onError = { message ->
                                        exporting = false
                                        statusMessage = "Export failed: $message"
                                    })
                            } catch (e: Exception) {
                                exporting = false
                                statusMessage = "Export failed: ${e.localizedMessage ?: "Unknown error"}"
                            }
                        }
                    },
                    enabled = videoUri != null && !exporting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (exporting) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (exporting) "Exporting" else "Export")
                }
            }

            Box(
                Modifier.fillMaxWidth().weight(0.9f).clip(RoundedCornerShape(22.dp)).background(Color(0xFF08090F)),
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { viewContext -> PlayerView(viewContext).apply { this.player = player; useController = true } },
                        update = { it.player = player }
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(76.dp).clip(CircleShape).background(Color(0xFF252132)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Your video preview", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Import a clip to begin", color = Muted, fontSize = 13.sp)
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(videoName, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.weight(1f))
                Text("${formatTime(playbackPosition)} / ${formatTime(durationMs)}", color = Muted, fontSize = 12.sp)
            }

            Surface(Modifier.fillMaxWidth().weight(0.72f), color = Panel, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("TIMELINE", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Drag handles to choose a clip", color = Muted, fontSize = 12.sp)
                        }
                        IconButton(onClick = { pickVideo.launch(arrayOf("video/*")) }) {
                            Icon(Icons.Default.Add, contentDescription = "Import video", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatTime((durationMs * trimStart).toLong()), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        RangeSlider(
                            value = trimStart..trimEnd,
                            onValueChange = { range -> trimStart = range.start; trimEnd = range.endInclusive },
                            valueRange = 0f..1f,
                            enabled = durationMs > 0 && !exporting,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(formatTime((durationMs * trimEnd).toLong()), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${formatTime((durationMs * (trimEnd - trimStart)).toLong())} selected", color = Muted, fontSize = 12.sp)
                        Text("${formatTime(durationMs)} total", color = Muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToolButton("Split", Icons.Default.ContentCut, enabled = false) { }
                        ToolButton(if (muteOriginal) "Muted" else "Audio", Icons.Default.MusicNote, enabled = videoUri != null) { muteOriginal = !muteOriginal }
                        ToolButton("Text", Icons.Default.TextFields, enabled = false) { }
                        ToolButton("Filters", Icons.Default.Tune, enabled = false) { }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { if (videoUri == null) pickVideo.launch(arrayOf("video/*")) else { player.seekTo((durationMs * trimStart).toLong()); player.playWhenReady = !player.isPlaying } },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (videoUri == null) "Import video" else if (isPlaying) "Pause" else "Play")
                        }
                        TextButton(onClick = { player.seekTo((durationMs * trimStart).toLong()) }, enabled = videoUri != null) {
                            Text("Reset preview", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    if (exporting) {
                        Slider(value = exportProgress, onValueChange = {}, enabled = false, modifier = Modifier.height(24.dp))
                    }
                    Text(statusMessage, color = if (statusMessage.startsWith("Export failed")) Color(0xFFFF8B8B) else Muted, fontSize = 12.sp, maxLines = 2)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ToolButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f),
        color = if (enabled) Color(0xFF292B3B) else Color(0xFF20212C),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = if (enabled) MaterialTheme.colorScheme.secondary else Muted, modifier = Modifier.size(19.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = if (enabled) Color.White else Muted, fontSize = 11.sp)
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000).toInt()
    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}

private suspend fun exportTrimmedVideo(
    context: Context,
    source: Uri,
    startFraction: Float,
    endFraction: Float,
    durationMs: Long,
    removeAudio: Boolean,
    onProgress: (Float) -> Unit,
    onComplete: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    if (durationMs <= 0L) throw IOException("Video duration is not available yet")
    val startMs = (durationMs * startFraction).toLong().coerceAtLeast(0L)
    val endMs = (durationMs * endFraction).toLong().coerceAtMost(durationMs)
    if (endMs - startMs < 500L) throw IOException("Select a clip that is at least half a second long")

    val output = File(context.cacheDir, "maru_export_${System.currentTimeMillis()}.mp4")
    val mediaItem = MediaItem.Builder()
        .setUri(source)
        .setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                .setStartPositionMs(startMs)
                .setEndPositionMs(endMs)
                .build()
        )
        .build()
    val editedItem = EditedMediaItem.Builder(mediaItem).setRemoveAudio(removeAudio).build()

    val transformer = Transformer.Builder(context)
        .addListener(object : Transformer.Listener {
            override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                try {
                    onProgress(1f)
                    onComplete(copyToMovies(context, output))
                } catch (e: Exception) {
                    onError(e.localizedMessage ?: "Could not save exported video")
                } finally {
                    output.delete()
                }
            }

            override fun onError(composition: androidx.media3.transformer.Composition, exportResult: ExportResult, exportException: ExportException) {
                output.delete()
                onError(exportException.localizedMessage ?: "Media export failed")
            }
        })
        .build()

    transformer.start(editedItem, output.absolutePath)
    // Media3's Transformer reports progress through its listener; keep the UI responsive while it runs.
    while (!output.exists() || output.length() == 0L) {
        delay(300)
        onProgress(0.25f)
    }
}

private fun copyToMovies(context: Context, file: File): Uri {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "MARU_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MARU VIDEOEDITOR")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create video in Movies")
        try {
            context.contentResolver.openOutputStream(uri)?.use { output -> FileInputStream(file).use { it.copyTo(output) } }
                ?: throw IOException("Unable to open exported video")
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            return uri
        } catch (e: Exception) {
            context.contentResolver.delete(uri, null, null)
            throw e
        }
    }
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
    val target = File(directory, "MARU_${System.currentTimeMillis()}.mp4")
    FileInputStream(file).use { input -> target.outputStream().use { input.copyTo(it) } }
    return Uri.fromFile(target)
}

package com.maru.videoeditor

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
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
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val EditorBackground = Color(0xFF10111A)
private val EditorPanel = Color(0xFF1B1D29)
private val EditorMuted = Color(0xFFA9A9BC)
private const val PROJECT_PREFS = "maru_editor_project"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaruVideoEditor() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var videoName by remember { mutableStateOf("Untitled project") }
    var durationMs by remember { mutableLongStateOf(0L) }
    var playheadMs by remember { mutableLongStateOf(0L) }
    var trimStart by remember { mutableFloatStateOf(0f) }
    var trimEnd by remember { mutableFloatStateOf(1f) }
    var isPlaying by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var overlayText by remember { mutableStateOf("") }
    var editingText by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Import a video to start a project") }

    val player = remember(context) { ExoPlayer.Builder(context).build() }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && player.duration > 0) {
                    durationMs = player.duration
                    trimEnd = 1f
                }
            }
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener); player.release() }
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PROJECT_PREFS, Context.MODE_PRIVATE)
        val savedUri = prefs.getString("video_uri", null)?.let(Uri::parse)
        if (savedUri != null) {
            try {
                context.contentResolver.openFileDescriptor(savedUri, "r")?.close()
                videoUri = savedUri
                videoName = prefs.getString("video_name", "Saved project") ?: "Saved project"
                trimStart = prefs.getFloat("trim_start", 0f)
                trimEnd = prefs.getFloat("trim_end", 1f)
                muted = prefs.getBoolean("muted", false)
                overlayText = prefs.getString("overlay_text", "") ?: ""
                player.setMediaItem(MediaItem.fromUri(savedUri))
                player.prepare()
                status = "Project restored"
            } catch (_: Exception) {
                status = "Saved video is unavailable. Import it again to continue."
            }
        }
    }

    LaunchedEffect(videoUri, videoName, trimStart, trimEnd, muted, overlayText) {
        context.getSharedPreferences(PROJECT_PREFS, Context.MODE_PRIVATE).edit().apply {
            putString("video_uri", videoUri?.toString())
            putString("video_name", videoName)
            putFloat("trim_start", trimStart)
            putFloat("trim_end", trimEnd)
            putBoolean("muted", muted)
            putString("overlay_text", overlayText)
            apply()
        }
    }

    LaunchedEffect(player, videoUri) {
        while (true) {
            playheadMs = player.currentPosition.coerceAtLeast(0L)
            delay(200)
        }
    }

    val importVideo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }
            videoUri = uri
            videoName = displayName(context, uri) ?: "Imported video"
            durationMs = 0L
            trimStart = 0f
            trimEnd = 1f
            playheadMs = 0L
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            status = "Video ready — adjust the trim range or add a preview caption"
        }
    }

    Scaffold(containerColor = EditorBackground) { insets ->
        Column(
            Modifier.fillMaxSize().background(EditorBackground).padding(insets).padding(horizontal = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("MARU", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 2.sp)
                    Text("VIDEOEDITOR  ·  PROJECT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp)
                }
                Button(
                    onClick = {
                        val source = videoUri ?: return@Button
                        exporting = true
                        status = "Exporting trimmed video… Keep the app open."
                        scope.launch {
                            try {
                                exportTrimmedVideo(context, source, trimStart, trimEnd, durationMs, muted)
                                status = "Export saved to Movies/MARU VIDEOEDITOR"
                            } catch (e: Exception) {
                                status = "Export failed: ${e.localizedMessage ?: "Unsupported video or storage error"}"
                            } finally { exporting = false }
                        }
                    },
                    enabled = videoUri != null && !exporting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (exporting) CircularProgressIndicator(Modifier.size(17.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (exporting) "Exporting" else "Export MP4")
                }
            }

            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp)).background(Color(0xFF08090F)),
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { PlayerView(it).apply { player = this@MaruVideoEditor.player; useController = false; resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT } },
                        update = { it.player = player }
                    )
                    if (overlayText.isNotBlank()) {
                        Text(
                            overlayText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 23.sp,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp)
                                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(70.dp).clip(CircleShape).background(Color(0xFF252132)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Your video preview", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Import a clip to begin editing", color = EditorMuted, fontSize = 13.sp)
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(videoName, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.weight(1f))
                Text("${timecode(playheadMs)} / ${timecode(durationMs)}", color = EditorMuted, fontSize = 12.sp)
            }

            Surface(Modifier.fillMaxWidth().weight(0.94f), color = EditorPanel, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)) {
                Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("TIMELINE", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Choose the section to export", color = EditorMuted, fontSize = 12.sp)
                        }
                        IconButton(onClick = { importVideo.launch(arrayOf("video/*")) }) {
                            Icon(Icons.Default.Add, contentDescription = "Import video", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(timecode((durationMs * trimStart).toLong()), color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
                        Spacer(Modifier.width(5.dp))
                        RangeSlider(
                            value = trimStart..trimEnd,
                            onValueChange = { range -> trimStart = range.start; trimEnd = range.endInclusive },
                            valueRange = 0f..1f,
                            enabled = durationMs > 0 && !exporting,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(timecode((durationMs * trimEnd).toLong()), color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp)
                    }
                    Slider(
                        value = if (durationMs > 0) (playheadMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f,
                        onValueChange = { fraction ->
                            val newPosition = (durationMs * fraction).toLong()
                            playheadMs = newPosition
                            player.seekTo(newPosition)
                        },
                        enabled = durationMs > 0,
                        modifier = Modifier.height(28.dp)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${timecode((durationMs * (trimEnd - trimStart)).toLong())} selected", color = EditorMuted, fontSize = 11.sp)
                        Text("Original audio ${if (muted) "off" else "on"}", color = EditorMuted, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        EditorTool("Audio", Icons.Default.MusicNote, enabled = videoUri != null, selected = muted) { muted = !muted }
                        EditorTool("Caption", Icons.Default.TextFields, enabled = videoUri != null, selected = editingText) { editingText = !editingText }
                        EditorTool("Speed", Icons.Default.Speed, enabled = videoUri != null, selected = false) {
                            playbackSpeed = if (playbackSpeed >= 2f) 0.5f else playbackSpeed + 0.5f
                            player.playbackParameters = PlaybackParameters(playbackSpeed)
                        }
                    }
                    if (editingText) {
                        Spacer(Modifier.height(8.dp))
                        BasicTextField(
                            value = overlayText,
                            onValueChange = { overlayText = it.take(80) },
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF292B3B), RoundedCornerShape(10.dp)).padding(11.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            decorationBox = { field ->
                                Box {
                                    if (overlayText.isEmpty()) Text("Type a preview caption (80 chars)", color = EditorMuted, fontSize = 13.sp)
                                    field()
                                }
                            }
                        )
                        Text("Caption is preview-only; it is not burned into the exported video yet.", color = EditorMuted, fontSize = 10.sp)
                    }
                    Spacer(Modifier.height(7.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                if (videoUri == null) importVideo.launch(arrayOf("video/*"))
                                else {
                                    if (!player.isPlaying && player.currentPosition < (durationMs * trimStart).toLong()) player.seekTo((durationMs * trimStart).toLong())
                                    player.playWhenReady = !player.isPlaying
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(5.dp))
                            Text(if (videoUri == null) "Import video" else if (isPlaying) "Pause" else "Play")
                        }
                        TextButton(onClick = { player.seekTo((durationMs * trimStart).toLong()) }, enabled = videoUri != null) {
                            Text("Speed ${playbackSpeed}×  ·  Start", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                        }
                    }
                    Text(status, color = if (status.startsWith("Export failed")) Color(0xFFFF8B8B) else EditorMuted, fontSize = 11.sp, maxLines = 2)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RowScope.EditorTool(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, selected: Boolean, action: () -> Unit) {
    Surface(
        onClick = action,
        enabled = enabled,
        modifier = Modifier.weight(1f),
        color = if (selected) Color(0xFF3B2E55) else Color(0xFF292B3B),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = if (enabled) MaterialTheme.colorScheme.secondary else EditorMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(3.dp))
            Text(label, color = if (enabled) Color.White else EditorMuted, fontSize = 10.sp)
        }
    }
}

private fun timecode(milliseconds: Long): String {
    val seconds = (milliseconds.coerceAtLeast(0L) / 1000).toInt()
    return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60)
}

private fun displayName(context: Context, uri: Uri): String? = context.contentResolver.query(
    uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
)?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

private suspend fun exportTrimmedVideo(context: Context, source: Uri, startFraction: Float, endFraction: Float, durationMs: Long, removeAudio: Boolean) {
    if (durationMs <= 0L) throw IOException("Video duration is not available yet")
    val startMs = (durationMs * startFraction).toLong().coerceAtLeast(0L)
    val endMs = (durationMs * endFraction).toLong().coerceAtMost(durationMs)
    if (endMs - startMs < 500L) throw IOException("Choose a selection at least half a second long")

    val output = File(context.cacheDir, "maru_${System.currentTimeMillis()}.mp4")
    val item = MediaItem.Builder().setUri(source).setClippingConfiguration(
        MediaItem.ClippingConfiguration.Builder().setStartPositionMs(startMs).setEndPositionMs(endMs).build()
    ).build()
    val edited = EditedMediaItem.Builder(item).setRemoveAudio(removeAudio).build()

    try {
        suspendCancellableCoroutine { continuation ->
            val transformer = Transformer.Builder(context)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                        try {
                            val saved = saveToMovies(context, output)
                            output.delete()
                            if (continuation.isActive) continuation.resume(saved)
                        } catch (error: Exception) {
                            output.delete()
                            if (continuation.isActive) continuation.resumeWithException(error)
                        }
                    }

                    override fun onError(composition: androidx.media3.transformer.Composition, exportResult: ExportResult, exportException: ExportException) {
                        output.delete()
                        if (continuation.isActive) continuation.resumeWithException(exportException)
                    }
                })
                .build()
            continuation.invokeOnCancellation { transformer.cancel(); output.delete() }
            transformer.start(edited, output.absolutePath)
        }
    } catch (error: Exception) {
        output.delete()
        throw error
    }
}

private fun saveToMovies(context: Context, file: File): Uri {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "MARU_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MARU VIDEOEDITOR")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
        val destination = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Could not create an output video")
        try {
            context.contentResolver.openOutputStream(destination)?.use { output -> FileInputStream(file).use { it.copyTo(output) } }
                ?: throw IOException("Could not open the output video")
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(destination, values, null, null)
            return destination
        } catch (error: Exception) {
            context.contentResolver.delete(destination, null, null)
            throw error
        }
    }
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
    val destination = File(directory, "MARU_${System.currentTimeMillis()}.mp4")
    FileInputStream(file).use { input -> destination.outputStream().use(input::copyTo) }
    return Uri.fromFile(destination)
}

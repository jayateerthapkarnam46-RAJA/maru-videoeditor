package com.maru.videoeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class ProjectCard(
    val title: String,
    val subtitle: String,
    val badge: String,
    val accent: Color
)

private data class TimelineClip(
    val title: String,
    val duration: String,
    val accent: Color,
    val label: String
)

private data class FilterOption(
    val name: String,
    val color: Color
)

private data class StickerOption(
    val name: String,
    val emoji: String
)

@Composable
fun MaruVideoEditor() {
    val navItems = listOf("Home", "Editor", "Audio", "Templates")
    val projectCards = listOf(
        ProjectCard("Travel Reel", "4 clips · 00:42", "4K", Color(0xFF8A63FF)),
        ProjectCard("City Vlog", "6 clips · 01:10", "HD", Color(0xFF4DC7FF)),
        ProjectCard("Birthday", "3 clips · 00:28", "AI", Color(0xFFFFA857))
    )
    val timelineClips = listOf(
        TimelineClip("Intro", "00:08", Color(0xFF5B55FF), "A"),
        TimelineClip("B-roll", "00:12", Color(0xFF38C8FF), "B"),
        TimelineClip("Voice", "00:10", Color(0xFFFFA857), "C"),
        TimelineClip("Outro", "00:07", Color(0xFF9CD85D), "D")
    )
    val filters = listOf(
        FilterOption("Cinematic", Color(0xFF92A5FF)),
        FilterOption("Vivid", Color(0xFF5BD8B1)),
        FilterOption("Warm", Color(0xFFFFC857)),
        FilterOption("Dark", Color(0xFF7F87B8)),
        FilterOption("Glitch", Color(0xFFEF6BAA))
    )
    val stickers = listOf(
        StickerOption("Heart", "❤"),
        StickerOption("Spark", "✨"),
        StickerOption("Fire", "🔥"),
        StickerOption("Gift", "🎁"),
        StickerOption("Star", "⭐")
    )
    var selectedProject by remember { mutableStateOf(projectCards.first()) }
    var selectedTool by remember { mutableStateOf("Edit") }
    var playback by remember { mutableStateOf(0.62f) }
    var textOverlay by remember { mutableStateOf("Summer Memories") }
    var currentFilter by remember { mutableStateOf("Cinematic") }
    var musicVolume by remember { mutableFloatStateOf(0.72f) }
    var speed by remember { mutableFloatStateOf(1.0f) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = index == 1,
                        onClick = {},
                        icon = {
                            Icon(
                                imageVector = when (label) {
                                    "Home" -> Icons.Default.VideoLibrary
                                    "Editor" -> Icons.Default.SmartDisplay
                                    "Audio" -> Icons.Default.MusicNote
                                    else -> Icons.Default.Add
                                },
                                contentDescription = label
                            )
                        },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            HeaderRow(selectedProject = selectedProject)

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                projectCards.forEach { project ->
                    ProjectCardItem(
                        project = project,
                        selected = project == selectedProject,
                        onClick = { selectedProject = project }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreviewCard(
                project = selectedProject,
                playback = playback,
                overlayText = textOverlay,
                accent = selectedProject.accent
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Edit", "Audio", "Text", "Effects", "Sticker").forEach { tool ->
                    val selected = tool == selectedTool
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedTool = tool }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tool,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTool) {
                "Edit" -> EditorPanel(
                    playback = playback,
                    onPlaybackChange = { playback = it },
                    speed = speed,
                    onSpeedChange = { speed = it }
                )
                "Audio" -> AudioPanel(
                    volume = musicVolume,
                    onVolumeChange = { musicVolume = it }
                )
                "Text" -> TextPanel(
                    text = textOverlay,
                    onTextChange = { textOverlay = it }
                )
                "Effects" -> EffectsPanel(
                    filters = filters,
                    currentFilter = currentFilter,
                    onSelect = { currentFilter = it }
                )
                "Sticker" -> StickerPanel(stickers = stickers)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timeline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "00:42",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(timelineClips) { clip ->
                    TimelineClipCard(clip = clip)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(icon = Icons.Default.PlayArrow, label = "Preview") {}
                ActionButton(icon = Icons.Default.Share, label = "Export") {}
                ActionButton(icon = Icons.Default.Check, label = "Save") {}
            }
        }
    }
}

@Composable
private fun HeaderRow(selectedProject: ProjectCard) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "MARU",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = selectedProject.title.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Color(0xFF8A63FF), Color(0xFF48C7FF))),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "M",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun ProjectCardItem(project: ProjectCard, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) project.accent.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (selected) project.accent.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Box(
                    modifier = Modifier
                        .background(project.accent.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = project.badge, color = project.accent, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = project.subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun PreviewCard(project: ProjectCard, playback: Float, overlayText: String, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF201B31),
                        accent,
                        Color(0xFF4CC9F0)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = project.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = project.subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "Live", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(70.dp)
                .background(Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Preview play",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        Text(
            text = overlayText,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.38f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 12.dp)
                .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(text = "${(playback * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
        }
    }
}

@Composable
private fun EditorPanel(
    playback: Float,
    onPlaybackChange: (Float) -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTextButton(label = "Trim", icon = Icons.Default.Crop)
                IconTextButton(label = "Speed", icon = Icons.Default.Speed)
                IconTextButton(label = "Volume", icon = Icons.Default.VolumeUp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Playback preview", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f), fontSize = 12.sp)
            Slider(value = playback, onValueChange = onPlaybackChange)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Preview speed", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f), fontSize = 12.sp)
            Slider(value = speed, onValueChange = onSpeedChange, valueRange = 0.5f..2f)
        }
    }
}

@Composable
private fun AudioPanel(volume: Float, onVolumeChange: (Float) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = "Music track", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Background score", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), fontSize = 12.sp)
                Text(text = "Summer Beat", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Volume", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f), fontSize = 12.sp)
            Slider(value = volume, onValueChange = onVolumeChange, valueRange = 0f..1f)
        }
    }
}

@Composable
private fun TextPanel(text: String, onTextChange: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = "Text overlay", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2330), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                androidx.compose.material3.TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextTag("Bold")
                TextTag("Shadow")
                TextTag("Glow")
            }
        }
    }
}

@Composable
private fun EffectsPanel(filters: List<FilterOption>, currentFilter: String, onSelect: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = "Filters", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filters) { filter ->
                    val selected = filter.name == currentFilter
                    Box(
                        modifier = Modifier
                            .width(104.dp)
                            .height(78.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(filter.color.copy(alpha = if (selected) 0.9f else 0.6f))
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Color.White.copy(alpha = 0.75f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelect(filter.name) }
                            .padding(8.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(text = filter.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StickerPanel(stickers: List<StickerOption>) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(text = "Stickers", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(stickers) { sticker ->
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(Color(0xFF262B3A), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = sticker.emoji, fontSize = 24.sp)
                            Text(text = sticker.name, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineClipCard(clip: TimelineClip) {
    Box(
        modifier = Modifier
            .width(125.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(clip.accent)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = clip.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = clip.label, color = Color.White, fontSize = 10.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(text = clip.duration, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label)
    }
}

@Composable
private fun IconTextButton(label: String, icon: ImageVector) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = Color.White, fontSize = 11.sp)
        }
    }
}

@Composable
private fun TextTag(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
    }
}

// This is a demo-ready editor surface. The actual professional video engine remains a larger project.


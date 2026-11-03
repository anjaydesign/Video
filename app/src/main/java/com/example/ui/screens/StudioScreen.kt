package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.engine.VideoCanvasDrawer
import com.example.model.VideoProject
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.currentProject.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackTime by viewModel.playbackTime.collectAsState()
    val isRendering by viewModel.isRendering.collectAsState()
    val renderProgress by viewModel.renderProgress.collectAsState()
    val renderedFile by viewModel.renderedVideoFile.collectAsState()
    val bitmapCache by viewModel.bitmapCache.collectAsState()

    val context = LocalContext.current

    val resolutions = listOf(
        VideoProject.RES_720x1280 to "720p HD (720x1280) - Standar HP",
        VideoProject.RES_540x960 to "540p Low (540x960) - HP Spek Ringan",
        VideoProject.RES_1080x1920 to "1080p FHD (1080x1920) - Ultra Tajam"
    )

    val themes = listOf(
        VideoProject.THEME_MODERN_POP to "Modern Pop (Neon & Slide)",
        VideoProject.THEME_MINIMAL_CLEAN to "Minimal Clean (Elegan Fade)",
        VideoProject.THEME_BOLD_IMPACT to "Bold Impact (Kuning-Hitam)"
    )

    var resExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Video Studio (9:16)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                )
                Text(
                    text = "Pratinjau canvas real-time & render video vertikal",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }
        }

        // 9:16 Canvas Viewport
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .fillMaxWidth(0.72f)
                        .aspectRatio(9f / 16f)
                        .border(2.dp, BorderColor, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        VideoCanvasDrawer.drawOnComposeScope(
                            drawScope = this,
                            scenes = project.scenes,
                            currentTime = playbackTime,
                            theme = project.theme,
                            bitmapCache = bitmapCache
                        )
                    }
                }
            }
        }

        // Playback Controls
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Time Scrubber & labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentSec = playbackTime.toInt()
                        val totalSec = project.totalDuration.toInt()
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", currentSec / 60, currentSec % 60),
                            style = MaterialTheme.typography.labelMedium.copy(color = AccentPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", totalSec / 60, totalSec % 60),
                            style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                        )
                    }

                    Slider(
                        value = playbackTime,
                        onValueChange = { viewModel.seekTo(it) },
                        valueRange = 0f..project.totalDuration.coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentPrimary,
                            activeTrackColor = AccentPrimary,
                            inactiveTrackColor = BorderColor
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.togglePlayPause() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) BgSurface else AccentPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("play_preview_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPlaying) "Pause" else "Play Preview", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.stopPreview() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(0.7f)
                                .height(44.dp)
                                .testTag("stop_preview_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                }
            }
        }

        // Render Settings & Action
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pengaturan Render", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextMain))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Resolution
                    Text("Resolusi Video", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = resExpanded,
                        onExpandedChange = { resExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = resolutions.find { it.first == project.resolution }?.second ?: project.resolution,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BgSurface,
                                unfocusedContainerColor = BgSurface,
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = resExpanded,
                            onDismissRequest = { resExpanded = false },
                            modifier = Modifier.background(BgSecondary)
                        ) {
                            resolutions.forEach { (resKey, resName) ->
                                DropdownMenuItem(
                                    text = { Text(resName, color = TextMain) },
                                    onClick = {
                                        viewModel.updateProjectDetails(project.name, project.theme, resKey)
                                        resExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme
                    Text("Tema Teks & Animasi", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = themes.find { it.first == project.theme }?.second ?: project.theme,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BgSurface,
                                unfocusedContainerColor = BgSurface,
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false },
                            modifier = Modifier.background(BgSecondary)
                        ) {
                            themes.forEach { (themeKey, themeName) ->
                                DropdownMenuItem(
                                    text = { Text(themeName, color = TextMain) },
                                    onClick = {
                                        viewModel.updateProjectDetails(project.name, themeKey, project.resolution)
                                        themeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.startRender() },
                        enabled = !isRendering,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentDanger),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_render_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRendering) "Merender..." else "🔴 Render Video (MP4)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Rendering progress box
                    AnimatedVisibility(visible = isRendering) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgSurface)
                                .padding(12.dp)
                        ) {
                            val pct = (renderProgress * 100).toInt()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Merender Frame...", style = MaterialTheme.typography.bodySmall.copy(color = TextMain))
                                Text("$pct%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AccentWarning))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { renderProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = AccentPrimary,
                                trackColor = BgPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { viewModel.cancelRender() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentDanger),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AccentDanger.copy(alpha = 0.5f))),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Batalkan Render", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Rendered Video Success Box
        if (renderedFile != null && renderedFile!!.exists()) {
            val file = renderedFile!!
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentSuccess.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎉 Video Selesai Dirender!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentSuccess
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "File: ${file.name} (${file.length() / 1024} KB)",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // VideoView preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoPath(file.absolutePath)
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            start()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxHeight().aspectRatio(9f / 16f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                shareVideoFile(context, file)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("share_video_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan / Simpan Video (MP4)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun shareVideoFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Video OpenMontage"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

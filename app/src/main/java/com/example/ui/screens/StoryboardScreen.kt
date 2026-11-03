package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SceneItem
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.io.File

@Composable
fun StoryboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.currentProject.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Storyboard Editor",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    )
                    Text(
                        text = "Atur narasi visual untuk 5 scene",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Total: ${project.totalDuration}s",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Project Title & Quick Actions
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Judul Proyek",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = project.name,
                        onValueChange = { viewModel.updateProjectDetails(it, project.theme, project.resolution) },
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { viewModel.saveCurrentProject() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_project_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Proyek", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 5 Scene items
        itemsIndexed(project.scenes, key = { index, scene -> "${project.id}_s_${scene.id}_$index" }) { index, scene ->
            SceneEditorCard(
                index = index,
                scene = scene,
                onUpdate = { updatedScene -> viewModel.updateScene(index, updatedScene) },
                onPickImage = { uri -> viewModel.saveSceneImage(index, uri, context) },
                onRemoveImage = { viewModel.removeSceneImage(index) }
            )
        }

        item {
            Button(
                onClick = { viewModel.switchTab(AppTab.STUDIO) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("goto_studio_button")
            ) {
                Text("Lanjut ke Studio Video (9:16)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun SceneEditorCard(
    index: Int,
    scene: SceneItem,
    onUpdate: (SceneItem) -> Unit,
    onPickImage: (Uri) -> Unit,
    onRemoveImage: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickImage(uri)
        }
    }

    val colorPresets = listOf(
        "#B91C1C", "#4338CA", "#1D4ED8", "#047857",
        "#6D28D9", "#0F766E", "#1E1B4B", "#334155"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Scene header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scene ${index + 1}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BgSurface,
                    modifier = Modifier.border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "${scene.duration}s",
                        style = MaterialTheme.typography.labelSmall.copy(color = AccentWarning),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scene Title
            Text("Judul Scene (Hook / Poin)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = scene.title,
                onValueChange = { onUpdate(scene.copy(title = it)) },
                placeholder = { Text("Contoh: HOOK MENARIK", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Narration Voiceover
            Text("Narasi Suara", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = scene.narration,
                onValueChange = { onUpdate(scene.copy(narration = it)) },
                placeholder = { Text("Teks narasi yang akan dibacakan...", color = TextMuted) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Duration Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Durasi Tampil: ${scene.duration} detik", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            }
            Slider(
                value = scene.duration,
                onValueChange = { onUpdate(scene.copy(duration = (Math.round(it * 2) / 2.0f))) },
                valueRange = 1.0f..10.0f,
                steps = 17,
                colors = SliderDefaults.colors(
                    thumbColor = AccentPrimary,
                    activeTrackColor = AccentPrimary,
                    inactiveTrackColor = BorderColor
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Color Palette Picker
            Text("Warna Latar (Solid / Gradien)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colorPresets.forEach { hex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        Color.Gray
                    }
                    val isSelected = scene.bgColor.equals(hex, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) AccentWarning else BorderColor,
                                shape = CircleShape
                            )
                            .clickable { onUpdate(scene.copy(bgColor = hex)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Background Image Picker
            Text("Latar Gambar Kustom (Galeri)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            Spacer(modifier = Modifier.height(6.dp))

            if (!scene.imageUri.isNullOrBlank()) {
                val imageFile = File(scene.imageUri)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "Gambar Scene",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Gambar Terpasang", style = MaterialTheme.typography.bodySmall.copy(color = AccentSuccess, fontWeight = FontWeight.Bold))
                            Text("Efek Ken Burns Zoom aktif", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                    }

                    IconButton(onClick = onRemoveImage) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus Gambar", tint = AccentDanger)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderColor))
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Gambar dari Galeri", fontSize = 13.sp)
                }
            }
        }
    }
}

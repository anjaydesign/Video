package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ScriptAssistant
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptAssistantScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val category by viewModel.scriptCategory.collectAsState()
    val topic by viewModel.scriptTopic.collectAsState()
    val audience by viewModel.scriptAudience.collectAsState()
    val speed by viewModel.myRepSpeed.collectAsState()
    val price by viewModel.myRepPrice.collectAsState()
    val benefit by viewModel.myRepBenefit.collectAsState()
    val generatedScript by viewModel.generatedScript.collectAsState()

    val categories = listOf(
        ScriptAssistant.CAT_PROMO_PRODUCT to "Promosi Produk / Jualan",
        ScriptAssistant.CAT_PROMO_WIFI to "Promosi WiFi MyRepublic (Data Wajib)",
        ScriptAssistant.CAT_EDUCATION to "Edukasi / Tips Cepat",
        ScriptAssistant.CAT_STORYTELLING to "Storytelling / Curhat Bisnis",
        ScriptAssistant.CAT_SOCIAL_HOOK to "Konten Hook Media Sosial",
        ScriptAssistant.CAT_SHORT_AD to "Iklan Singkat 15 Detik"
    )

    var expandedCategoryDropdown by remember { mutableStateOf(false) }

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
                    text = "AI Script Assistant (Lokal)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                )
                Text(
                    text = "Generator naskah cerdas berbasis rule & template lokal tanpa server.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }
        }

        // Generator Form
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kategori Video",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedCategoryDropdown,
                        onExpandedChange = { expandedCategoryDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.first == category }?.second ?: category,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
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
                            expanded = expandedCategoryDropdown,
                            onDismissRequest = { expandedCategoryDropdown = false },
                            modifier = Modifier.background(BgSecondary)
                        ) {
                            categories.forEach { (catKey, catName) ->
                                DropdownMenuItem(
                                    text = { Text(catName, color = TextMain) },
                                    onClick = {
                                        viewModel.scriptCategory.value = catKey
                                        expandedCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Topik / Nama Produk / Layanan",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { viewModel.scriptTopic.value = it },
                        placeholder = { Text("Contoh: Sepatu Kulit Anti Air / MyRepublic Gamer", color = TextMuted) },
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

                    // Dynamic fields for MyRepublic
                    AnimatedVisibility(visible = category == ScriptAssistant.CAT_PROMO_WIFI) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            // Integrity Notice Box
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentWarning.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, AccentWarning.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = AccentWarning, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Aturan Integritas: Masukkan detail asli paket MyRepublic. Sistem tidak akan mengarang harga/kecepatan.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextMain)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Kecepatan (Mbps)", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = speed,
                                onValueChange = { viewModel.myRepSpeed.value = it },
                                placeholder = { Text("Contoh: 100 Mbps, 250 Mbps", color = TextMuted) },
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

                            Text("Harga Promo / Bulan", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = price,
                                onValueChange = { viewModel.myRepPrice.value = it },
                                placeholder = { Text("Contoh: Rp280.000/bln", color = TextMuted) },
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

                            Text("Benefit Tambahan", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = benefit,
                                onValueChange = { viewModel.myRepBenefit.value = it },
                                placeholder = { Text("Contoh: Gratis Pasang, Kuota Unlimited 1:1", color = TextMuted) },
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
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Target Penonton",
                        style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = audience,
                        onValueChange = { viewModel.scriptAudience.value = it },
                        placeholder = { Text("Contoh: Gamers, Ibu Rumah Tangga, Gen Z", color = TextMuted) },
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

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.generateScript() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_script_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⚡ Hasilkan 5 Naskah Scene", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Script Output Card
        if (generatedScript != null) {
            val script = generatedScript!!
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentPrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Hasil Naskah Terstruktur",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccentWarning
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = script.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hook: ${script.hook}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                        Text(
                            text = "CTA: ${script.cta}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = BorderColor
                        )

                        script.scenes.forEach { s ->
                            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                Text(
                                    text = "Scene ${s.id} (${s.title}):",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AccentPrimary
                                    )
                                )
                                Text(
                                    text = "\"${s.narration}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMain)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.applyScriptToStoryboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("apply_script_button")
                        ) {
                            Text("Terapkan ke Storyboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

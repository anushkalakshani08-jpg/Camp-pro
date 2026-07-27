package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.AppLanguage
import com.example.data.model.AppPalette
import com.example.data.model.AppSettings
import com.example.data.model.AppThemeMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Localization
import com.example.util.StringKey
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    settings: AppSettings
) {
    val context = LocalContext.current

    var bizName by remember(settings) { mutableStateOf(settings.businessName) }
    var bizAddress by remember(settings) { mutableStateOf(settings.businessAddress) }
    var bizPhone by remember(settings) { mutableStateOf(settings.businessPhone) }
    var bizEmail by remember(settings) { mutableStateOf(settings.businessEmail) }
    var bizTerms by remember(settings) { mutableStateOf(settings.termsAndConditions) }
    var logoPath by remember(settings) { mutableStateOf(settings.businessLogoPath) }

    // Image Picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = copyUriToInternalStorage(context, it)
            logoPath = savedPath
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Theme Engine Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.getString(StringKey.THEME_MODE, settings.language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.updateThemeMode(mode) },
                                label = { Text(mode.name) },
                                leadingIcon = {
                                    val icon = when (mode) {
                                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                                        AppThemeMode.DARK -> Icons.Default.DarkMode
                                        AppThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
                                    }
                                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = Localization.getString(StringKey.APP_COLOR_PALETTE, settings.language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PaletteSelectorCircle(
                            palette = AppPalette.NATURE_GREEN,
                            color = ForestGreenPrimary,
                            isSelected = settings.palette == AppPalette.NATURE_GREEN,
                            onSelect = { viewModel.updatePalette(AppPalette.NATURE_GREEN) }
                        )
                        PaletteSelectorCircle(
                            palette = AppPalette.OCEAN_BLUE,
                            color = OceanBluePrimary,
                            isSelected = settings.palette == AppPalette.OCEAN_BLUE,
                            onSelect = { viewModel.updatePalette(AppPalette.OCEAN_BLUE) }
                        )
                        PaletteSelectorCircle(
                            palette = AppPalette.SUNSET_ORANGE,
                            color = SunsetOrangePrimary,
                            isSelected = settings.palette == AppPalette.SUNSET_ORANGE,
                            onSelect = { viewModel.updatePalette(AppPalette.SUNSET_ORANGE) }
                        )
                        PaletteSelectorCircle(
                            palette = AppPalette.WOOD_EARTH,
                            color = WoodEarthPrimary,
                            isSelected = settings.palette == AppPalette.WOOD_EARTH,
                            onSelect = { viewModel.updatePalette(AppPalette.WOOD_EARTH) }
                        )
                    }
                }
            }
        }

        // Language Selector Card (Instant English / Sinhala Toggle)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.getString(StringKey.APP_LANGUAGE, settings.language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            ElevatedFilterChip(
                                selected = settings.language == lang,
                                onClick = { viewModel.updateLanguage(lang) },
                                label = {
                                    Text(
                                        text = lang.displayName,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Business Branding & Invoice Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.getString(StringKey.BUSINESS_BRANDING, settings.language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Logo Preview & Picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Gray.copy(alpha = 0.2f))
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!logoPath.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(File(logoPath!!)),
                                    contentDescription = "Business Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Button(onClick = { imagePicker.launch("image/*") }) {
                                Text(Localization.getString(StringKey.SELECT_LOGO, settings.language))
                            }
                            if (!logoPath.isNullOrEmpty()) {
                                Text(
                                    text = Localization.getString(StringKey.LOGO_SAVED, settings.language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bizName,
                        onValueChange = { bizName = it },
                        label = { Text(Localization.getString(StringKey.BUSINESS_NAME, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bizPhone,
                        onValueChange = { bizPhone = it },
                        label = { Text(Localization.getString(StringKey.BUSINESS_PHONE, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bizEmail,
                        onValueChange = { bizEmail = it },
                        label = { Text(Localization.getString(StringKey.BUSINESS_EMAIL, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bizAddress,
                        onValueChange = { bizAddress = it },
                        label = { Text(Localization.getString(StringKey.BUSINESS_ADDRESS, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bizTerms,
                        onValueChange = { bizTerms = it },
                        label = { Text(Localization.getString(StringKey.TERMS_CONDITIONS, settings.language)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateBusinessDetails(
                                name = bizName.trim(),
                                address = bizAddress.trim(),
                                phone = bizPhone.trim(),
                                email = bizEmail.trim(),
                                terms = bizTerms.trim(),
                                logoPath = logoPath
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.getString(StringKey.ACTION_SAVE, settings.language))
                    }
                }
            }
        }
    }
}

@Composable
fun PaletteSelectorCircle(
    palette: AppPalette,
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) AccentGold else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
        }
    }
}

private fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val logoFile = File(context.filesDir, "business_logo.png")
        FileOutputStream(logoFile).use { out ->
            inputStream.copyTo(out)
        }
        logoFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

package com.weather.app.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.os.LocaleListCompat
import androidx.glance.appwidget.updateAll
import com.weather.app.R
import com.weather.app.widget.WeatherGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SupportedLanguage(
    val code: String, // "" for system, "uk", "en", "de", "ru", "es", "it"
    val nativeName: String,
    val flag: String
)

val supportedLanguages = listOf(
    SupportedLanguage("", "Системна / System default", "🌐"),
    SupportedLanguage("uk", "Українська", "🇺🇦"),
    SupportedLanguage("pl", "Polski", "🇵🇱"),
    SupportedLanguage("en", "English", "🇬🇧"),
    SupportedLanguage("de", "Deutsch", "🇩🇪"),
    SupportedLanguage("es", "Español", "🇪🇸"),
    SupportedLanguage("it", "Italiano", "🇮🇹"),
    SupportedLanguage("ru", "Русский", "🇷🇺")
)

@Composable
fun LanguageSelectionDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLangCode = if (currentLocales.isEmpty) "" else currentLocales[0]?.language ?: ""

    val selectLanguage: (String) -> Unit = { code ->
        val localeList = if (code.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(code)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WeatherGlanceWidget().updateAll(context)
            } catch (_: Throwable) {
            }
        }
        onDismissRequest()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.select_language),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                supportedLanguages.forEach { lang ->
                    val isSelected = if (lang.code.isEmpty()) currentLangCode.isEmpty() else currentLangCode == lang.code

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectLanguage(lang.code) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = lang.flag, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (lang.code.isEmpty()) stringResource(R.string.lang_system) else lang.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectLanguage(lang.code) }
                        )
                    }
                }
            }
        }
    }
}

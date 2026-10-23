package com.tuannguyen.remotelastv.ui.language

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tuannguyen.remotelastv.ui.common.AppLanguage
import java.util.Locale

@Composable
fun LanguageScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf(AppLanguage.current()) }
    val deviceLanguage = remember {
        Resources.getSystem().configuration.locales[0]
            .getDisplayName(Locale.ENGLISH)
    }

    LanguageContent(
        selected = selected,
        deviceLanguage = deviceLanguage,
        onSelect = { language ->
            if (language != selected) {
                selected = language
                selected.apply()
            }
        },
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
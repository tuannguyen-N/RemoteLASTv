package com.tuannguyen.remotelastv.ui.language

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@Composable
fun LanguageScreen(
    modifier: Modifier = Modifier,
    viewModel: LanguageViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val deviceLanguage = remember {
        Resources.getSystem().configuration.locales[0]
            .getDisplayName(Locale.ENGLISH)
    }

    LanguageContent(
        selected = selectedLanguage,
        deviceLanguage = deviceLanguage,
        onSelect = { language ->
            if (language != selectedLanguage) {
                viewModel.onLanguageSelected(language)
            }
        },
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
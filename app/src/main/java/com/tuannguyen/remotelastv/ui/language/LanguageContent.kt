package com.tuannguyen.remotelastv.ui.language

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.AppLanguage
import com.tuannguyen.remotelastv.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageContent(
    selected: AppLanguage,
    deviceLanguage: String,
    onSelect: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.Space.xs),
            verticalArrangement = Arrangement.spacedBy(Dimens.Space.md),
        ) {
            Text(
                text = stringResource(R.string.choose_the_language_used_throughout_remotelastv),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.entries.forEachIndexed { index, language ->
                        LanguageRow(
                            title = language.nativeName,
                            subtitle = when (language) {
                                AppLanguage.SYSTEM -> stringResource(
                                    R.string.follows_your_phone,
                                    deviceLanguage
                                )

                                else -> language.englishName.takeIf { it != language.nativeName }
                            },
                            selected = language == selected,
                            onClick = { onSelect(language) },
                        )
                        if (index != AppLanguage.entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Dimens.Space.md),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.the_app_refreshes_briefly_when_you_change_the_language) +
                        stringResource(R.string.your_saved_tvs_and_settings_are_not_affected),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.Space.xxs),
            )
        }
    }
}

@Composable
private fun LanguageRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.Size.minTouchTarget)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Dimens.Space.md, vertical = Dimens.Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            Spacer(Modifier.size(Dimens.Size.iconMedium))
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF0E1116)
@Composable
private fun LanguageContentPreview() {
    MaterialTheme {
        LanguageContent(
            selected = AppLanguage.ENGLISH,
            deviceLanguage = "English (United States)",
            onSelect = {},
            onBack = {},
        )
    }
}
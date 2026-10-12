package com.tuannguyen.remotelastv.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.navigation.Route
import com.tuannguyen.remotelastv.ui.settings.components.SettingsContent

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onAction: (Route) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {

    }

    SettingsContent(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is SettingsAction.Navigate -> onAction(action.route)
                SettingsAction.Close -> onClose()
                SettingsAction.Share -> {
                    // TODO: Share
                }

                SettingsAction.RateAndReview -> {
                    // TODO: Rate
                }

                SettingsAction.Feedback -> {
                    // TODO: Feedback
                }

                SettingsAction.ActivatePremium -> {
                    // TODO: Premium
                }
            }
        },
    )
}
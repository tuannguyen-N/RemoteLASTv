package com.tuannguyen.remotelastv.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.navigation.Route
import com.tuannguyen.remotelastv.ui.settings.components.SettingsContent
import com.tuannguyen.remotelastv.ui.utils.sendFeedback
import com.tuannguyen.remotelastv.ui.utils.shareApp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onAction: (Route) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SettingsContent(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is SettingsAction.Navigate -> onAction(action.route)
                SettingsAction.Close -> onNavigateBack()

                SettingsAction.Share -> {
                    shareApp(context)
                }

                SettingsAction.RateAndReview -> {
                    // TODO: Rate
                }

                SettingsAction.Feedback -> {
                    sendFeedback(context, "RemoteLASTv - Feedback")
                }

                SettingsAction.ActivatePremium -> {
                    // TODO: Premium
                }
            }
        },
    )
}
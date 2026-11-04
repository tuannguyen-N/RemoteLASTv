package com.tuannguyen.remotelastv.ui.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.navigation.Route
import com.tuannguyen.remotelastv.ui.settings.components.SettingsContent
import com.tuannguyen.remotelastv.ui.utils.openPrivacyPolicy
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
    val versionName = remember {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
    }

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
                    sendFeedback(context, R.string.feedback_subject)
                }

                SettingsAction.ActivatePremium -> {
                    // TODO: Premium
                }

                SettingsAction.PrivacyPolicy -> {
                    openPrivacyPolicy(context)
                }

                SettingsAction.AdPrivacyOptions -> {
                    val activity = context.findActivity()
                    if (activity != null) {
                        viewModel.showPrivacyOptions(activity)
                    } else {
                        Log.e(TAG, "Cannot show ad privacy options without an Activity context")
                    }
                }

                SettingsAction.OpenPrivacySheet -> Unit
            }
        },
        versionName = versionName
    )
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private const val TAG = "SettingsScreen"
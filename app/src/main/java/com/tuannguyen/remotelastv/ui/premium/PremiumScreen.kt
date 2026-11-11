package com.tuannguyen.remotelastv.ui.premium

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuannguyen.remotelastv.ui.common.toUi
import com.tuannguyen.remotelastv.ui.premium.components.PremiumContent
import com.tuannguyen.remotelastv.ui.premium.components.PurchaseSuccessDialog
import com.tuannguyen.remotelastv.ui.utils.findActivity

@Composable
fun PremiumScreen(
    onClose: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentOnClose by rememberUpdatedState(onClose)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PremiumUiEvent.Close -> currentOnClose()
                is PremiumUiEvent.ShowMessage ->
                    Toast.makeText(context, event.messageRes, Toast.LENGTH_LONG).show()
            }
        }
    }

    val planUis = uiState.plans.map { it.toUi() }

    if (uiState.showPurchaseSuccess) {
        PurchaseSuccessDialog(onContinue = viewModel::onSuccessDialogDismissed)
    }

    PremiumContent(
        plans = planUis,
        selectedId = uiState.selectedId,
        premiumStatus = uiState.premiumStatus,
        onSelectPlan = viewModel::selectPlan,
        onClose = onClose,
        onSubscribe = { context.findActivity()?.let(viewModel::onSubscribe) },
        onTermsClick = onTermsClick,
        onPrivacyClick = onPrivacyClick,
        subscribeEnabled = !uiState.premiumStatus.isPremium &&
                !uiState.isLoading &&
                !uiState.isPurchasing,
        modifier = modifier,
    )
}

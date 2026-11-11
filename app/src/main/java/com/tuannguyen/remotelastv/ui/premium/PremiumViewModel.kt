package com.tuannguyen.remotelastv.ui.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import com.tuannguyen.remotelastv.domain.model.PurchaseEvent
import com.tuannguyen.remotelastv.domain.repository.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    private val _events = Channel<PremiumUiEvent>(Channel.BUFFERED)
    val events: Flow<PremiumUiEvent> = _events.receiveAsFlow()

    init {
        loadPlans()
        observePremiumStatus()
        observePurchaseEvents()
    }

    fun selectPlan(id: PremiumPlanId) {
        _uiState.update {
            if (it.premiumStatus.isPremium) it else it.copy(selectedId = id)
        }
    }

    fun onSubscribe(activity: Activity) {
        val state = _uiState.value
        if (state.premiumStatus.isPremium || state.isLoading || state.isPurchasing) return

        if (state.plans.isEmpty()) {
            loadPlans()
            return
        }

        _uiState.update { it.copy(isPurchasing = true) }
        viewModelScope.launch {
            premiumRepository.launchPurchase(activity, state.selectedId)
                .onFailure {
                    _uiState.update { s -> s.copy(isPurchasing = false) }
                    _events.send(PremiumUiEvent.ShowMessage(R.string.premium_purchase_failed))
                }
        }
    }

    private fun loadPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            premiumRepository.loadPlans()
                .onSuccess { plans ->
                    _uiState.update { state ->
                        val selected = state.premiumStatus.activePlanId?.takeIf { active ->
                            plans.any { it.id == active }
                        } ?: state.selectedId.takeIf { sel -> plans.any { it.id == sel } }
                        ?: plans.firstOrNull()?.id
                        ?: state.selectedId
                        state.copy(plans = plans, selectedId = selected, isLoading = false)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(PremiumUiEvent.ShowMessage(R.string.premium_error_load_plans))
                }
        }
    }

    private fun observePremiumStatus() {
        viewModelScope.launch {
            premiumRepository.premiumStatus.collect { status ->
                _uiState.update {
                    it.copy(
                        premiumStatus = status,
                        selectedId = status.activePlanId ?: it.selectedId,
                    )
                }
            }
        }
    }

    fun onSuccessDialogDismissed() {
        _uiState.update { it.copy(showPurchaseSuccess = false) }
        viewModelScope.launch { _events.send(PremiumUiEvent.Close) }
    }

    private fun observePurchaseEvents() {
        viewModelScope.launch {
            premiumRepository.purchaseEvents.collect { event ->
                _uiState.update { it.copy(isPurchasing = false) }
                when (event) {
                    PurchaseEvent.Pending ->
                        _events.send(PremiumUiEvent.ShowMessage(R.string.premium_purchase_pending))

                    is PurchaseEvent.Failed ->
                        _events.send(PremiumUiEvent.ShowMessage(R.string.premium_purchase_failed))

                    PurchaseEvent.Success ->
                        _uiState.update { it.copy(showPurchaseSuccess = true) }

                    PurchaseEvent.Canceled -> Unit
                }
            }
        }
    }
}
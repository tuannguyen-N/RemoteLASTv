package com.tuannguyen.remotelastv.ui.premium

import com.tuannguyen.remotelastv.domain.model.PremiumPlan
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import com.tuannguyen.remotelastv.domain.model.PremiumStatus

data class PremiumUiState(
    val plans: List<PremiumPlan> = emptyList(),
    val selectedId: PremiumPlanId = PremiumPlanId.YEAR,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val showPurchaseSuccess: Boolean = false,
    val premiumStatus: PremiumStatus = PremiumStatus(),
)
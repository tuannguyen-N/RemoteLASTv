package com.tuannguyen.remotelastv.domain.model

data class PremiumStatus(
    val isPremium: Boolean = false,
    val activePlanId: PremiumPlanId? = null,
)
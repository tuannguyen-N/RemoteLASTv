package com.tuannguyen.remotelastv.ui.common

import com.tuannguyen.remotelastv.domain.model.PremiumPlanId

data class PremiumPlanUi(
    val id: PremiumPlanId,
    val title: String,
    val subtitle: String,
    val price: String,
    val badge: String? = null,
)
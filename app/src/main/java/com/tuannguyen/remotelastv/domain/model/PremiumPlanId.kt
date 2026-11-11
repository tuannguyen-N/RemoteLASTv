package com.tuannguyen.remotelastv.domain.model

enum class PremiumPlanId(val basePlanId: String) {
    YEAR("yearly"),
    MONTH("monthly"),
    SIX_MONTHS("6-months")
}
package com.tuannguyen.remotelastv.domain.repository

import android.app.Activity
import com.tuannguyen.remotelastv.domain.model.PremiumPlan
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import com.tuannguyen.remotelastv.domain.model.PremiumStatus
import com.tuannguyen.remotelastv.domain.model.PurchaseEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PremiumRepository {
    val premiumStatus: Flow<PremiumStatus>

    val hasSyncedPremiumStatus: StateFlow<Boolean>

    val purchaseEvents: Flow<PurchaseEvent>

    suspend fun loadPlans(): Result<List<PremiumPlan>>

    suspend fun launchPurchase(activity: Activity, planId: PremiumPlanId): Result<Unit>

    suspend fun refresh(): Result<Unit>
}
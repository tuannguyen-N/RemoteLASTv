package com.tuannguyen.remotelastv.data.repository

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.tuannguyen.remotelastv.data.billing.BillingClientWrapper
import com.tuannguyen.remotelastv.data.billing.PurchaseUpdate
import com.tuannguyen.remotelastv.data.billing.findBasePlanOffer
import com.tuannguyen.remotelastv.data.di.ApplicationScope
import com.tuannguyen.remotelastv.data.local.PremiumLocalDataSource
import com.tuannguyen.remotelastv.domain.model.PremiumConfig
import com.tuannguyen.remotelastv.domain.model.PremiumPlan
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import com.tuannguyen.remotelastv.domain.model.PremiumStatus
import com.tuannguyen.remotelastv.domain.model.PurchaseEvent
import com.tuannguyen.remotelastv.domain.repository.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val billing: BillingClientWrapper,
    private val local: PremiumLocalDataSource,
    @ApplicationScope scope: CoroutineScope
) : PremiumRepository {
    override val premiumStatus: Flow<PremiumStatus> = local.premiumStatus

    private val _hasSyncedPremiumStatus = MutableStateFlow(false)
    override val hasSyncedPremiumStatus: StateFlow<Boolean> =
        _hasSyncedPremiumStatus.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 8)
    override val purchaseEvents: Flow<PurchaseEvent> = _purchaseEvents.asSharedFlow()

    private var pendingPlanId: PremiumPlanId? = null

    private val refreshMutex = Mutex()

    init {
        scope.launch { billing.purchaseUpdates.collect(::onPurchaseUpdate) }
    }

    override suspend fun loadPlans(): Result<List<PremiumPlan>> =
        billing.queryProductDetails().map { details ->
            listOf(
                PremiumPlanId.YEAR,
                PremiumPlanId.SIX_MONTHS,
                PremiumPlanId.MONTH,
            ).mapNotNull { id ->
                val offer = details.findBasePlanOffer(id) ?: return@mapNotNull null
                val price = offer.pricingPhases.pricingPhaseList.lastOrNull()?.formattedPrice
                    ?: return@mapNotNull null
                PremiumPlan(id = id, formattedPrice = price)
            }
        }

    override suspend fun launchPurchase(
        activity: Activity,
        planId: PremiumPlanId,
    ): Result<Unit> {
        val result = billing.launchPurchase(activity, planId)
        if (result.isSuccess) pendingPlanId = planId
        return result
    }

    override suspend fun refresh(): Result<Unit> =
        refreshMutex.withLock {
            billing.queryActivePurchases().map { purchases ->
                val owned = purchases.filter {
                    PremiumConfig.PRODUCT_ID in it.products &&
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                owned.filterNot { it.isAcknowledged }
                    .forEach { billing.acknowledge(it) }

                val currentStatus = local.premiumStatus.first()

                val activePurchase = owned.maxByOrNull { it.purchaseTime }

                val status = PremiumStatus(
                    isPremium = activePurchase != null,
                    activePlanId = if (activePurchase != null) {
                        pendingPlanId ?: currentStatus.activePlanId
                    } else {
                        null
                    }
                )
                local.setPremiumStatus(status)
                local.premiumStatus.first { it == status }
                _hasSyncedPremiumStatus.value = true
            }
        }

    private suspend fun onPurchaseUpdate(update: PurchaseUpdate) {
        when (val code = update.result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                refresh()

                val hasPending = update.purchases.any {
                    it.purchaseState == Purchase.PurchaseState.PENDING
                }

                val premium = local.premiumStatus.first().isPremium

                _purchaseEvents.tryEmit(
                    when {
                        premium -> PurchaseEvent.Success
                        hasPending -> PurchaseEvent.Pending
                        else -> PurchaseEvent.Failed(code)
                    }
                )

                if (premium) pendingPlanId = null
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                pendingPlanId = null
                _purchaseEvents.tryEmit(PurchaseEvent.Canceled)
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                refresh()

                _purchaseEvents.tryEmit(
                    if (local.premiumStatus.first().isPremium) {
                        PurchaseEvent.Success
                    } else {
                        PurchaseEvent.Failed(code)
                    }
                )
            }

            else -> {
                _purchaseEvents.tryEmit(PurchaseEvent.Failed(code))
            }
        }
    }
}
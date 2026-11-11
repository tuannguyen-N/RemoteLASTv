package com.tuannguyen.remotelastv.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.tuannguyen.remotelastv.domain.model.PremiumConfig
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingClientWrapper @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = 16)
    val purchaseUpdates: SharedFlow<PurchaseUpdate> = _purchaseUpdates.asSharedFlow()

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases ->
            _purchaseUpdates.tryEmit(PurchaseUpdate(result, purchases.orEmpty()))
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .enableAutoServiceReconnection()
        .build()

    private val connectMutex = Mutex()
    private var cachedDetails: ProductDetails? = null

    private suspend fun ensureConnected(): Boolean = connectMutex.withLock {
        if (client.isReady) return@withLock true
        suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) {
                        cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }

                override fun onBillingServiceDisconnected() = Unit
            })
        }
    }

    private fun notConnected() = BillingException(
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
        "Cannot connect to Google Play",
    )

    suspend fun queryProductDetails(): Result<ProductDetails> {
        if (!ensureConnected()) return Result.failure(notConnected())

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PremiumConfig.PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val result = client.queryProductDetails(params)
        val code = result.billingResult.responseCode
        val details = result.productDetailsList?.firstOrNull()

        return if (code == BillingClient.BillingResponseCode.OK && details != null) {
            cachedDetails = details
            Result.success(details)
        } else {
            Result.failure(
                BillingException(
                    code,
                    "queryProductDetails failed: ${result.billingResult.debugMessage}"
                )
            )
        }
    }

    suspend fun launchPurchase(activity: Activity, planId: PremiumPlanId): Result<Unit> {
        val details = cachedDetails ?: queryProductDetails().getOrElse { return Result.failure(it) }

        val offer = details.findBasePlanOffer(planId) ?: return Result.failure(
            BillingException(
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                "Base plan ${planId.basePlanId} not found",
            )
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offer.offerToken)
                        .build()
                )
            )
            .build()

        val result =
            withContext(Dispatchers.Main) { client.launchBillingFlow(activity, flowParams) }
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Result.success(Unit)
        } else {
            Result.failure(BillingException(result.responseCode, result.debugMessage))
        }
    }

    suspend fun queryActivePurchases(): Result<List<Purchase>> {
        if (!ensureConnected()) return Result.failure(notConnected())

        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val code = result.billingResult.responseCode
        return if (code == BillingClient.BillingResponseCode.OK) {
            Result.success(result.purchasesList)
        } else {
            Result.failure(BillingException(code, result.billingResult.debugMessage))
        }
    }

    suspend fun acknowledge(purchase: Purchase): Result<Unit> {
        if (!ensureConnected()) return Result.failure(notConnected())

        val result = client.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            Result.success(Unit)
        } else {
            Result.failure(BillingException(result.responseCode, result.debugMessage))
        }
    }

}

fun ProductDetails.findBasePlanOffer(planId: PremiumPlanId): ProductDetails.SubscriptionOfferDetails? =
    subscriptionOfferDetails.orEmpty()
        .firstOrNull { it.basePlanId == planId.basePlanId && it.offerId == null }

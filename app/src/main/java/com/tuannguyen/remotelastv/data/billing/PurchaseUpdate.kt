package com.tuannguyen.remotelastv.data.billing

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

data class PurchaseUpdate(val result: BillingResult, val purchases: List<Purchase>)
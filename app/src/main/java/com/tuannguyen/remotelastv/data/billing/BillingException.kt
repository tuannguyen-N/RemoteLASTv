package com.tuannguyen.remotelastv.data.billing

class BillingException(val responseCode: Int, message: String) : Exception(message)
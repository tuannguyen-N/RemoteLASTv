package com.tuannguyen.remotelastv.data.tv.roku

import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class RokuRegistry @Inject constructor() {
    @Volatile var ip: String? = null
}
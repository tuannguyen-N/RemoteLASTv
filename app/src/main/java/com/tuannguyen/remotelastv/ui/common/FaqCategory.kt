package com.tuannguyen.remotelastv.ui.common

import androidx.annotation.StringRes
import com.tuannguyen.remotelastv.R

enum class FaqCategory(@get:StringRes val labelRes: Int) {
    ALL(R.string.faq_category_all),
    CONNECTION(R.string.faq_category_connection),
    CONTROL(R.string.faq_category_control),
    SETTINGS(R.string.faq_category_settings),
}
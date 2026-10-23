package com.tuannguyen.remotelastv.ui.common

import androidx.annotation.StringRes
import com.tuannguyen.remotelastv.R

data class FaqItem(
    val id: String,
    val category: FaqCategory,
    @get:StringRes val questionRes: Int,
    @get:StringRes val answerRes: Int,
) {
    companion object {
        fun getFaqs(): List<FaqItem> {
            return listOf(
                FaqItem(
                    "supported_tvs", FaqCategory.CONNECTION,
                    R.string.faq_question_supported_tvs,
                    R.string.faq_answer_supported_tvs,
                ),
                FaqItem(
                    "same_network", FaqCategory.CONNECTION,
                    R.string.faq_question_same_network,
                    R.string.faq_answer_same_network,
                ),
                FaqItem(
                    "lg_pair", FaqCategory.CONNECTION,
                    R.string.faq_question_lg_pair,
                    R.string.faq_answer_lg_pair,
                ),
                FaqItem(
                    "samsung_allow", FaqCategory.CONNECTION,
                    R.string.faq_question_samsung_allow,
                    R.string.faq_answer_samsung_allow,
                ),
                FaqItem(
                    "androidtv_pin", FaqCategory.CONNECTION,
                    R.string.faq_question_androidtv_pin,
                    R.string.faq_answer_androidtv_pin,
                ),
                FaqItem(
                    "auto_reconnect", FaqCategory.CONNECTION,
                    R.string.faq_question_auto_reconnect,
                    R.string.faq_answer_auto_reconnect,
                ),
                FaqItem(
                    "connection_dropped", FaqCategory.CONNECTION,
                    R.string.faq_question_connection_dropped,
                    R.string.faq_answer_connection_dropped,
                ),
                FaqItem(
                    "buttons_no_response", FaqCategory.CONTROL,
                    R.string.faq_question_buttons_no_response,
                    R.string.faq_answer_buttons_no_response,
                ),
                FaqItem(
                    "power_on", FaqCategory.CONTROL,
                    R.string.faq_question_power_on,
                    R.string.faq_answer_power_on,
                ),
                FaqItem(
                    "touchpad", FaqCategory.CONTROL,
                    R.string.faq_question_touchpad,
                    R.string.faq_answer_touchpad,
                ),
                FaqItem(
                    "shortcut_apps", FaqCategory.CONTROL,
                    R.string.faq_question_shortcut_apps,
                    R.string.faq_answer_shortcut_apps,
                ),
                FaqItem(
                    "mic_permission", FaqCategory.CONTROL,
                    R.string.faq_question_mic_permission,
                    R.string.faq_answer_mic_permission,
                ),
                FaqItem(
                    "nav_mode", FaqCategory.SETTINGS,
                    R.string.faq_question_nav_mode,
                    R.string.faq_answer_nav_mode,
                ),
                FaqItem(
                    "media_buttons", FaqCategory.SETTINGS,
                    R.string.faq_question_media_buttons,
                    R.string.faq_answer_media_buttons,
                ),
                FaqItem(
                    "forget_tv", FaqCategory.SETTINGS,
                    R.string.faq_question_forget_tv,
                    R.string.faq_answer_forget_tv,
                ),
            )

        }
    }
}
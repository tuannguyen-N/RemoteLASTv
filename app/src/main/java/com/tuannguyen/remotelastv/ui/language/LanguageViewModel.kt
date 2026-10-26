package com.tuannguyen.remotelastv.ui.language

import androidx.lifecycle.ViewModel
import com.tuannguyen.remotelastv.ui.common.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor() : ViewModel() {

    private val _selectedLanguage = MutableStateFlow(AppLanguage.current())
    val selectedLanguage = _selectedLanguage.asStateFlow()

    fun onLanguageSelected(language: AppLanguage) {
        _selectedLanguage.value = language
        language.apply()
    }
}
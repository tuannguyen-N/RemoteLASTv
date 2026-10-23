package com.tuannguyen.remotelastv.ui.faq

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tuannguyen.remotelastv.ui.common.FaqCategory
import com.tuannguyen.remotelastv.ui.common.FaqItem

@Composable
fun FaqScreen(
    onNavigateBack: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf(FaqCategory.ALL.name) }
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    val category = FaqCategory.valueOf(categoryName)
    val context = LocalContext.current
    val items = remember(query, category, context) {
        FaqItem.getFaqs().filter { faq ->
            (category == FaqCategory.ALL || faq.category == category) &&
                    (query.isBlank() ||
                            context.getString(faq.questionRes).contains(query, ignoreCase = true) ||
                            context.getString(faq.answerRes).contains(query, ignoreCase = true))
        }
    }

    FaqContent(
        query = query,
        onQueryChange = { query = it },
        selectedCategory = category,
        onCategoryChange = { categoryName = it.name; expandedId = null },
        items = items,
        expandedId = expandedId,
        onToggle = { id -> expandedId = if (expandedId == id) null else id },
        showQuickFix = query.isBlank() &&
                (category == FaqCategory.ALL || category == FaqCategory.CONNECTION),
        onClearFilters = { query = ""; categoryName = FaqCategory.ALL.name },
        onBack = onNavigateBack,
        onContactClick = onContactClick,
        modifier = modifier,
    )
}
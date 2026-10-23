package com.tuannguyen.remotelastv.ui.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.ui.common.FaqCategory
import com.tuannguyen.remotelastv.ui.common.FaqItem
import com.tuannguyen.remotelastv.ui.theme.Dimens

private val quickFixSteps = listOf(
    R.string.faq_quick_fix_same_network,
    R.string.faq_quick_fix_tv_on,
    R.string.faq_quick_fix_vpn_off,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqContent(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: FaqCategory,
    onCategoryChange: (FaqCategory) -> Unit,
    items: List<FaqItem>,
    expandedId: String?,
    onToggle: (String) -> Unit,
    showQuickFix: Boolean,
    onClearFilters: () -> Unit,
    onBack: () -> Unit,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.faq_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.faq_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SearchField(query = query, onQueryChange = onQueryChange) }
            item { CategoryChips(selected = selectedCategory, onSelect = onCategoryChange) }
            if (showQuickFix) {
                item { QuickFixCard() }
            }
            if (items.isEmpty()) {
                item { EmptyState(onClearFilters = onClearFilters) }
            } else {
                // Short list, so one block is fine. Split into items(...) if the FAQ grows past ~30 entries.
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Column {
                            items.forEachIndexed { index, faq ->
                                FaqRow(
                                    item = faq,
                                    expanded = faq.id == expandedId,
                                    onClick = { onToggle(faq.id) },
                                )
                                if (index != items.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { ContactFooter(onContactClick = onContactClick) }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.faq_search_questions)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.faq_clear_search),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun CategoryChips(selected: FaqCategory, onSelect: (FaqCategory) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FaqCategory.entries.forEach { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelect(category) },
                label = { Text(stringResource(category.labelRes)) },
            )
        }
    }
}

@Composable
private fun QuickFixCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.faq_quick_fix_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.faq_quick_fix_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            quickFixSteps.forEach { step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(stringResource(step), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


@Composable
private fun FaqRow(
    item: FaqItem,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing,
        ),
        label = "faqArrow",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = Dimens.Space.md,
                vertical = Dimens.Space.sm,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(item.questionRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = Dimens.Space.sm),
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(
                    if (expanded) {
                        R.string.faq_collapse
                    } else {
                        R.string.faq_expand
                    }
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(arrowRotation),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing,
                ),
                expandFrom = Alignment.Top,
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 180,
                    easing = LinearOutSlowInEasing,
                ),
            ),
            exit = shrinkVertically(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutLinearInEasing,
                ),
                shrinkTowards = Alignment.Top,
            ) + fadeOut(
                animationSpec = tween(durationMillis = 120),
            ),
        ) {
            Text(
                text = stringResource(item.answerRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(
                        top = Dimens.Space.sm,
                        end = Dimens.Space.xl,
                    ),
            )
        }
    }
}

@Composable
private fun EmptyState(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.faq_no_matching_questions),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            stringResource(R.string.faq_try_different_keywords),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onClearFilters) {
            Text(stringResource(R.string.faq_clear_filters))
        }
    }
}

@Composable
private fun ContactFooter(onContactClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.faq_still_stuck),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onContactClick) {
            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.faq_send_feedback))
        }
        Spacer(Modifier.height(8.dp))
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF0E1116)
@Composable
private fun FaqContentPreview() {
    MaterialTheme {
        FaqContent(
            query = "",
            onQueryChange = {},
            selectedCategory = FaqCategory.ALL,
            onCategoryChange = {},
            items = FaqItem.getFaqs(),
            expandedId = "lg_pair",
            onToggle = {},
            showQuickFix = true,
            onClearFilters = {},
            onBack = {},
            onContactClick = {},
        )
    }
}
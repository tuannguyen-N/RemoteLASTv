package com.tuannguyen.remotelastv.ui.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import com.tuannguyen.remotelastv.ui.common.PremiumPlanUi

@Composable
fun PremiumScreen(
    onClose: () -> Unit,
    onSubscribe: (PremiumPlanId) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedId by rememberSaveable { mutableStateOf(PremiumPlanId.YEAR) }

    // TODO: thay bằng dữ liệu từ ProductDetails (Play Billing) qua ViewModel
    val plans = listOf(
        PremiumPlanUi(
            id = PremiumPlanId.YEAR,
            title = stringResource(R.string.premium_plan_year),
            subtitle = stringResource(R.string.premium_plan_year_description),
            price = "$1.99",
            badge = stringResource(R.string.premium_plan_most_popular),
        ),
        PremiumPlanUi(
            id = PremiumPlanId.MONTH,
            title = stringResource(R.string.premium_plan_month),
            subtitle = stringResource(R.string.premium_plan_month_description),
            price = "$0.99",
        ),
    )

    PremiumContent(
        plans = plans,
        selectedId = selectedId,
        onSelectPlan = { selectedId = it },
        onClose = onClose,
        onSubscribe = { onSubscribe(selectedId) },
        onTermsClick = onTermsClick,
        onPrivacyClick = onPrivacyClick,
        modifier = modifier,
    )
}
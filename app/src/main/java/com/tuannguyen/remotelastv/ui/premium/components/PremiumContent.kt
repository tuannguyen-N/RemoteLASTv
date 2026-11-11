package com.tuannguyen.remotelastv.ui.premium.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuannguyen.remotelastv.R
import com.tuannguyen.remotelastv.domain.model.PremiumStatus
import com.tuannguyen.remotelastv.domain.model.PremiumPlanId
import com.tuannguyen.remotelastv.ui.common.IconSource
import com.tuannguyen.remotelastv.ui.common.PremiumPlanUi
import com.tuannguyen.remotelastv.ui.components.AppIcon
import com.tuannguyen.remotelastv.ui.theme.ButtonColors
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme
import com.tuannguyen.remotelastv.ui.theme.extendedColors

private object PremiumColors {
    val Background = Color(0xFF0D0E19)
    val CardSelected = Color(0xFF241D45)
    val CardUnselected = Color(0xFF1A1B2A)
    val CardUnselectedBorder = Color(0xFF2B2C3F)
    val Badge = Color(0xFF7C3AED)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFB3B3C7)
    val Accent = Color(0xFF9F7AEA)
    val RadioIdle = Color(0xFF4A4B60)
    val CloseBackground = Color(0xFF22233A)
    val Glow = Color(0xFF5B3FD0)
}

@Composable
fun PremiumContent(
    plans: List<PremiumPlanUi>,
    selectedId: PremiumPlanId,
    premiumStatus: PremiumStatus = PremiumStatus(),
    onSelectPlan: (PremiumPlanId) -> Unit,
    onClose: () -> Unit,
    onSubscribe: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
    subscribeEnabled: Boolean = true,
    heroIcon: IconSource = IconSource.Resource(R.drawable.ic_tv_no_ads_with_remote),
    noAdsIcon: IconSource = IconSource.Resource(R.drawable.ic_no_ads),
    boltIcon: IconSource = IconSource.Vector(Icons.Filled.Bolt),
    heartIcon: IconSource = IconSource.Vector(Icons.Filled.Favorite),
    lockIcon: IconSource = IconSource.Vector(Icons.Filled.Lock),
    closeIcon: IconSource = IconSource.Vector(Icons.Filled.Close),
    googlePlayIcon: IconSource = IconSource.Resource(R.drawable.ic_google_play),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .drawBehind {
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PremiumColors.Glow.copy(alpha = 0.35f),
                                        Color.Transparent,
                                    ),
                                    center = center.copy(y = size.height * 0.75f),
                                    radius = size.width * 0.6f,
                                )
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        source = heroIcon,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.premium_screen_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PremiumColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.premium_screen_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PremiumColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(32.dp)) // chừa chỗ cho badge nhô lên

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        plans.forEach { plan ->
                            PlanCard(
                                plan = plan,
                                selected = if (premiumStatus.isPremium) {
                                    plan.id == premiumStatus.activePlanId
                                } else {
                                    plan.id == selectedId
                                },
                                active = premiumStatus.isPremium &&
                                        plan.id == premiumStatus.activePlanId,
                                enabled = !premiumStatus.isPremium,
                                onClick = { onSelectPlan(plan.id) },
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        FeatureItem(
                            icon = noAdsIcon,
                            title = stringResource(R.string.premium_feature_no_ads_title),
                            subtitle = stringResource(R.string.premium_feature_no_ads_description),
                        )
                        FeatureItem(
                            icon = boltIcon,
                            title = stringResource(R.string.premium_feature_smoother_title),
                            subtitle = stringResource(R.string.premium_feature_smoother_description),
                        )
                        FeatureItem(
                            icon = heartIcon,
                            title = stringResource(R.string.premium_feature_support_title),
                            subtitle = stringResource(R.string.premium_feature_support_description),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SubscribeButton(
                    text = stringResource(
                        if (premiumStatus.isPremium) {
                            R.string.premium_currently_active
                        } else {
                            R.string.premium_subscribe_google_play
                        }
                    ),
                    icon = googlePlayIcon,
                    enabled = subscribeEnabled && !premiumStatus.isPremium,
                    onClick = onSubscribe,
                )

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.premium_terms_of_service),
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                        color = PremiumColors.TextSecondary,
                        modifier = Modifier.clickable(onClick = onTermsClick),
                    )
                    Text(
                        text = stringResource(R.string.premium_links_separator),
                        style = MaterialTheme.typography.bodySmall,
                        color = PremiumColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                    Text(
                        text = stringResource(R.string.premium_privacy_policy),
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                        color = PremiumColors.TextSecondary,
                        modifier = Modifier.clickable(onClick = onPrivacyClick),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(14.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(PremiumColors.CloseBackground)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                source = closeIcon,
                contentDescription = stringResource(R.string.premium_close),
                tint = PremiumColors.TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}


@Composable
private fun PlanCard(
    plan: PremiumPlanUi,
    selected: Boolean,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val borderBrush = if (selected) {
        Brush.horizontalGradient(ButtonColors)
    } else {
        Brush.linearGradient(
            listOf(PremiumColors.CardUnselectedBorder, PremiumColors.CardUnselectedBorder)
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) PremiumColors.CardSelected else PremiumColors.CardUnselected)
                .border(if (selected) 1.dp else 1.dp, borderBrush, shape)
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlanRadio(selected = selected)

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PremiumColors.TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = plan.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PremiumColors.TextSecondary,
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)) {
                        append(plan.price)
                    }
                },
                color = PremiumColors.TextPrimary,
            )
        }

        (if (active) stringResource(R.string.premium_plan_active) else plan.badge)?.let { badge ->
            Text(
                text = badge,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = PremiumColors.TextPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = (-14).dp)
                    .clip(CircleShape)
                    .background(if (active) MaterialTheme.extendedColors.success else PremiumColors.Badge)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun PlanRadio(selected: Boolean) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, Brush.horizontalGradient(ButtonColors), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(PremiumColors.Accent)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, PremiumColors.RadioIdle, CircleShape)
        )
    }
}

@Composable
private fun FeatureItem(icon: IconSource, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(48.dp), contentAlignment = Alignment.Center) {
            AppIcon(
                source = icon,
                tint = PremiumColors.Accent,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = PremiumColors.TextPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = PremiumColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun SubscribeButton(
    text: String,
    icon: IconSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(ButtonColors))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        AppIcon(source = icon, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
private fun previewPremiumPlans() = listOf(
    PremiumPlanUi(
        id = PremiumPlanId.YEAR,
        title = stringResource(R.string.premium_plan_year),
        subtitle = stringResource(R.string.premium_plan_year_description),
        price = "190.000Đ",
        badge = stringResource(R.string.premium_plan_most_popular),
    ),
    PremiumPlanUi(
        id = PremiumPlanId.SIX_MONTHS,
        title = stringResource(R.string.premium_plan_6_month),
        subtitle = stringResource(R.string.premium_plan_6_months_description),
        price = "$2.99",
    ),
    PremiumPlanUi(
        id = PremiumPlanId.MONTH,
        title = stringResource(R.string.premium_plan_month),
        subtitle = stringResource(R.string.premium_plan_month_description),
        price = "$0.99",
    ),
)

@Preview(name = "Premium - Year", showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun PremiumContentYearPreview() {
    RemoteLASTvTheme {
        PremiumContent(
            plans = previewPremiumPlans(),
            selectedId = PremiumPlanId.YEAR,
            onSelectPlan = {},
            onClose = {},
            onSubscribe = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}

@Preview(name = "Premium - Month", showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun PremiumContentMonthPreview() {
    RemoteLASTvTheme {
        PremiumContent(
            plans = previewPremiumPlans(),
            selectedId = PremiumPlanId.MONTH,
            onSelectPlan = {},
            onClose = {},
            onSubscribe = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}

@Preview(name = "Premium - Active Year Plan", showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun PremiumContentActivePlanPreview() {
    RemoteLASTvTheme {
        PremiumContent(
            plans = previewPremiumPlans(),
            selectedId = PremiumPlanId.YEAR,
            premiumStatus = PremiumStatus(
                isPremium = true,
                activePlanId = PremiumPlanId.YEAR,
            ),
            onSelectPlan = {},
            onClose = {},
            onSubscribe = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}
package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.billing.BillingManager
import com.example.billing.PaywallPricingState
import com.example.billing.SubscriptionOfferInfo
import com.example.ui.screens.PaywallDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BillingPricingTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun calculatesAnnualSavingsAndParsesPeriodsAccurately() {
        assertEquals(58, BillingManager.calculateAnnualSavingsPercent(1_990_000_000L, 9_990_000_000L))
        assertNull(BillingManager.calculateAnnualSavingsPercent(1_000_000L, 12_000_000L))
        assertEquals(context.getString(R.string.period_month), BillingManager.formatBillingPeriodShort(context, "P1M"))
        assertEquals(context.getString(R.string.period_year), BillingManager.formatBillingPeriodShort(context, "P1Y"))
        assertEquals(context.getString(R.string.duration_days, 7), BillingManager.formatTrialDuration(context, "P1W"))
    }

    @Test
    fun paywallDisplaysExactPlayBillingCurrencyWithoutHardcodedUsd() {
        val state = PaywallPricingState(
            isLoading = false,
            monthly = SubscriptionOfferInfo(
                recurringFormattedPrice = "CLP 1,990",
                recurringPriceMicros = 1_990_000_000L,
                periodLabel = context.getString(R.string.period_month),
                hasFreeTrial = false,
                trialFormattedPrice = null,
                trialDurationLabel = null
            ),
            yearly = SubscriptionOfferInfo(
                recurringFormattedPrice = "CLP 9,990",
                recurringPriceMicros = 9_990_000_000L,
                periodLabel = context.getString(R.string.period_year),
                hasFreeTrial = false,
                trialFormattedPrice = null,
                trialDurationLabel = null,
                savingsPercent = 58
            ),
            lifetimeFormattedPrice = "CLP 15,990"
        )

        composeRule.setContent {
            PaywallDialog(
                pricingState = state,
                onDismiss = {},
                onPurchaseLifetime = {},
                onPurchaseMonthly = {},
                onPurchaseYearly = {}
            )
        }
        composeRule.waitForIdle()

        val monthLabel = context.getString(R.string.period_month)
        val yearLabel = context.getString(R.string.period_year)
        composeRule.onNodeWithText(context.getString(R.string.paywall_monthly_cta, "CLP 1,990", monthLabel)).assertExists()
        composeRule.onNodeWithText("CLP 9,990 / $yearLabel").assertExists()
        composeRule.onNodeWithText("CLP 15,990").assertExists()
        composeRule.onNodeWithText("$0.00", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("$19.99", substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("$10.00", substring = true).assertDoesNotExist()
    }
}

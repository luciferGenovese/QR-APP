package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class SubscriptionOfferInfo(
    val recurringFormattedPrice: String,
    val recurringPriceMicros: Long,
    val periodLabel: String,
    val hasFreeTrial: Boolean,
    val trialFormattedPrice: String?,
    val trialDurationLabel: String?,
    val savingsPercent: Int? = null
)

data class PaywallPricingState(
    val isLoading: Boolean = true,
    val monthly: SubscriptionOfferInfo? = null,
    val yearly: SubscriptionOfferInfo? = null,
    val lifetimeFormattedPrice: String? = null
)

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _productDetailsList = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetailsList: StateFlow<List<ProductDetails>> = _productDetailsList.asStateFlow()

    private val _pricingState = MutableStateFlow(PaywallPricingState(isLoading = true))
    val pricingState: StateFlow<PaywallPricingState> = _pricingState.asStateFlow()

    private val productMap = ConcurrentHashMap<String, ProductDetails>()
    @Volatile
    private var isConnecting = false
    @Volatile
    private var hasActiveInApp = false
    @Volatile
    private var hasActiveSubs = false

    companion object {
        const val PRO_LIFETIME = "pro_lifetime"
        const val PRO_MONTHLY = "pro_monthly"
        const val PRO_YEARLY = "pro_yearly"

        internal fun formatBillingPeriodShort(context: Context, isoPeriod: String): String {
            return when {
                isoPeriod.endsWith("D", ignoreCase = true) -> context.getString(R.string.period_day)
                isoPeriod.endsWith("W", ignoreCase = true) -> context.getString(R.string.period_week)
                isoPeriod.endsWith("M", ignoreCase = true) -> context.getString(R.string.period_month)
                isoPeriod.endsWith("Y", ignoreCase = true) -> context.getString(R.string.period_year)
                else -> context.getString(R.string.period_month)
            }
        }

        internal fun formatTrialDuration(context: Context, isoPeriod: String): String {
            val match = Regex("^P(\\d+)([DWMY])$", RegexOption.IGNORE_CASE).matchEntire(isoPeriod.trim())
                ?: return context.getString(R.string.duration_days, 7)
            val count = match.groupValues[1].toIntOrNull() ?: 7
            return when (match.groupValues[2].uppercase()) {
                "D" -> context.getString(R.string.duration_days, count)
                "W" -> if (count == 1) context.getString(R.string.duration_days, 7)
                else context.getString(R.string.duration_weeks, count)
                "M" -> if (count == 1) context.getString(R.string.duration_one_month)
                else context.getString(R.string.duration_months, count)
                "Y" -> if (count == 1) context.getString(R.string.duration_one_year)
                else context.getString(R.string.duration_years, count)
                else -> context.getString(R.string.duration_days, 7)
            }
        }

        internal fun calculateAnnualSavingsPercent(
            monthlyPriceMicros: Long?,
            yearlyPriceMicros: Long?
        ): Int? {
            if (monthlyPriceMicros == null || yearlyPriceMicros == null) return null
            val annualAtMonthlyRate = monthlyPriceMicros * 12L
            if (annualAtMonthlyRate <= 0L || yearlyPriceMicros >= annualAtMonthlyRate) return null
            val pct = (((annualAtMonthlyRate - yearlyPriceMicros) * 100.0) / annualAtMonthlyRate).toInt()
            return pct.takeIf { it > 0 }
        }
    }

    init {
        startConnection()
    }

    fun refreshProducts() {
        if (billingClient.isReady) {
            queryPurchases()
            queryProductDetails()
        } else if (!isConnecting) {
            startConnection()
        }
    }

    private fun startConnection() {
        if (isConnecting) return
        isConnecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    queryProductDetails()
                } else {
                    _pricingState.value = _pricingState.value.copy(isLoading = false)
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
                _pricingState.value = _pricingState.value.copy(isLoading = false)
            }
        })
    }

    private fun queryProductDetails() {
        _pricingState.value = _pricingState.value.copy(isLoading = true)

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_LIFETIME)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_YEARLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        val pendingQueries = AtomicInteger(2)

        billingClient.queryProductDetailsAsync(inAppParams) { result, inAppResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                inAppResult.productDetailsList?.forEach { product ->
                    productMap[product.productId] = product
                }
            }
            publishProductState(stillLoading = pendingQueries.decrementAndGet() > 0)
        }

        billingClient.queryProductDetailsAsync(subsParams) { result, subResultDetails ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                subResultDetails.productDetailsList?.forEach { product ->
                    productMap[product.productId] = product
                }
            }
            publishProductState(stillLoading = pendingQueries.decrementAndGet() > 0)
        }
    }

    private fun publishProductState(stillLoading: Boolean) {
        val currentList = productMap.values.toList()
        _productDetailsList.value = currentList

        val monthlyProduct = productMap[PRO_MONTHLY]
        val yearlyProduct = productMap[PRO_YEARLY]
        val lifetimeProduct = productMap[PRO_LIFETIME]

        val monthlyInfo = monthlyProduct?.let { extractSubscriptionInfo(it, savingsPercent = null) }
        val yearlyBaseInfo = yearlyProduct?.let { extractSubscriptionInfo(it, savingsPercent = null) }
        val savings = calculateAnnualSavingsPercent(
            monthlyPriceMicros = monthlyInfo?.recurringPriceMicros,
            yearlyPriceMicros = yearlyBaseInfo?.recurringPriceMicros
        )
        val yearlyInfo = yearlyBaseInfo?.copy(savingsPercent = savings)
        val lifetimePrice = lifetimeProduct?.oneTimePurchaseOfferDetails?.formattedPrice

        _pricingState.value = PaywallPricingState(
            isLoading = stillLoading,
            monthly = monthlyInfo,
            yearly = yearlyInfo,
            lifetimeFormattedPrice = lifetimePrice
        )
    }

    private fun selectBestOffer(product: ProductDetails): ProductDetails.SubscriptionOfferDetails? {
        val offers = product.subscriptionOfferDetails ?: return null
        return offers.find { offer ->
            offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
        } ?: offers.firstOrNull()
    }

    private fun extractSubscriptionInfo(
        product: ProductDetails,
        savingsPercent: Int?
    ): SubscriptionOfferInfo? {
        val offer = selectBestOffer(product) ?: return null
        val phases = offer.pricingPhases.pricingPhaseList
        val recurringPhase = phases.lastOrNull() ?: return null
        val freeTrialPhase = phases.firstOrNull()?.takeIf {
            it.priceAmountMicros == 0L && phases.size > 1
        }

        return SubscriptionOfferInfo(
            recurringFormattedPrice = recurringPhase.formattedPrice,
            recurringPriceMicros = recurringPhase.priceAmountMicros,
            periodLabel = formatBillingPeriodShort(context, recurringPhase.billingPeriod),
            hasFreeTrial = freeTrialPhase != null,
            trialFormattedPrice = freeTrialPhase?.formattedPrice,
            trialDurationLabel = freeTrialPhase?.let { formatTrialDuration(context, it.billingPeriod) },
            savingsPercent = savingsPercent
        )
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, inAppPurchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                hasActiveInApp = hasActiveProPurchase(inAppPurchases)
                handlePurchases(inAppPurchases)
                _isPro.value = hasActiveInApp || hasActiveSubs
            }
        }
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, subsPurchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                hasActiveSubs = hasActiveProPurchase(subsPurchases)
                handlePurchases(subsPurchases)
                _isPro.value = hasActiveInApp || hasActiveSubs
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        }
    }

    private fun hasActiveProPurchase(purchases: List<Purchase>): Boolean {
        return purchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED && (
                purchase.products.contains(PRO_LIFETIME) ||
                    purchase.products.contains(PRO_MONTHLY) ||
                    purchase.products.contains(PRO_YEARLY)
                )
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.products.contains(PRO_LIFETIME) ||
                purchase.products.contains(PRO_MONTHLY) ||
                purchase.products.contains(PRO_YEARLY)
            ) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    _isPro.value = true
                    if (!purchase.isAcknowledged) {
                        billingClient.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                        ) {}
                    }
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String): Boolean {
        return try {
            val product = productMap[productId] ?: _productDetailsList.value.find { it.productId == productId }
            if (product != null) {
                val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)

                if (product.productType == BillingClient.ProductType.SUBS) {
                    val bestOffer = selectBestOffer(product)
                    val offerToken = bestOffer?.offerToken
                    if (offerToken.isNullOrEmpty()) {
                        Log.w("BillingManager", "No active offerToken found for subscription $productId")
                        return false
                    }
                    paramsBuilder.setOfferToken(offerToken)
                }

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(paramsBuilder.build()))
                    .build()

                val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                launchResult.responseCode == BillingClient.BillingResponseCode.OK
            } else {
                refreshProducts()
                Toast.makeText(
                    context,
                    context.getString(R.string.billing_loading_retry),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        } catch (e: Exception) {
            Log.e("BillingManager", "Failed to launch billing flow", e)
            false
        }
    }

    fun terminate() {
        isConnecting = false
        try {
            billingClient.endConnection()
        } catch (_: Exception) {
        }
    }
}

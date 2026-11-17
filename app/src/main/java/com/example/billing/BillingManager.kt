package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    companion object {
        const val PRO_LIFETIME = "pro_lifetime"
        const val PRO_MONTHLY = "pro_monthly"
        const val PRO_YEARLY = "pro_yearly"
    }

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                    queryProductDetails()
                }
            }
            override fun onBillingServiceDisconnected() {
                try {
                    startConnection()
                } catch (e: Exception) {
                    Log.e("BillingManager", "Error reconnecting to billing service", e)
                }
            }
        })
    }

    private fun queryProductDetails() {
        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRO_LIFETIME)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )).build()

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRO_MONTHLY)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRO_YEARLY)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )).build()

        val allProducts = mutableListOf<ProductDetails>()

        billingClient.queryProductDetailsAsync(inAppParams) { result, inAppResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                inAppResult.productDetailsList?.let { allProducts.addAll(it) }
                
                billingClient.queryProductDetailsAsync(subsParams) { subResult, subResultDetails ->
                    if (subResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        subResultDetails.productDetailsList?.let { allProducts.addAll(it) }
                        _productDetailsList.value = allProducts
                    }
                }
            }
        }
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { result, inAppPurchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(inAppPurchases)
            }
        }
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { result, subsPurchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(subsPurchases)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.products.contains(PRO_LIFETIME) || 
                purchase.products.contains(PRO_MONTHLY) || 
                purchase.products.contains(PRO_YEARLY)) {
                
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

    fun launchBillingFlow(activity: Activity, productId: String) {
        try {
            val product = _productDetailsList.value.find { it.productId == productId }
            if (product != null) {
                val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)

                if (product.productType == BillingClient.ProductType.SUBS) {
                    val offers = product.subscriptionOfferDetails
                    // Pick the offer with free trial (pricingPhase with priceAmountMicros == 0L) or the first available
                    val bestOffer = offers?.find { offer ->
                        offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
                    } ?: offers?.firstOrNull()

                    val offerToken = bestOffer?.offerToken
                    if (offerToken.isNullOrEmpty()) {
                        Log.w("BillingManager", "No active offerToken found for subscription $productId")
                        return
                    }
                    paramsBuilder.setOfferToken(offerToken)
                }

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(paramsBuilder.build()))
                    .build()

                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                // Simulated local unlock for the preview emulator / testing
                _isPro.value = true
            }
        } catch (e: Exception) {
            Log.e("BillingManager", "Failed to launch billing flow", e)
        }
    }
    
    fun terminate() {
        if(billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}

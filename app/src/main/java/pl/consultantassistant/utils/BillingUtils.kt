package pl.consultantassistant.utils

import android.app.Activity
import android.widget.Toast
import com.android.billingclient.api.*
import es.dmoral.toasty.Toasty
import pl.consultantassistant.R

class BillingUtils(private val activity: Activity) : PurchasesUpdatedListener {

    private var instance: BillingUtils? = null
    var purchaseListener: PurchaseListener? = null

    private lateinit var billingClient: BillingClient
    private val skuList = listOf("access_to_app")

    @Synchronized
    private fun createInstance() {
        if (instance == null) {
            instance = BillingUtils(activity)
        }
    }

    fun getInstance(): BillingUtils? {
        if (instance == null) createInstance()
        return instance
    }

    fun setupBillingClient(): BillingClient {

        billingClient = BillingClient
                .newBuilder(activity)
                .enablePendingPurchases()
                .setListener(this)
                .build()

        return billingClient
    }

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    // The BillingClient is ready. You can query purchases here.
                    val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
                    val purchasesList = purchasesResult.purchasesList

                    if (purchasesList.isNotEmpty()) {

                        // Only active subscriptions
                        for (purchase in purchasesList) {
                            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                // Toasty.success(activity, "Subscription active", Toast.LENGTH_LONG).show()
                                purchaseListener?.onActiveSubscription()
                            }
                        }

                    } else {

                        Toasty.warning(
                            activity,
                            activity.getString(R.string.billing_subscription_not_active_toast),
                            Toast.LENGTH_LONG
                        ).show()

                        val params = SkuDetailsParams
                            .newBuilder()
                            .setSkusList(skuList)
                            .setType(BillingClient.SkuType.SUBS)
                            .build()

                        billingClient.querySkuDetailsAsync(params) { billingResultCode, skuDetailsList ->
                            if (billingResultCode.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {

                                for (skuDetails in skuDetailsList) {
                                    val flowParams = BillingFlowParams
                                        .newBuilder()
                                        .setSkuDetails(skuDetails)
                                        .build()
                                    billingClient.launchBillingFlow(activity, flowParams)
                                }
                            }
                        }

                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                // Log.d("BILLING", "CLIENT SETUP FAILED")
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if ((billingResult.responseCode == BillingClient.BillingResponseCode.OK || billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) && purchases != null) {
            for (purchase in purchases) {
                purchaseListener?.handlePurchase(purchase)
            }
        } else {
            // Handle any other error codes.
            activity.finish()
        }
    }

    interface PurchaseListener {
        fun handlePurchase(purchase: Purchase)
        fun onActiveSubscription()
    }
}
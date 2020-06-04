package pl.consultantassistant.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import es.dmoral.toasty.Toasty

class BillingUtils(private val activity: Activity) : PurchasesUpdatedListener {

    private var instance: BillingUtils? = null
    var purchaseHandlerListener: PurchaseHandler? = null

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

                    Log.d("BILLING", "CLIENT SETUP FINISHED")

                    // The BillingClient is ready. You can query purchases here.
                    val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
                    val purchasesList = purchasesResult.purchasesList

                    if (purchasesList != null) {
                        for (purchase in purchasesList) {
                            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                Toasty.success(activity, "Subscription active", Toast.LENGTH_LONG).show()
                            } else {
                                Toasty.success(activity, "Please activate a subscription to gain access", Toast.LENGTH_LONG).show()
                                startPurchaseFlow()
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("BILLING", "CLIENT SETUP FAILED")
            }
        })
    }

    private fun startPurchaseFlow() {

        val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.SUBS)
                .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {

                Log.d("BILLING", skuDetailsList.toString())

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

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                purchaseHandlerListener?.handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            activity.finish()
        } else {
            // Handle any other error codes.
            activity.finish()
        }
    }

    interface PurchaseHandler {
        fun handlePurchase(purchase: Purchase)
    }
}
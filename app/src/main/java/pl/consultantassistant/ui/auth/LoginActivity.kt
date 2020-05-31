package pl.consultantassistant.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.login_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import pl.consultantassistant.R
import pl.consultantassistant.databinding.LoginLayoutBinding
import pl.consultantassistant.utils.AuthListener
import pl.consultantassistant.utils.startHomeActivity

class LoginActivity : AppCompatActivity(), AuthListener, KodeinAware, PurchasesUpdatedListener {

    override val kodein by kodein()
    private val factory by instance<AuthViewModelFactory>()
    private lateinit var viewModel: AuthViewModel
    private lateinit var billingClient: BillingClient
    private val skuList = listOf("access_to_app")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBillingClient()

        val binding: LoginLayoutBinding =
            DataBindingUtil.setContentView(this, R.layout.login_layout)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this
    }

    private fun setupBillingClient() {

        billingClient = BillingClient
            .newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    Log.d("BILLING", "CLIENT SETUP FINISHED")

                    // The BillingClient is ready. You can query purchases here.
                    val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
                    val purchasesList = purchasesResult.purchasesList

                    if (purchasesList != null) {
                        for (purchase in purchasesList) {
                            Toasty.success(
                                this@LoginActivity,
                                "Product ${purchase.sku} active",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toasty.error(
                            this@LoginActivity,
                            "Please activate a subscription",
                            Toast.LENGTH_LONG
                        ).show()
                        startPurchaseFlow()
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
                    billingClient.launchBillingFlow(this@LoginActivity, flowParams)
                }
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            finish()
        } else {
            // Handle any other error codes.
            finish()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.

            Toasty.success(this, "Access purchased", Toast.LENGTH_LONG).show()

            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                lifecycleScope.launch {
                    val ackPurchaseResult = withContext(Dispatchers.IO) {
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                    }
                }
            }
        }
    }

    override fun onStarted() {
        progress_bar.visibility = View.VISIBLE
    }

    override fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        Toasty.error(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onIncorrectEmail(errorCode: Int) {
        if (errorCode == AuthViewModel.ERROR_EMPTY_FIELD)
            edittext_email_input.error = this.getString(R.string.field_can_not_be_empty)
    }

    override fun onIncorrectPassword(errorCode: Int) {
        when (errorCode) {
            AuthViewModel.ERROR_EMPTY_FIELD -> edittext_password_input.error =
                this.getString(R.string.field_can_not_be_empty)
            AuthViewModel.ERROR_PASSWORD_LENGTH -> edittext_password_input.error =
                this.getString(R.string.password_too_weak)
            AuthViewModel.ERROR_PASSWORDS_DO_NOT_MATCH -> edittext_password_input.error =
                this.getString(R.string.passwords_dont_match)
        }
    }

    override fun onIncorrect2ndPassword(errorCode: Int) {
        // Nothing to do here
    }

    override fun onSuccess() {
        progress_bar.visibility = View.GONE
        startHomeActivity()
    }

    public override fun onStart() {
        super.onStart()
        billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        // Check if user is signed in (non-null) and update UI accordingly.
        viewModel.user?.let {
            startHomeActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        billingClient.queryPurchases(BillingClient.SkuType.SUBS)
    }

    override fun onNoConnectionAvailable() {
        Toasty.warning(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
    }
}
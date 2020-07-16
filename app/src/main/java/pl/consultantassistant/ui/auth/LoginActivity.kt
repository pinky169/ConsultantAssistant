package pl.consultantassistant.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.acknowledgePurchase
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
import pl.consultantassistant.utils.BillingUtils
import pl.consultantassistant.utils.setAppTheme
import pl.consultantassistant.utils.startHomeActivity

class LoginActivity : AppCompatActivity(), AuthListener, KodeinAware, BillingUtils.PurchaseListener {

    override val kodein by kodein()
    private val factory by instance<AuthViewModelFactory>()
    private lateinit var viewModel: AuthViewModel
    private lateinit var billingUtils: BillingUtils
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppTheme()

        billingUtils = BillingUtils(this).getInstance()!!
        billingClient = billingUtils.setupBillingClient()
        billingUtils.purchaseListener = this
        billingUtils.startConnection()

        val binding: LoginLayoutBinding =
            DataBindingUtil.setContentView(this, R.layout.login_layout)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this
    }

    override fun handlePurchase(purchase: Purchase) {

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            // Grant entitlement to the user.
            // Check if user is signed in (non-null) and update UI accordingly.
            if (viewModel.user != null) {
                startHomeActivity()
            } else {
                Toasty.success(
                    this,
                    getString(R.string.purchase_successful_text),
                    Toast.LENGTH_LONG
                ).show()
            }

            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged) {

                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Toasty.warning(this, getString(R.string.purchase_pending_text), Toast.LENGTH_LONG)
                .show()
        } else {
            Toasty.error(
                this,
                getString(R.string.purchase_unspecified_state_text),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onActiveSubscription() {
        viewModel.user?.let {
            startHomeActivity()
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
    }

    override fun onResume() {
        super.onResume()
        billingClient.queryPurchases(BillingClient.SkuType.SUBS)
    }

    override fun onNoConnectionAvailable() {
        Toasty.warning(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
    }
}
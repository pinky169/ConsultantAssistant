package pl.consultantassistant.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.login_layout.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import pl.consultantassistant.R
import pl.consultantassistant.databinding.LoginLayoutBinding
import pl.consultantassistant.utils.AuthListener
import pl.consultantassistant.utils.startHomeActivity

class LoginActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory by instance<AuthViewModelFactory>()
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: LoginLayoutBinding =
            DataBindingUtil.setContentView(this, R.layout.login_layout)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this
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
        // Check if user is signed in (non-null) and update UI accordingly.
        viewModel.user?.let {
            startHomeActivity()
        }
    }

    override fun onNoConnectionAvailable() {
        Toasty.warning(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
    }
}
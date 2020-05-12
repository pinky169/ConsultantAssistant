package pl.consultantassistant.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.signup_layout.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import pl.consultantassistant.R
import pl.consultantassistant.databinding.SignupLayoutBinding
import pl.consultantassistant.utils.AuthListener
import pl.consultantassistant.utils.startHomeActivity

class SignUpActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory by instance<AuthViewModelFactory>()
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: SignupLayoutBinding =
            DataBindingUtil.setContentView(this, R.layout.signup_layout)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this
        title = getString(R.string.sign_up_title)
    }

    override fun onStarted() {
        progress_bar.visibility = View.VISIBLE
    }

    override fun onSuccess() {
        progress_bar.visibility = View.GONE
        startHomeActivity()
    }

    override fun onFailure(message: String) {
        progress_bar.visibility = View.GONE
        Toasty.error(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onIncorrectEmail(errorCode: Int) {
        when (errorCode) {
            AuthViewModel.ERROR_EMPTY_FIELD -> edittext_email_input.error =
                this.getString(R.string.field_can_not_be_empty)
        }
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
        when (errorCode) {
            AuthViewModel.ERROR_EMPTY_FIELD -> edittext_2nd_password_input.error =
                this.getString(R.string.field_can_not_be_empty)
            AuthViewModel.ERROR_PASSWORD_LENGTH -> edittext_2nd_password_input.error =
                this.getString(R.string.password_too_weak)
            AuthViewModel.ERROR_PASSWORDS_DO_NOT_MATCH -> edittext_2nd_password_input.error =
                this.getString(R.string.passwords_dont_match)
        }
    }

    override fun onNoConnectionAvailable() {
        Toasty.warning(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
    }
}
package pl.consultantassistant.ui.new_customer_activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.new_customer_layout.*
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Customer
import pl.consultantassistant.utils.setAppTheme

class NewCustomerActivity : AppCompatActivity() {

    private lateinit var selectedUserLevel: String
    private lateinit var customerName: String
    private lateinit var customerSurname: String
    private lateinit var customerEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppTheme()
        setContentView(R.layout.new_customer_layout)

        setupViews()
        setupFAB()
    }

    private fun setupViews() {

        if (intent != null && intent.hasExtra("customer")) {

            val customer = intent.getSerializableExtra("customer") as Customer

            name_edit_text.setText(customer.name)
            surname_edit_text.setText(customer.surname)
            email_edit_text.setText(customer.email)
            customer_level_spinner.setSelection(getIndex(customer_level_spinner, customer.userLevel))
        }

        setupSpinnerContent()
    }

    private fun setupSpinnerContent() {

        val userLevels = resources.getStringArray(R.array.user_levels_array)

        customer_level_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUserLevel = userLevels[position]
            }

        }
    }

    private fun setupFAB() {
        fab_save.setOnClickListener {

            customerName = name_edit_text.text.toString().trim()
            customerSurname = surname_edit_text.text.toString().trim()
            customerEmail = email_edit_text.text.toString().trim()

            if(customerName.isBlank() && customerSurname.isBlank()) {
                name_edit_text.error = getString(R.string.name_input_error_message)
                surname_edit_text.error = getString(R.string.surname_input_error_message)
            } else if (customerName.isBlank()) {
                name_edit_text.error = getString(R.string.name_input_error_message)
            } else if (customerSurname.isBlank()) {
                surname_edit_text.error = getString(R.string.surname_input_error_message)
            } else if(customerEmail.isNotBlank() && !isEmailValid(customerEmail)) {
                email_edit_text.error = getString(R.string.email_input_wrong_format_error_message)
            } else {

                val resultIntent = Intent()
                resultIntent.putExtra("name", customerName)
                resultIntent.putExtra("surname", customerSurname)
                resultIntent.putExtra("email", customerEmail)
                resultIntent.putExtra("userLevel", selectedUserLevel)

                if (intent.hasExtra("customer") && intent.hasExtra("position")) {
                    val customer = intent.getSerializableExtra("customer") as Customer
                    val position = intent.getIntExtra("position", -1)
                    resultIntent.putExtra("changedPosition", position)
                    resultIntent.putExtra("customerID", customer.customerID)
                }

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun getIndex(spinner: Spinner, myString: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(myString, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
}
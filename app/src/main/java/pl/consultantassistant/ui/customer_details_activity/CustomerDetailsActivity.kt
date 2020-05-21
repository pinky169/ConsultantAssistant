package pl.consultantassistant.ui.customer_details_activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.customer_details_layout.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import pl.consultantassistant.R
import pl.consultantassistant.data.firebase.FirebaseQueryLiveData
import pl.consultantassistant.data.models.Customer
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModel
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModelFactory
import pl.consultantassistant.utils.LoadingListener
import androidx.navigation.ui.AppBarConfiguration as AppBarConfiguration1


class CustomerDetailsActivity : AppCompatActivity(), KodeinAware, LoadingListener {

    override val kodein by kodein()
    private val factory by instance<CustomerDetailsViewModelFactory>()
    private lateinit var viewModel: CustomerDetailsViewModel
    private lateinit var partnerID: String
    private lateinit var customerID: String
    private lateinit var customer: Customer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.customer_details_layout)

        customer = intent.getSerializableExtra("customer") as Customer
        partnerID = intent.getStringExtra("partnerID")
        customerID = customer.customerID

        viewModel = ViewModelProvider(this, factory).get(CustomerDetailsViewModel::class.java)
        FirebaseQueryLiveData.loadingListener = this
        viewModel.setPartnerId(partnerID)
        viewModel.setCustomerId(customerID)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.fragment_container)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            title = when (destination.id) {
                R.id.fragment_details -> getString(R.string.full_name, customer.name, customer.surname)
                R.id.fragment_products -> getString(R.string.section_products_header_title, customer.name, customer.surname)
                R.id.fragment_gallery -> getString(R.string.section_gallery_header_title, customer.name, customer.surname)
                else -> getString(R.string.app_name)
            }
        }

        bottom_navigation_view.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration1(
            topLevelDestinationIds = setOf(
                R.id.fragment_products,
                R.id.fragment_gallery
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onStarted() {
        customer_details_progress_bar.visibility = View.VISIBLE
    }

    override fun onSuccess() {
        customer_details_progress_bar.visibility = View.GONE
    }

    override fun onCanceled() {
        customer_details_progress_bar.visibility = View.GONE
    }
}
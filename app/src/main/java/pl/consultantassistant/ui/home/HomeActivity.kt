package pl.consultantassistant.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.home_layout.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import pl.consultantassistant.R
import pl.consultantassistant.data.firebase.FirebaseQueryLiveData
import pl.consultantassistant.ui.home.adapter.CustomersAdapter
import pl.consultantassistant.ui.home.viewmodel.HomeViewModel
import pl.consultantassistant.ui.home.viewmodel.HomeViewModelFactory
import pl.consultantassistant.utils.LoadingListener
import pl.consultantassistant.utils.CustomerItemListener
import pl.consultantassistant.utils.startLoginActivity
import pl.mymonat.activities.customer_details_activity.CustomerDetailsActivity
import pl.mymonat.activities.new_customer_activity.NewCustomerActivity
import pl.mymonat.models.Customer

class HomeActivity : AppCompatActivity(), CustomerItemListener, LoadingListener, KodeinAware {

    override val kodein by kodein()
    private val factory by instance<HomeViewModelFactory>()
    private lateinit var viewModel: HomeViewModel
    private lateinit var recyclerAdapter: CustomersAdapter
    private lateinit var partnerID: String

    companion object {
        const val CREATE_NEW_CUSTOMER_REQUEST_CODE = 777
        const val UPDATE_CUSTOMER_REQUEST_CODE = 888
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        setupRecyclerView()
        setupFloatingActionButton()
        FirebaseQueryLiveData.loadingListener = this

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        partnerID = viewModel.partnerID!!

        loadPartner(viewModel)
        loadCustomers(viewModel)
    }

    private fun loadPartner(viewModel: HomeViewModel) {
        viewModel.getPartner().observe(this@HomeActivity, Observer {
            if (it.name.isNotBlank() && it.surname.isNotBlank()) {
                title = getString(R.string.full_name, it.name, it.surname)
            }
            getDeviceTokenAndUpload()
        })
    }

    private fun loadCustomers(viewModel: HomeViewModel) {
        viewModel.getCustomers().observe(this@HomeActivity, Observer { customers ->
            if (customers.isEmpty()) {
                showEmptyView(true)
            } else {
                showEmptyView(false)
            }
            recyclerAdapter.submitList(customers)
        })
    }

    private fun getDeviceTokenAndUpload() {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val deviceToken = sharedPreferences.getString("device_token", null)!!
        val userId = viewModel.partnerID!!

        viewModel.insertToken(userId, deviceToken)
    }

    private fun showEmptyView(boolean: Boolean) {
        if (boolean)
         customers_empty_view.visibility = View.VISIBLE
        else
         customers_empty_view.visibility = View.GONE
    }

    private fun setupFloatingActionButton() {
        fab.setOnClickListener {
            val intent = Intent(this, NewCustomerActivity::class.java)
            startActivityForResult(intent, CREATE_NEW_CUSTOMER_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_NEW_CUSTOMER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                val customerName = data!!.getStringExtra("name")
                val customerSurname = data.getStringExtra("surname")
                val customerEmail = data.getStringExtra("email")
                val userLevel = data.getStringExtra("userLevel")

                val newCustomerReference = viewModel.getSpecificPartnerCustomersReference(partnerID).push()
                val newCustomerID = newCustomerReference.key!!
                val newCustomer = Customer(newCustomerID, customerName, customerSurname, customerEmail, userLevel)

                viewModel.insertCustomer(partnerID, newCustomerID, newCustomer)

                Toasty.success(this, getString(R.string.customer_added_successfully_text), Toast.LENGTH_LONG).show()
            }
        } else if(requestCode == UPDATE_CUSTOMER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                val customerID = data!!.getStringExtra("customerID")
                val customerName = data.getStringExtra("name")
                val customerSurname = data.getStringExtra("surname")
                val customerEmail = data.getStringExtra("email")
                val userLevel = data.getStringExtra("userLevel")

                val updatedCustomer = Customer(customerID, customerName, customerSurname, customerEmail, userLevel)

                viewModel.updateCustomer(partnerID, updatedCustomer)
            }
        }
    }

    private fun setupRecyclerView() {

        recyclerAdapter = CustomersAdapter()
        recyclerAdapter.itemListener = this
        val recyclerLayoutManager = LinearLayoutManager(this)

        customers_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            adapter = recyclerAdapter
        }
    }

    override fun createPopupMenu(view: View, customer: Customer) {
        // Creating a popup menu
        val popup = PopupMenu(view.context, view)

        // Inflating menu from xml resource
        popup.inflate(R.menu.home_recycler_view_item_menu)

        // Set menu item text
        popup.menu.getItem(0).title = getString(R.string.menu_edit_person_text, customer.name, customer.surname)
        popup.menu.getItem(1).title = getString(R.string.menu_delete_person_text, customer.name, customer.surname)

        // Adding click listener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.home_recycler_view_item_action_delete -> {
                    viewModel.deleteCustomer(partnerID, customer.customerID)
                    Toasty.success(
                        this,
                        getString(
                            R.string.menu_delete_person_success_text,
                            customer.name,
                            customer.surname
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    true
                }
                R.id.home_recycler_view_item_action_edit -> {
                    val intent = Intent(this, NewCustomerActivity::class.java)
                    intent.putExtra("customer", customer)
                    startActivityForResult(intent, UPDATE_CUSTOMER_REQUEST_CODE)
                    true
                }
                else -> false
            }
        }

        //displaying the popup
        popup.show()
    }

    override fun onItemClicked(customer: Customer) {
        val intent = Intent(this, CustomerDetailsActivity::class.java)
        intent.putExtra("partnerID", partnerID)
        intent.putExtra("customer", customer)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.home_menu, menu)

        val searchItem: MenuItem = menu!!.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchQuery: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                viewModel.search(searchQuery!!)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_logout -> {
                viewModel.logout()
                startLoginActivity()
                finish()
                Toasty.info(this, getString(R.string.log_out_confirmation), Toast.LENGTH_LONG).show()
            }
        }
        return true
    }

    override fun onStarted() {
        home_progress_bar.visibility = View.VISIBLE
    }

    override fun onSuccess() {
        home_progress_bar.visibility = View.GONE
    }

    override fun onCanceled() {
        home_progress_bar.visibility = View.GONE
    }
}

package pl.consultantassistant.ui.home

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.CalendarContract.Events
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.home_layout.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import pl.consultantassistant.R
import pl.consultantassistant.data.firebase.FirebaseQueryLiveData
import pl.consultantassistant.data.models.Customer
import pl.consultantassistant.databinding.HomeLayoutBinding
import pl.consultantassistant.ui.customer_details_activity.CustomerDetailsActivity
import pl.consultantassistant.ui.home.adapter.CustomersAdapter
import pl.consultantassistant.ui.home.viewmodel.HomeViewModel
import pl.consultantassistant.ui.home.viewmodel.HomeViewModelFactory
import pl.consultantassistant.ui.new_customer_activity.NewCustomerActivity
import pl.consultantassistant.utils.CustomerItemListener
import pl.consultantassistant.utils.LoadingListener
import pl.consultantassistant.utils.setAppTheme
import pl.consultantassistant.utils.startLoginActivity


class HomeActivity : AppCompatActivity(), CustomerItemListener, LoadingListener, KodeinAware {

    override val kodein by kodein()
    private val factory by instance<HomeViewModelFactory>()
    private lateinit var viewModel: HomeViewModel
    private lateinit var recyclerAdapter: CustomersAdapter
    private lateinit var partnerID: String

    companion object {
        const val CREATE_NEW_CUSTOMER_REQUEST_CODE = 777
        const val UPDATE_CUSTOMER_REQUEST_CODE = 888
        const val ORDER_ALPHABETICALLY = "surname"
        const val ORDER_USER_LEVEL = "userLevel"
        const val THEME_LIGHT = "day"
        const val THEME_DARK = "night"
        const val THEME_SHARED_PREFERENCES = "themePreferences"
        const val THEME_MODE = "themeMode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAppTheme()

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        partnerID = viewModel.partnerID!!

        val binding: HomeLayoutBinding = DataBindingUtil.setContentView(this, R.layout.home_layout)
        binding.apply {
            lifecycleOwner = this@HomeActivity
            viewmodel = viewModel
        }

        setupRecyclerView()
        setupFloatingActionButton()
        FirebaseQueryLiveData.loadingListener = this

        viewModel.setSortingOrder(getSortingOrder()!!)
        loadPartner(viewModel)
        loadCustomers(viewModel)
    }

    private fun loadPartner(viewModel: HomeViewModel) {
        viewModel.getPartner().observe(this@HomeActivity, Observer {
            getDeviceTokenAndUpload()
        })
    }

    private fun loadCustomers(viewModel: HomeViewModel) {
        viewModel.getCustomers().observe(this@HomeActivity, Observer { customers ->
            recyclerAdapter.originalList = customers
            recyclerAdapter.submitList(customers)
        })
    }

    private fun getDeviceTokenAndUpload() {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val deviceToken = sharedPreferences.getString("device_token", null)!!
        val userId = viewModel.partnerID!!

        viewModel.insertToken(userId, deviceToken)
    }

    private fun setupFloatingActionButton() {
        fab.setOnClickListener {
            val intent = Intent(this, NewCustomerActivity::class.java)
            startActivityForResult(intent, CREATE_NEW_CUSTOMER_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CREATE_NEW_CUSTOMER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val customerName = data!!.getStringExtra("name")
            val customerSurname = data.getStringExtra("surname")
            val customerEmail = data.getStringExtra("email")
            val userLevel = data.getStringExtra("userLevel")

            val newCustomerReference =
                    viewModel.getSpecificPartnerCustomersReference(partnerID).push()
            val newCustomerID = newCustomerReference.key!!
            val newCustomer =
                    Customer(newCustomerID, customerName, customerSurname, customerEmail, userLevel)

            viewModel.insertCustomer(partnerID, newCustomerID, newCustomer)

            Toasty.success(
                    this,
                    getString(R.string.customer_added_successfully_text),
                    Toast.LENGTH_LONG
            ).show()

        } else if (requestCode == UPDATE_CUSTOMER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val changedPosition = data!!.getIntExtra("changedPosition", -1)
            val customerID = data.getStringExtra("customerID")
            val customerName = data.getStringExtra("name")
            val customerSurname = data.getStringExtra("surname")
            val customerEmail = data.getStringExtra("email")
            val userLevel = data.getStringExtra("userLevel")

            val updatedCustomer =
                    Customer(customerID, customerName, customerSurname, customerEmail, userLevel)

            viewModel.updateCustomer(partnerID, updatedCustomer)
            recyclerAdapter.notifyItemChanged(changedPosition)
        }
    }

    private fun setupRecyclerView() {

        recyclerAdapter = CustomersAdapter(this)
        val recyclerLayoutManager = LinearLayoutManager(this)

        customers_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            adapter = recyclerAdapter
        }
    }

    private fun setDialogOnClickListener(partnerID: String, customer: Customer): DialogInterface.OnClickListener {

        return DialogInterface.OnClickListener { _, whichButton ->
            when (whichButton) {
                DialogInterface.BUTTON_POSITIVE -> {

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

                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    return@OnClickListener
                }
            }
        }
    }

    override fun createPopupMenu(view: View, position: Int, customer: Customer) {

        // Creating a popup menu
        val popup = PopupMenu(view.context, view)

        // Inflating menu from xml resource
        popup.inflate(R.menu.home_recycler_view_item_menu)

        // Set menu item text
        popup.menu.getItem(0).title =
                getString(R.string.menu_edit_person_text, customer.name, customer.surname)
        popup.menu.getItem(1).title =
                getString(R.string.menu_delete_person_text, customer.name, customer.surname)

        // Adding click listener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.home_recycler_view_item_action_delete -> {

                    val materialAlertDialogTheme = when (getSharedPreferences(THEME_SHARED_PREFERENCES, Context.MODE_PRIVATE).getString("themeMode", THEME_LIGHT)) {
                        THEME_LIGHT -> R.style.ThemeOverlay_App_MaterialAlertDialog_Day
                        THEME_DARK -> R.style.ThemeOverlay_App_MaterialAlertDialog_Night
                        else -> null
                    }

                    MaterialAlertDialogBuilder(this, materialAlertDialogTheme!!)
                            .setTitle(getString(R.string.alert_dialog_delete_customer_title, customer.name, customer.surname))
                            .setPositiveButton(getString(R.string.alert_dialog_button_positive_text), setDialogOnClickListener(partnerID, customer))
                            .setNegativeButton(getString(R.string.alert_dialog_button_negative_text), setDialogOnClickListener(partnerID, customer))
                            .show()

                    true
                }

                R.id.home_recycler_view_item_action_edit -> {
                    val intent = Intent(this, NewCustomerActivity::class.java)
                    intent.putExtra("position", position)
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

        menuInflater.inflate(R.menu.home_menu, menu!!)

        /********************************
         * Sorting order checkbox setup *
         ********************************/
        val sortingOrder = getSortingOrder()

        if (sortingOrder.equals(ORDER_USER_LEVEL))
            menu.findItem(R.id.menu_action_sort_by_customer_level).isChecked = true
        else if (sortingOrder.equals(ORDER_ALPHABETICALLY))
            menu.findItem(R.id.menu_action_sort_alphabetically).isChecked = true

        /************************
         * Theme checkbox setup *
         ************************/
        val themeMode = getSharedPreferences("themePreferences", Context.MODE_PRIVATE).getString("themeMode", "light")

        if (themeMode == THEME_LIGHT)
            menu.findItem(R.id.menu_style_mode).isChecked = false
        else if (themeMode == THEME_DARK)
            menu.findItem(R.id.menu_style_mode).isChecked = true

        /********************
         * SearchView setup *
         ********************/
        val searchItem: MenuItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(searchQuery: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                recyclerAdapter.filter?.filter(searchQuery)
                return true
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

            R.id.menu_notification -> {
                openCalendarNewEvent()
            }

            R.id.menu_action_sort_alphabetically -> {
                item.isChecked = !item.isChecked
                viewModel.setSortingOrder(ORDER_ALPHABETICALLY)
                saveSortingOrder(ORDER_ALPHABETICALLY)
            }

            R.id.menu_action_sort_by_customer_level -> {
                item.isChecked = !item.isChecked
                viewModel.setSortingOrder(ORDER_USER_LEVEL)
                saveSortingOrder(ORDER_USER_LEVEL)
            }
            R.id.menu_style_mode -> {

                if (item.isChecked) {
                    getSharedPreferences("themePreferences", Context.MODE_PRIVATE).edit().putString("themeMode", "day").apply()
                    item.isChecked = false
                } else {
                    getSharedPreferences("themePreferences", Context.MODE_PRIVATE).edit().putString("themeMode", "night").apply()
                    item.isChecked = true
                }

                // Recreate to apply new theme
                recreate()

                /**
                If you need to apply animation use

                val intent = intent
                finish()
                startActivity(intent)

                 */
            }
        }

        return true
    }

    private fun saveSortingOrder(order: String?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this@HomeActivity)
        prefs.edit().putString("SORTING_ORDER", order).apply()
    }

    private fun getSortingOrder(): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this@HomeActivity)
        return prefs.getString("SORTING_ORDER", ORDER_ALPHABETICALLY)
    }

    private fun openCalendarNewEvent() {

        val intent = Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)

        startActivity(intent)
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

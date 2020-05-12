package pl.consultantassistant.ui.customer_details_activity.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.fragment_products.*
import pl.consultantassistant.R
import pl.consultantassistant.ui.customer_details_activity.adapter.ProductsAdapter
import pl.consultantassistant.ui.customer_details_activity.fragments.DetailsFragment.Companion.EDITING_STATE_DISABLED
import pl.consultantassistant.ui.customer_details_activity.fragments.DetailsFragment.Companion.EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY
import pl.consultantassistant.ui.customer_details_activity.fragments.DetailsFragment.Companion.EDITING_STATE_ENABLED
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModel
import pl.mymonat.models.Product

class ProductsFragment() : Fragment() {

    // ViewModel
    private lateinit var viewModel: CustomerDetailsViewModel

    // Products RecyclerView Adapter
    private lateinit var recyclerAdapter: ProductsAdapter

    // Partner aka logged in user
    private lateinit var partnerID: String

    // ID of the selected customer
    private lateinit var customerID: String

    // State with information if editing
    // the list of customer products is enabled or not
    private var editingState: String = ""

    // Array of strings with names od products
    private var allProductsArray: Array<String> = arrayOf()

    // ArrayList with current products
    // used to select those products in chip group
    private var currentProducts: ArrayList<Product> = arrayListOf()

    // List with selected products in chip group
    // which will be send to database
    private var productsToSubmit: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupListeners()
        setupRecyclerView()
        loadAllPossibleProducts()

        viewModel = ViewModelProvider(requireActivity()).get(CustomerDetailsViewModel::class.java)
        viewModel.getPartnerId().observe(viewLifecycleOwner, Observer { partnerID = it })
        viewModel.getCustomerId().observe(viewLifecycleOwner, Observer { customerID = it })
        viewModel.getCustomerProducts().observe(viewLifecycleOwner, Observer { productList ->
            showOrHideProducts(productList)
            recyclerAdapter.submitList(productList)
            currentProducts.clear()
            currentProducts.addAll(productList)
        })
    }

    private fun setupRecyclerView() {

        recyclerAdapter = ProductsAdapter()
        val recyclerLayoutManager = LinearLayoutManager(requireContext())

        products_recycler_view.apply {
            layoutManager = recyclerLayoutManager
            adapter = recyclerAdapter
        }
    }

    private fun loadAllPossibleProducts() {

        allProductsArray = requireActivity().resources.getStringArray(R.array.product_list)

        for (product in allProductsArray) {
            val chip = Chip(requireContext())
            chip.text = product
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    productsToSubmit.add(button.text.toString())
                } else {
                    productsToSubmit.remove(button.text.toString())
                }
            }
            all_products_chip_group.addView(chip)
        }
    }

    private fun showOrHideProducts(products: List<Product>) {
        if (products.isNotEmpty()) {
            products_recycler_view.visibility = View.VISIBLE
            products_empty_view.visibility = View.GONE
            editingState = EDITING_STATE_DISABLED
        } else {
            products_recycler_view.visibility = View.GONE
            products_empty_view.visibility = View.VISIBLE
            editingState = EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY
        }

        // Update menu
        requireActivity().invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.products_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.products_action_edit -> switchProductsView()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuButton = menu.findItem(R.id.products_action_edit)
        when (editingState) {
            EDITING_STATE_ENABLED -> {
                menuButton.isVisible = true
                menuButton.setIcon(R.drawable.ic_save)
            }
            EDITING_STATE_DISABLED -> {
                menuButton.setIcon(R.drawable.ic_edit)
            }
            EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY -> {
                menuButton.isVisible = false
            }
        }
    }

    private fun switchProductsView() {
        if (editingState == EDITING_STATE_DISABLED || editingState == EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY) {

            // Clear checks first
            all_products_chip_group.clearCheck()

            // Select all customer products when opening the chip group
            // so the partner knows which products were selected previously
            for (chip in all_products_chip_group.children) {
                val currentChip = chip as Chip
                val currentChipId = currentChip.id
                for (customerProduct in currentProducts) {
                    if (currentChip.text.toString() == customerProduct.productName) {
                        all_products_chip_group.check(currentChipId)
                    }
                }
            }

            products_empty_view.visibility = View.GONE
            products_view_switcher.showNext()
            editingState = EDITING_STATE_ENABLED

        } else if (editingState == EDITING_STATE_ENABLED) {
            products_view_switcher.showPrevious()
            saveProducts()
            editingState = EDITING_STATE_DISABLED
        }

        requireActivity().invalidateOptionsMenu()
    }

    private fun saveProducts() {

        viewModel.deleteAllCustomerProducts(partnerID, customerID).invokeOnCompletion {
            for (product in productsToSubmit) {

                val newProductReference =
                    viewModel.getCustomerProductsReference(partnerID, customerID).push()
                val newProductKey = newProductReference.key!!
                val newProduct = Product(customerID, newProductKey, product)

                viewModel.insertProduct(partnerID, newProduct)
            }
        }
    }

    private fun setupListeners() {
        products_empty_view.setOnClickListener { switchProductsView() }
    }
}
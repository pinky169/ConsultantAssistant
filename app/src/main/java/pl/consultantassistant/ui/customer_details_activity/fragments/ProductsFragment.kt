package pl.consultantassistant.ui.customer_details_activity.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.fragment_products.*
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Product
import pl.consultantassistant.databinding.FragmentProductsBinding
import pl.consultantassistant.ui.customer_details_activity.adapter.ProductsAdapter
import pl.consultantassistant.ui.customer_details_activity.fragments.DetailsFragment.Companion.EDITING_STATE_DISABLED
import pl.consultantassistant.ui.customer_details_activity.fragments.DetailsFragment.Companion.EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY
import pl.consultantassistant.ui.customer_details_activity.fragments.DetailsFragment.Companion.EDITING_STATE_ENABLED
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModel
import java.util.*

class ProductsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    // ViewModel
    private lateinit var viewModel: CustomerDetailsViewModel

    // Data binding for this fragment
    private lateinit var binding : FragmentProductsBinding

    // Products RecyclerView Adapter
    private lateinit var recyclerAdapter: ProductsAdapter

    // Partner aka logged in user
    private lateinit var partnerID: String

    // ID of the selected customer
    private lateinit var customerID: String

    // Type of products to load into RecyclerView
    // 0 - customer products actually bought
    // 1 - proposed products for customer to buy in the future
    // 2 - products samples chosen for customer
    private var productsType: Int = 0

    // State with information if editing
    // the list of customer products is enabled or not
    private var editingState: String = ""

    // Array of strings with names od products
    private var allProductsArray: ArrayList<Array<String>> = arrayListOf()

    // ArrayList with current products
    // used to select those products in chip group
    private var currentProducts: ArrayList<String> = arrayListOf()

    // List with selected products in chip group
    // which will be send to database
    private var productsToSubmit: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_products, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupListeners()
        setupRecyclerView()
        getAllPossibleProducts()
        loadAllPossibleProducts()

        viewModel = ViewModelProvider(requireActivity()).get(CustomerDetailsViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.getPartnerId().observe(viewLifecycleOwner, Observer { partnerID = it })
        viewModel.getCustomerId().observe(viewLifecycleOwner, Observer { customerID = it })
        viewModel.getProductsType().observe(viewLifecycleOwner, Observer { productsType = it })
        viewModel.getCustomerProducts().observe(viewLifecycleOwner, Observer { productList ->
            setEditingState(productList)
            recyclerAdapter.submitList(productList)
            currentProducts.clear()
            productList.forEach { product -> currentProducts.add(product.productName) }
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

        for (productsArray in allProductsArray) {

            val chipGroup = ChipGroup(requireContext())
            val color: Int = generateRandomColor()

            for (product in productsArray) {
                val chip = Chip(requireContext())
                chip.text = product
                chip.chipBackgroundColor = ColorStateList.valueOf(color)
                chip.isAllCaps = true
                chip.isCheckable = true
                chip.setOnCheckedChangeListener { button, isChecked ->
                    if (isChecked) {
                        productsToSubmit.add(button.text.toString())
                    } else {
                        productsToSubmit.remove(button.text.toString())
                    }
                }

                chipGroup.addView(chip)
            }

            all_products_view.addView(chipGroup)
        }
    }

    private fun setEditingState(products: List<Product>) {

        editingState = if (products.isEmpty())
            EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY
        else
            EDITING_STATE_DISABLED

        // Update menu - calls onPrepareOptionsMenu()
        requireActivity().invalidateOptionsMenu()

        // Always show customer products first
        products_view_switcher.displayedChild = 0
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
                menuButton.isVisible = true
                menuButton.setIcon(R.drawable.ic_edit)
            }
            EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY -> {
                menuButton.isVisible = false
            }
        }
    }

    private fun switchProductsView() {

        if (editingState == EDITING_STATE_DISABLED || editingState == EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY) {

            // Select all customer products when opening the chip group
            // so the partner knows which products were selected previously
            for (child in all_products_view.children) {

                val chipGroup = child as ChipGroup
                chipGroup.clearCheck()

                for (chip in chipGroup.children) {

                    val currentChip = chip as Chip
                    val currentChipId = currentChip.id

                    for (customerProduct in currentProducts) {
                        if (currentChip.text.toString() == customerProduct) {
                            chipGroup.check(currentChipId)
                        }
                    }
                }
            }

            products_empty_view.visibility = View.GONE
            products_view_switcher.visibility = View.VISIBLE
            products_view_switcher.showNext()
            editingState = EDITING_STATE_ENABLED

        } else if (editingState == EDITING_STATE_ENABLED) {
            products_view_switcher.showPrevious()
            saveProducts()
            editingState = EDITING_STATE_DISABLED
        }

        requireActivity().invalidateOptionsMenu()
    }

    private fun hasSelectionChanged() : Boolean {
        return if (productsToSubmit.size != currentProducts.size)
            true
        else return productsToSubmit != currentProducts
    }

    private fun saveProducts() {

        productsToSubmit.sort()
        currentProducts.sort()

        if (hasSelectionChanged()) {

            viewModel.deleteCustomerProducts(partnerID, customerID, productsType).invokeOnCompletion {
                for (product in productsToSubmit) {

                    val newProductReference = viewModel.getCustomerProductsReference(partnerID, customerID, productsType).push()
                    val newProductKey = newProductReference.key!!
                    val newProduct = Product(customerID, newProductKey, product)

                    viewModel.insertProduct(partnerID, newProduct, productsType)
                }
            }
        }
    }

    private fun setupListeners() {
        products_empty_view.setOnClickListener { switchProductsView() }
        type_of_products_spinner.onItemSelectedListener = this@ProductsFragment
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.setProductsType(position)
    }

    private fun getAllPossibleProducts(): List<Array<String>> {

        val resources = requireActivity().resources

        // Array of string-arrays
        val typedArray = resources.obtainTypedArray(R.array.all_products)

        for (i in 0..typedArray.length()) {
            val currentArrayId = typedArray.getResourceId(i, 0)
            if (currentArrayId > 0) {
                val currentGroup = resources.getStringArray(currentArrayId)
                allProductsArray.add(currentGroup)
            }
        }

        typedArray.recycle()

        return allProductsArray
    }

    private fun generateRandomColor(): Int {

        val random = Random()

        // This is the base color which will be mixed with the generated one
        val baseColor = Color.WHITE

        val baseRed = Color.red(baseColor)
        val baseGreen = Color.green(baseColor)
        val baseBlue = Color.blue(baseColor)

        val red: Int = (baseRed + random.nextInt(256)) / 2
        val green: Int = (baseGreen + random.nextInt(256)) / 2
        val blue: Int = (baseBlue + random.nextInt(256)) / 2

        return Color.rgb(red, green, blue)
    }

    override fun onResume() {
        super.onResume()
        editingState = EDITING_STATE_DISABLED
        requireActivity().invalidateOptionsMenu()
        products_view_switcher.displayedChild = 0
    }
}
package pl.consultantassistant.ui.customer_details_activity.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.customer_details_editable_item_layout.*
import kotlinx.android.synthetic.main.fragment_details.*
import pl.consultantassistant.R
import pl.consultantassistant.data.models.CustomerDetails
import pl.consultantassistant.databinding.FragmentDetailsBinding
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModel

class DetailsFragment : Fragment() {

    private lateinit var viewModel: CustomerDetailsViewModel
    private lateinit var binding : FragmentDetailsBinding
    private var detailsEditingState: String = ""
    private var customerDetails: CustomerDetails? = null
    private lateinit var partnerID: String
    private lateinit var customerID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupListeners()

        viewModel = ViewModelProvider(requireActivity()).get(CustomerDetailsViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.getPartnerId().observe(viewLifecycleOwner, Observer { partnerID = it })
        viewModel.getCustomerId().observe(viewLifecycleOwner, Observer { customerID = it })
        viewModel.getCustomerDetails().observe(viewLifecycleOwner, Observer { details ->

            // Fill views with data
            details?.let {

                // Editing state is disabled by default
                detailsEditingState = EDITING_STATE_DISABLED

                // Set customer details on spinners
                hair_color_spinner.setSelection(resources.getStringArray(R.array.hair_colors).indexOf(it.hairColor))
                dyed_hair_spinner.setSelection(resources.getStringArray(R.array.yes_no_array).indexOf(it.dyedHair))
                type_of_hair_spinner.setSelection(resources.getStringArray(R.array.types_of_hair).indexOf(it.typeOfHair))
                washing_frequency_spinner.setSelection(resources.getStringArray(R.array.hair_washing_frequency).indexOf(it.washingFrequency))
                hair_condition_spinner.setSelection(resources.getStringArray(R.array.hair_condition).indexOf(it.hairCondition))
                hair_related_routine_spinner.setSelection(resources.getStringArray(R.array.hair_related_routine).indexOf(it.routineActivities))
                termo_protection_spinner.setSelection(resources.getStringArray(R.array.yes_no_array).indexOf(it.termoProtection))

                customerDetails = it.copy()

            } ?: run {

                // View is empty, editing is disabled here as well
                detailsEditingState = EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY
            }

            // Update menu
            requireActivity().invalidateOptionsMenu()
        })
    }

    private fun setupListeners() {
        details_empty_view.setOnClickListener { switchCustomerDetailsView() }
    }

    private fun switchCustomerDetailsView() {

        if (detailsEditingState == EDITING_STATE_DISABLED || detailsEditingState == EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY) {
            details_view_switcher.visibility = View.VISIBLE
            details_empty_view.visibility = View.GONE
            details_view_switcher.showNext()
            detailsEditingState = EDITING_STATE_ENABLED
        } else if (detailsEditingState == EDITING_STATE_ENABLED) {
            details_view_switcher.showPrevious()
            saveDetails()
            detailsEditingState = EDITING_STATE_DISABLED
        }

        requireActivity().invalidateOptionsMenu()
    }

    private fun saveDetails() {

        val hairColor = hair_color_spinner.selectedItem.toString()
        val hasDyedHair = dyed_hair_spinner.selectedItem.toString()
        val typeOfHair = type_of_hair_spinner.selectedItem.toString()
        val washingFrequency = washing_frequency_spinner.selectedItem.toString()
        val hairCondition = hair_condition_spinner.selectedItem.toString()
        val routineActivities = hair_related_routine_spinner.selectedItem.toString()
        val termoProtectionNeeded = termo_protection_spinner.selectedItem.toString()
        val purposeOfTreatment = treatment_details_edit_text.text.toString()
        val moreDetails = more_details_edit_text.text.toString()

        val updatedDetails = CustomerDetails(
            customerID,
            hairColor,
            hasDyedHair,
            typeOfHair,
            washingFrequency,
            hairCondition,
            routineActivities,
            termoProtectionNeeded,
            purposeOfTreatment,
            moreDetails
        )

        if(updatedDetails != customerDetails) {
            viewModel.insertCustomerDetails(partnerID, updatedDetails)
            Toasty.success(requireContext(), getString(R.string.toast_message_saved), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.details_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuButton = menu.findItem(R.id.details_action_edit)
        when (detailsEditingState) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.details_action_edit -> switchCustomerDetailsView()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        detailsEditingState = EDITING_STATE_DISABLED
        requireActivity().invalidateOptionsMenu()
        details_view_switcher.displayedChild = 0
    }

    companion object {
        const val EDITING_STATE_ENABLED = "ENABLED"
        const val EDITING_STATE_DISABLED = "DISABLED"
        const val EDITING_STATE_DISABLED_WHEN_VIEW_EMPTY = "DISABLED_WHEN_VIEW_EMPTY"
    }
}
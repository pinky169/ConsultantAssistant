package pl.consultantassistant.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.customer_item_layout.view.*
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Customer
import pl.consultantassistant.utils.CustomerItemListener
import java.util.*

class CustomersAdapter : ListAdapter<Customer, CustomersAdapter.ViewHolder>(diffCallback),
    Filterable {

    var itemListener: CustomerItemListener? = null
    var originalList: List<Customer>? = null
    var filteredList: List<Customer> = mutableListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemContext = itemView.context
        private val icon = itemView.icon
        private val fullName = itemView.full_name_text_view
        private val userLevel = itemView.user_level
        private val popupMenu = itemView.popup_menu
        private val userLevelArray = itemContext.resources.getStringArray(R.array.user_levels_array)

        fun bind(position: Int, customer: Customer) {
            fullName.text = itemContext.getString(R.string.full_name, customer.name, customer.surname)
            userLevel.text = customer.userLevel
            loadIcon(customer)

            itemView.setOnClickListener { itemListener?.onItemClicked(customer) }
            popupMenu.setOnClickListener {
                itemListener?.createPopupMenu(
                    popupMenu,
                    position,
                    customer
                )
            }
        }

        private fun loadIcon(customer: Customer) {
            when (customer.userLevel) {
                userLevelArray[0] -> { icon.setImageResource(R.drawable.ic_vip) }
                userLevelArray[1] -> { icon.setImageResource(R.drawable.ic_customer) }
                userLevelArray[2] -> { icon.setImageResource(R.drawable.ic_customer_interested) }
                userLevelArray[3] -> { icon.setImageResource(R.drawable.ic_partner) }
                userLevelArray[4] -> { icon.setImageResource(R.drawable.ic_interested_partner) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.customer_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(position, getItem(position))

    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {

                val locale = Locale.getDefault()
                val charString = charSequence.toString().toLowerCase(locale)

                if (charString.isEmpty()) {
                    filteredList = originalList!!
                } else {

                    originalList?.let { originalList ->

                        val filterResults: ArrayList<Customer> = arrayListOf()
                        for (customer in originalList) {

                            if (customer.name.toLowerCase(locale).contains(charString) ||
                                customer.surname.toLowerCase(locale).contains(charString) ||
                                "${customer.name.toLowerCase(locale)} ${customer.surname.toLowerCase(
                                    locale
                                )}".contains(charString) ||
                                customer.userLevel.toLowerCase(locale).contains(charString)
                            ) {
                                filterResults.add(customer)
                            }
                        }
                        filteredList = filterResults
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredList = filterResults.values as List<Customer>
                submitList(filteredList)
            }
        }
    }

    companion object {

        val diffCallback: DiffUtil.ItemCallback<Customer> =
            object : DiffUtil.ItemCallback<Customer>() {

                override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean {
                    return oldItem.customerID == newItem.customerID
                }

                override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean {
                    return oldItem.name == newItem.name &&
                            oldItem.surname == newItem.surname &&
                            oldItem.userLevel == newItem.userLevel
                }
            }
    }
}
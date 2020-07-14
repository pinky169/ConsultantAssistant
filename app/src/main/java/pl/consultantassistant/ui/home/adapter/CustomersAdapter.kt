package pl.consultantassistant.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pl.consultantassistant.data.models.Customer
import pl.consultantassistant.databinding.CustomerItemLayoutBinding
import pl.consultantassistant.utils.CustomerItemListener
import java.util.*

class CustomersAdapter(private val clickListener: CustomerItemListener) : ListAdapter<Customer, CustomersAdapter.ViewHolder>(diffCallback), Filterable {

    var originalList: List<Customer>? = null
    var filteredList: List<Customer> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(position, getItem(position), clickListener)

    class ViewHolder(val binding: CustomerItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, customer: Customer, itemListener: CustomerItemListener) {
            binding.position = position
            binding.customer = customer
            binding.clickListener = itemListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CustomerItemLayoutBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

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

                            val name = customer.name.toLowerCase(locale)
                            val surname = customer.surname.toLowerCase(locale)
                            val fullName = "$name $surname"
                            val userLevel = customer.userLevel.toLowerCase(locale)

                            if (name.contains(charString) ||
                                    surname.contains(charString) ||
                                    fullName.contains(charString) ||
                                    userLevel.contains(charString)
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
                        return oldItem == newItem
                    }
                }
    }
}
package pl.consultantassistant.ui.customer_details_activity.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pl.consultantassistant.data.models.Product
import pl.consultantassistant.databinding.ProductItemLayoutBinding

class ProductsAdapter : ListAdapter<Product, ProductsAdapter.ViewHolder>(diffCallback) {

    class ViewHolder(val binding: ProductItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.product = product
            binding.chip.isAllCaps = true
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ProductItemLayoutBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {

        val diffCallback: DiffUtil.ItemCallback<Product> =
                object : DiffUtil.ItemCallback<Product>() {

                    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
                        return oldItem.productID == newItem.productID
                    }

                    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
                        return oldItem == newItem
                    }
                }
    }
}
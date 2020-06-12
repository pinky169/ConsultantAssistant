package pl.consultantassistant.ui.customer_details_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_product_recycler_view_item.view.*
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Product

class ProductsAdapter: ListAdapter<Product, ProductsAdapter.ViewHolder>(diffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val chip = itemView.chip

        fun bind(product: Product) {
            chip.text = product.productName
            chip.isAllCaps = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_product_recycler_view_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {

        val diffCallback: DiffUtil.ItemCallback<Product> =
            object : DiffUtil.ItemCallback<Product>() {

                override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
                    return oldItem.productID == newItem.productID
                }

                override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
                    return oldItem.productName == newItem.productName
                }
            }
    }
}
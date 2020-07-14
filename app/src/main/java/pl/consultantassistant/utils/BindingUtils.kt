package pl.consultantassistant.utils

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.chip.Chip
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Customer
import pl.consultantassistant.data.models.Photo
import pl.consultantassistant.data.models.Product

@BindingAdapter("fullNameFormatted")
fun TextView.setFullNameFormatted(customer: Customer) {
    text = context.getString(R.string.full_name, customer.name, customer.surname)
}

@BindingAdapter("userLevel")
fun TextView.setUserLevel(customer: Customer) {
    text = customer.userLevel
}

@BindingAdapter("customerIcon")
fun ImageView.setCustomerIcon(customer: Customer) {
    val userLevelArray = resources.getStringArray(R.array.user_levels_array)
    setImageResource(
            when (customer.userLevel) {
                userLevelArray[0] -> R.drawable.ic_vip
                userLevelArray[1] -> R.drawable.ic_customer
                userLevelArray[2] -> R.drawable.ic_customer_interested
                userLevelArray[3] -> R.drawable.ic_partner
                userLevelArray[4] -> R.drawable.ic_interested_partner
                else -> R.drawable.ic_person
            })
}

@BindingAdapter("productName")
fun Chip.setProductName(product: Product) {
    text = product.productName
}

@BindingAdapter("photoResource", "progressBar", "popupMenu", requireAll = true)
fun ImageView.setPhotoResource(photo: Photo, progressBar: ProgressBar, popupMenu: ImageButton) {

    Glide.with(context)
            .load(Uri.parse(photo.photoURL))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    popupMenu.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    popupMenu.visibility = View.VISIBLE
                    return false
                }
            })
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .thumbnail(0.2f)
            .centerCrop()
            .into(this)
}
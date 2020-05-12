package pl.consultantassistant.ui.customer_details_activity.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.gallery_item_layout.view.*
import pl.consultantassistant.R
import pl.consultantassistant.utils.GalleryItemListener
import pl.mymonat.models.Photo

class PhotosAdapter : ListAdapter<Photo, PhotosAdapter.ViewHolder>(diffCallback) {

    var itemListener: GalleryItemListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemContext = itemView.context
        private val photoImageView = itemView.photo
        private val popupMenu = itemView.popup_menu

        fun bind(photo: Photo) {

            Glide.with(itemContext)
                .load(Uri.parse(photo.photoURL))
                .thumbnail(0.2f)
                .centerCrop()
                .into(photoImageView)

            itemView.setOnClickListener { itemListener?.onItemClicked(photo) }
            popupMenu.setOnClickListener { itemListener?.createPopupMenu(popupMenu, photo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {

        val diffCallback: DiffUtil.ItemCallback<Photo> = object : DiffUtil.ItemCallback<Photo>() {

                override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                    return oldItem.photoID == newItem.photoID
                }

                override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                    return oldItem.customerID == newItem.customerID &&
                            oldItem.photoURL == newItem.photoURL
                }
            }
    }
}
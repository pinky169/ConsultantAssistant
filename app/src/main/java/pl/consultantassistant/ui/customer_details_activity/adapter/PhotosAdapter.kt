package pl.consultantassistant.ui.customer_details_activity.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.gallery_item_layout.view.*
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Photo
import pl.consultantassistant.utils.GalleryItemListener

class PhotosAdapter : ListAdapter<Photo, PhotosAdapter.ViewHolder>(diffCallback) {

    var itemListener: GalleryItemListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val itemContext = itemView.context
        private val photoImageView = itemView.photo
        private val popupMenu = itemView.popup_menu
        private val progressBar = itemView.photo_progress_bar

        fun bind(photo: Photo) {

            Glide.with(itemContext)
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
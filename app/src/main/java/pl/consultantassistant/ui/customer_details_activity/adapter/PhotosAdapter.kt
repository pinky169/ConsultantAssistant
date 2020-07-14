package pl.consultantassistant.ui.customer_details_activity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pl.consultantassistant.data.models.Photo
import pl.consultantassistant.databinding.GalleryItemLayoutBinding
import pl.consultantassistant.utils.GalleryItemListener

class PhotosAdapter(private val photosClickListener: GalleryItemListener) : ListAdapter<Photo, PhotosAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), photosClickListener)

    class ViewHolder(val binding: GalleryItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo, photoClickListener: GalleryItemListener) {
            binding.photoProgressBar.visibility = View.VISIBLE
            binding.photo = photo
            binding.photoClickListener = photoClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GalleryItemLayoutBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    companion object {

        val diffCallback: DiffUtil.ItemCallback<Photo> = object : DiffUtil.ItemCallback<Photo>() {

            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem.photoID == newItem.photoID
            }

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
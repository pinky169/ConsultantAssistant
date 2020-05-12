package pl.consultantassistant.ui.customer_details_activity.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.MimeTypeMap
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*
import pl.consultantassistant.R
import pl.consultantassistant.ui.customer_details_activity.adapter.PhotosAdapter
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModel
import pl.consultantassistant.utils.GalleryItemListener
import pl.consultantassistant.utils.startFullScreenPhotoActivity
import pl.mymonat.models.Photo

class GalleryFragment : Fragment(), GalleryItemListener {

    // ViewModel
    private lateinit var viewModel: CustomerDetailsViewModel

    // RecyclerView Adapter
    private lateinit var galleryRecyclerViewAdapter: PhotosAdapter

    // Partner aka logged in user
    private lateinit var partnerID: String

    // ID of the selected customer
    private lateinit var customerID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupListeners()
        setupGalleryRecyclerView()

        viewModel = ViewModelProvider(requireActivity()).get(CustomerDetailsViewModel::class.java)
        viewModel.getPartnerId().observe(viewLifecycleOwner, Observer { partnerID = it })
        viewModel.getCustomerId().observe(viewLifecycleOwner, Observer { customerID = it })
        viewModel.getCustomerPhotos().observe(viewLifecycleOwner, Observer { photos ->
            showOrHidePhotos(photos)
            galleryRecyclerViewAdapter.submitList(photos)
        })
    }

    private fun setupGalleryRecyclerView() {

        galleryRecyclerViewAdapter = PhotosAdapter()
        galleryRecyclerViewAdapter.itemListener = this
        val recyclerLayoutManager = GridLayoutManager(requireContext(), 3)

        gallery_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = recyclerLayoutManager
            adapter = galleryRecyclerViewAdapter
        }
    }

    override fun createPopupMenu(view: View, photo: Photo) {

        //creating a popup menu
        val popup = PopupMenu(view.context, view)

        //inflating menu from xml resource
        popup.inflate(R.menu.gallery_recycler_view_item_menu)

        //adding click listener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.gallery_menu_action_delete -> {
                    viewModel.deleteCustomerPhoto(partnerID, photo)
                    viewModel.deletePhotoFromStorage(partnerID, photo)
                    true
                }
                else -> false
            }
        }

        //displaying the popup
        popup.show()
    }

    override fun onItemClicked(photo: Photo) {
        requireContext().startFullScreenPhotoActivity(photo.photoURL)
    }

    private fun openFileChooser() {
        val intent = Intent().also {
            it.type = "image/*"
            it.action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Wybierz zdjęcie"), PICK_IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri?): String? {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(requireActivity().contentResolver.getType(uri!!))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            gallery_progress_bar.visibility = View.VISIBLE

            val fileExtension = getFileExtension(data.data)!!

            val newPhotoReference = viewModel.getCustomerPhotosReference(partnerID, customerID).push()
            val newPhotoKey = newPhotoReference.key!!

            val imageReference = viewModel.getCustomerStorageReference(partnerID, customerID).child("$newPhotoKey.$fileExtension")

            imageReference
                .putFile(data.data!!)
                .addOnSuccessListener {
                    imageReference.downloadUrl.addOnSuccessListener {
                        val imgURL = it.toString()
                        val newPhoto = Photo(customerID, newPhotoKey, imgURL, fileExtension)
                        viewModel.insertPhoto(partnerID, newPhoto)
                        gallery_progress_bar.visibility = View.GONE
                    }
                }.addOnCanceledListener {
                    gallery_progress_bar.visibility = View.GONE
                }
        }
    }

    private fun setupListeners() {
        gallery_empty_view.setOnClickListener { openFileChooser() }
    }

    private fun showOrHidePhotos(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            gallery_recycler_view.visibility = View.VISIBLE
            gallery_empty_view.visibility = View.GONE
        } else {
            gallery_recycler_view.visibility = View.GONE
            gallery_empty_view.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.gallery_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.gallery_action_add_new_photo -> openFileChooser()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1337
    }
}
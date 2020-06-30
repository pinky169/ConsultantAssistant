package pl.consultantassistant.ui.customer_details_activity.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_gallery.*
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Photo
import pl.consultantassistant.databinding.FragmentGalleryBinding
import pl.consultantassistant.ui.customer_details_activity.adapter.PhotosAdapter
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModel
import pl.consultantassistant.utils.GalleryItemListener
import pl.consultantassistant.utils.getFileExtension
import pl.consultantassistant.utils.getLastModifiedDate
import pl.consultantassistant.utils.startFullScreenPhotoActivity


class GalleryFragment : Fragment(), GalleryItemListener {

    // ViewModel
    private lateinit var viewModel: CustomerDetailsViewModel

    // Data binding for this fragment
    private lateinit var binding : FragmentGalleryBinding

    // RecyclerView Adapter
    private lateinit var galleryRecyclerViewAdapter: PhotosAdapter

    // Partner aka logged in user
    private lateinit var partnerID: String

    // ID of the selected customer
    private lateinit var customerID: String

    // gallery state
    private var isGalleryEmpty: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupListeners()
        setupGalleryRecyclerView()

        viewModel = ViewModelProvider(requireActivity()).get(CustomerDetailsViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.getPartnerId().observe(viewLifecycleOwner, Observer { partnerID = it })
        viewModel.getCustomerId().observe(viewLifecycleOwner, Observer { customerID = it })
        viewModel.getCustomerPhotos().observe(viewLifecycleOwner, Observer { photos ->
            setEmptyState(photos)
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
        requireContext().startFullScreenPhotoActivity(photo)
    }

    private fun openFileChooser() {

        if (checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE)

        } else {

            val intent = Intent().also {
                it.type = "image/*"
                it.action = Intent.ACTION_OPEN_DOCUMENT
                it.addCategory(Intent.CATEGORY_OPENABLE)
                it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_image_chooser_title)), PICK_IMAGE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            val intent = Intent().also {
                it.type = "image/*"
                it.action = Intent.ACTION_OPEN_DOCUMENT
                it.addCategory(Intent.CATEGORY_OPENABLE)
                it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }

            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_image_chooser_title)), PICK_IMAGE_REQUEST)
        }
        else {
            Toasty.warning(requireContext(), getString(R.string.permission_request_denied_text), Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadPhotoAndStoreInDatabase(imageUri: Uri) {

        val fileExtension: String = getFileExtension(requireActivity(), imageUri)!!
        val lastModifiedDate: String = getLastModifiedDate(requireActivity(), imageUri)

        val newPhotoReference = viewModel.getCustomerPhotosReference(partnerID, customerID).push()
        val newPhotoKey = newPhotoReference.key!!

        val imageReference = viewModel.getCustomerStorageReference(partnerID, customerID)
            .child("$newPhotoKey.$fileExtension")

        imageReference
            .putFile(imageUri)
            .addOnSuccessListener {

                imageReference.downloadUrl.addOnSuccessListener {
                    val imgURL = it.toString()
                    val newPhoto = Photo(customerID, newPhotoKey, imgURL, fileExtension, lastModifiedDate)
                    viewModel.insertPhoto(partnerID, newPhoto)
                    gallery_progress_bar?.visibility = View.GONE
                }

                context?.let {
                    Toasty.success(it, getString(R.string.image_upload_success_toast_message), Toast.LENGTH_SHORT).show()
                }

            }
            .addOnCanceledListener {
                gallery_progress_bar?.visibility = View.GONE
                context?.let {
                    Toasty.error(it, getString(R.string.image_upload_error_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {

            gallery_progress_bar?.visibility = View.VISIBLE

            // Handle Multiple images
            if (data.clipData != null) {

                val numberOfSelectedPhotos = data.clipData!!.itemCount

                for (i in 0 until numberOfSelectedPhotos) {
                    uploadPhotoAndStoreInDatabase(data.clipData!!.getItemAt(i).uri)
                }
            }
            // Handle single image
            else if (data.data != null) {
                uploadPhotoAndStoreInDatabase(data.data!!)
            }
        }
    }

    private fun setupListeners() {
        gallery_empty_view.setOnClickListener { openFileChooser() }
    }

    private fun setEmptyState(photos: List<Photo>) {

        isGalleryEmpty = photos.isEmpty()

        // Refresh menu depending on whether the list is empty or not
        requireActivity().invalidateOptionsMenu()
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val menuButton = menu.findItem(R.id.gallery_action_add_new_photo)
        when (isGalleryEmpty) {
            true -> {
                menuButton.isVisible = false
            }
            false -> {
                menuButton.isVisible = true
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1337
        private const val PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 3117
    }
}
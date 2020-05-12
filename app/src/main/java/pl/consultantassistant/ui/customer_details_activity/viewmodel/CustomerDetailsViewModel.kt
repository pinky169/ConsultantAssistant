package pl.consultantassistant.ui.customer_details_activity.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.consultantassistant.data.firebase.FirebaseQueryLiveData
import pl.consultantassistant.data.repository.Repository
import pl.mymonat.models.CustomerDetails
import pl.mymonat.models.Photo
import pl.mymonat.models.Product


class CustomerDetailsViewModel(application: Application, private val repository: Repository) : AndroidViewModel(application) {

    private val partnerID = MutableLiveData<String>()
    private val customerID = MutableLiveData<String>()
    private val detailsLiveData: LiveData<CustomerDetails>
    private val detailsDataSnapshotLiveData: LiveData<DataSnapshot>
    private val productsLiveData: LiveData<List<Product>>
    private val productsDataSnapshotLiveData: LiveData<DataSnapshot>
    private val photosLiveData: LiveData<List<Photo>>
    private val photosDataSnapshotLiveData: LiveData<DataSnapshot>

    init {

        detailsDataSnapshotLiveData = Transformations.switchMap(CustomerMediatorLiveData(partnerID, customerID)) {
            FirebaseQueryLiveData(repository.getCustomerDetailsReference(it.first!!, it.second!!))
        }

        detailsLiveData = Transformations.map(detailsDataSnapshotLiveData) { DetailsDeserializer().apply(it) }

        productsDataSnapshotLiveData =  Transformations.switchMap(CustomerMediatorLiveData(partnerID, customerID)) {
            FirebaseQueryLiveData(repository.getCustomerProductsReference(it.first!!, it.second!!))
        }

        productsLiveData = Transformations.map(productsDataSnapshotLiveData) { ProductsDeserializer().apply(it) }

        photosDataSnapshotLiveData =  Transformations.switchMap(CustomerMediatorLiveData(partnerID, customerID)) {
            FirebaseQueryLiveData(repository.getCustomerPhotosReference(it.first!!, it.second!!))
        }

        photosLiveData = Transformations.map(photosDataSnapshotLiveData) { PhotosDeserializer().apply(it) }
    }

    fun setPartnerId(partnerId: String) {
        partnerID.value = partnerId
    }

    fun setCustomerId(customerId: String) {
        customerID.value = customerId
    }

    fun getPartnerId(): LiveData<String> {
        return partnerID
    }

    fun getCustomerId(): LiveData<String> {
        return customerID
    }


    /* **************************
    *       Customer details
    * **************************/

    fun getCustomerDetails(): LiveData<CustomerDetails> {
        return detailsLiveData
    }

    fun insertCustomerDetails(partnerID: String, customerDetails: CustomerDetails) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertCustomerDetails(partnerID, customerDetails)
    }

    fun updateCustomerDetails(partnerID: String, customerDetails: CustomerDetails) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCustomerDetails(partnerID, customerDetails)
    }

    fun deleteCustomerDetails(partnerID: String, customerID: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCustomerDetails(partnerID, customerID)
    }


    /* **************************
    *       Customer products
    * **************************/

    fun getCustomerProducts(): LiveData<List<Product>> {
        return productsLiveData
    }

    fun getCustomerProductsReference(uid: String, customerID: String) = repository.getCustomerProductsReference(uid, customerID)

    fun insertProduct(partnerID: String, product: Product) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertProduct(partnerID, product)
    }

    fun deleteCustomerProduct(partnerID: String, product: Product) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCustomerProduct(partnerID, product)
    }

    fun deleteAllCustomerProducts(partnerID: String, customerID: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllCustomerProducts(partnerID, customerID)
    }


    /* **************************
    *       Customer photos
    * **************************/

    fun getCustomerPhotos(): LiveData<List<Photo>> {
        return photosLiveData
    }

    fun getCustomerPhotosReference(uid: String, customerID: String) = repository.getCustomerPhotosReference(uid, customerID)

    fun insertPhoto(partnerID: String, photo: Photo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertPhoto(partnerID, photo)
    }

    fun deleteCustomerPhoto(partnerID: String, photo: Photo) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCustomerPhoto(partnerID, photo)
    }

    fun deleteAllCustomerPhotos(partnerID: String, customerID: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllCustomerPhotos(partnerID, customerID)
    }


    /* **************************
    *      Customer storage
    * **************************/

    fun getCustomerStorageReference(uid: String, customerID: String) = repository.getCustomerStorageReference(uid, customerID)

    fun deletePhotoFromStorage(uid: String, photo: Photo) = repository.deletePhotoFromStorage(uid, photo)

    /* **************************
    *      Helper classes
    * **************************/

    inner class CustomerMediatorLiveData<String>(partnerID: LiveData<String>, customerID: LiveData<String>): MediatorLiveData<Pair<String?, String?>>() {

        init {
            addSource(partnerID) {
                value = it to customerID.value
            }

            addSource(customerID) {
                value = partnerID.value to it
            }
        }
    }

    inner class DetailsDeserializer : Function<DataSnapshot> {
        fun apply(dataSnapshot: DataSnapshot): CustomerDetails? {
            return dataSnapshot.getValue(CustomerDetails::class.java)
        }
    }

    inner class ProductsDeserializer : Function<DataSnapshot> {
        fun apply(dataSnapshot: DataSnapshot): List<Product> {
            val productsList = arrayListOf<Product>()
            for (child in dataSnapshot.children) {
                val currentProduct = child.getValue(Product::class.java)!!
                productsList.add(currentProduct)
            }
            return productsList
        }
    }

    inner class PhotosDeserializer : Function<DataSnapshot> {
        fun apply(dataSnapshot: DataSnapshot): List<Photo> {
            val photosList = arrayListOf<Photo>()
            for (child in dataSnapshot.children) {
                val currentPhoto = child.getValue(Photo::class.java)!!
                photosList.add(currentPhoto)
            }
            return photosList
        }
    }
}
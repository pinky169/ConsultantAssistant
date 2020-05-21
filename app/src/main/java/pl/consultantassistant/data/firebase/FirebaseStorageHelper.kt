package pl.consultantassistant.data.firebase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import pl.consultantassistant.data.models.Photo

class FirebaseStorageHelper {

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val storageReference: StorageReference by lazy {
        storage.getReference("uploads")
    }

    fun getCustomerStorageReference(uid: String, customerID: String): StorageReference {
        return storageReference.child(uid).child(customerID)
    }

    fun deletePhotoFromStorage(uid: String, photo: Photo) {
        getCustomerStorageReference(uid, photo.customerID).child("${photo.photoID}.${photo.photoExtension}").delete()
    }
}
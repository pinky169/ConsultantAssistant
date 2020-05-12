package pl.consultantassistant.data.repository

import pl.consultantassistant.data.firebase.FirebaseAuthHelper
import pl.consultantassistant.data.firebase.FirebaseDatabaseHelper
import pl.consultantassistant.data.firebase.FirebaseStorageHelper
import pl.consultantassistant.data.models.Partner
import pl.mymonat.models.Customer
import pl.mymonat.models.CustomerDetails
import pl.mymonat.models.Photo
import pl.mymonat.models.Product

class Repository(private val firebase: FirebaseAuthHelper, private val firebaseDB: FirebaseDatabaseHelper, private val storage: FirebaseStorageHelper) {


    /* **************************
     *       Authentication
     * **************************/

    fun currentUser() = firebase.currentUser()

    fun currentUserId() = firebase.currentUserId()

    fun register(email: String, password: String) = firebase.register(email, password)

    fun login(email: String, password: String) = firebase.login(email, password)

    fun logout() = firebase.logout()

    fun verifyEmail() = firebase.verifyEmail()


    /* **************************
     *          Partner
     * **************************/

    fun getPartnerReference(uid: String) = firebaseDB.getPartnerReference(uid)

    fun insertPartner(uid: String, partner: Partner) = firebaseDB.insertPartner(uid, partner)

    fun updatePartner(uid: String, partner: Partner) = firebaseDB.updatePartner(uid, partner)


    /* **************************
     *          Customer
     * **************************/

    fun getSpecificPartnerCustomersReference(uid: String) = firebaseDB.getSpecificPartnerCustomersReference(uid)

    fun insertCustomer(uid: String, customerID: String, customer: Customer) = firebaseDB.insertCustomer(uid, customerID, customer)

    fun deleteCustomer(uid: String, customerID: String) = firebaseDB.deleteCustomer(uid, customerID)

    fun updateCustomer(uid: String, customer: Customer) = firebaseDB.updateCustomer(uid, customer)


    /* **************************
    *       Customer details
    * **************************/

    fun getCustomerDetailsReference(uid: String, customerID: String) = firebaseDB.getSpecificCustomerDetailsReference(uid, customerID)

    fun insertCustomerDetails(uid: String, customerDetails: CustomerDetails) = firebaseDB.insertCustomerDetails(uid, customerDetails)

    fun updateCustomerDetails(uid: String, customerDetails: CustomerDetails) = firebaseDB.updateCustomerDetails(uid, customerDetails)

    fun deleteCustomerDetails(uid: String, customerID: String) = firebaseDB.deleteCustomerDetails(uid, customerID)


    /* **************************
    *       Customer products
    * **************************/

    fun getCustomerProductsReference(uid: String, customerID: String) = firebaseDB.getSpecificCustomerProductsReference(uid, customerID)

    fun insertProduct(uid: String, product: Product) = firebaseDB.insertCustomerProduct(uid, product)

    fun deleteCustomerProduct(uid: String, product: Product) = firebaseDB.deleteCustomerProduct(uid, product)

    fun deleteAllCustomerProducts(uid: String, customerID: String) = firebaseDB.deleteAllCustomerProducts(uid, customerID)


    /* **************************
    *       Customer photos
    * **************************/

    fun getCustomerPhotosReference(uid: String, customerID: String) = firebaseDB.getSpecificCustomerPhotosReference(uid, customerID)

    fun insertPhoto(uid: String, photo: Photo) = firebaseDB.insertPhoto(uid, photo)

    fun deleteCustomerPhoto(uid: String, photo: Photo) = firebaseDB.deleteCustomerPhoto(uid, photo)

    fun deleteAllCustomerPhotos(uid: String, customerID: String) = firebaseDB.deleteAllCustomerPhotos(uid, customerID)

    /* **************************
    *       Customer storage
    * **************************/

    fun getCustomerStorageReference(uid: String, customerID: String) = storage.getCustomerStorageReference(uid, customerID)

    fun deletePhotoFromStorage(uid: String, photo: Photo) = storage.deletePhotoFromStorage(uid, photo)


    /* **********************************
    *  Delete all customer data at once *
    * ***********************************/

    fun deleteAllCustomerData(uid: String, customerID: String) {
        deleteAllCustomerPhotos(uid, customerID)
        deleteAllCustomerProducts(uid, customerID)
        deleteCustomerDetails(uid, customerID)
        deleteCustomer(uid, customerID)
        // Firebase storage doesn't allow deleting "folders"
        // that means you have to iterate through the "folder"
        // and delete each file one by one
    }


    /* **************************
     *          Tokens
     * **************************/

    fun insertToken(uid: String, token: String) = firebaseDB.insertToken(uid, token)
}
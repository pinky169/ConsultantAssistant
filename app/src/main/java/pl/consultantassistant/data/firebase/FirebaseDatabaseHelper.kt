package pl.consultantassistant.data.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import pl.consultantassistant.data.models.Partner
import pl.mymonat.models.Customer
import pl.mymonat.models.CustomerDetails
import pl.mymonat.models.Photo
import pl.mymonat.models.Product

class FirebaseDatabaseHelper {


    /* ***************************************************
    *                   Firebase Database                *
    **************************************************** */

    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }


    /* ***************************************************
    *                   Database users                   *
    **************************************************** */

    private val partnersReference: DatabaseReference by lazy {
        database.getReference("partners")
    }

    fun getPartnerReference(uid: String): DatabaseReference {
        return partnersReference.child(uid)
    }

    fun insertPartner(uid: String, partner: Partner) = getPartnerReference(uid).setValue(partner)

    fun updatePartner(uid: String, partner: Partner) = getPartnerReference(uid).updateChildren(partner.toMap())


    /* ***************************************************
    *                   Database customers               *
    **************************************************** */

    private val customersReference: DatabaseReference by lazy {
        database.getReference("customers")
    }

    fun getSpecificPartnerCustomersReference(uid: String): DatabaseReference {
        return customersReference.child(uid)
    }

    fun insertCustomer(uid: String, customerID: String, customer: Customer) = getSpecificPartnerCustomersReference(uid).child(customerID).setValue(customer)

    fun deleteCustomer(uid: String, customerID: String) = getSpecificPartnerCustomersReference(uid).child(customerID).removeValue()

    fun updateCustomer(uid: String, customer: Customer) = getSpecificPartnerCustomersReference(uid).child(customer.customerID).updateChildren(customer.toMap())


    /* ***************************************************
    *            Database customer details               *
    **************************************************** */

    private val customersDetailsReference: DatabaseReference by lazy {
        database.getReference("details")
    }

    fun getSpecificCustomerDetailsReference(uid: String, customerID: String): DatabaseReference {
        return customersDetailsReference.child(uid).child(customerID)
    }

    fun insertCustomerDetails(uid: String, customerDetails: CustomerDetails) = getSpecificCustomerDetailsReference(uid, customerDetails.customerID).setValue(customerDetails)

    fun updateCustomerDetails(uid: String, customerDetails: CustomerDetails) = getSpecificCustomerDetailsReference(uid, customerDetails.customerID).updateChildren(customerDetails.toMap())

    fun deleteCustomerDetails(uid: String, customerID: String) = getSpecificCustomerDetailsReference(uid, customerID).removeValue()


    /* ***************************************************
    *            Database customer products               *
    **************************************************** */

    private val customersProductsReference: DatabaseReference by lazy {
        database.getReference("products")
    }

    fun getSpecificCustomerProductsReference(uid: String, customerID: String): DatabaseReference {
        return customersProductsReference.child(uid).child(customerID)
    }

    fun insertCustomerProduct(uid: String, product: Product) = getSpecificCustomerProductsReference(uid, product.customerID).child(product.productID).setValue(product)

    fun updateCustomerProduct(uid: String, product: Product) = getSpecificCustomerProductsReference(uid, product.customerID).child(product.productID).updateChildren(product.toMap())

    fun deleteCustomerProduct(uid: String, product: Product) = getSpecificCustomerProductsReference(uid, product.customerID).child(product.productID).removeValue()

    fun deleteAllCustomerProducts(uid: String, customerID: String) = getSpecificCustomerProductsReference(uid, customerID).removeValue()


    /* ***************************************************
    *            Database customer photos               *
    **************************************************** */

    private val customerPhotosReference: DatabaseReference by lazy {
        database.getReference("uploads")
    }

    fun getSpecificCustomerPhotosReference(uid: String, customerID: String): DatabaseReference {
        return customerPhotosReference.child(uid).child(customerID)
    }

    fun insertPhoto(uid: String, photo: Photo) = getSpecificCustomerPhotosReference(uid, photo.customerID).child(photo.photoID).setValue(photo)

    fun deleteCustomerPhoto(uid: String, photo: Photo) = getSpecificCustomerPhotosReference(uid, photo.customerID).child(photo.photoID).removeValue()

    fun deleteAllCustomerPhotos(uid: String, customerID: String) = getSpecificCustomerPhotosReference(uid, customerID).removeValue()


    /* ***************************************************
    *                   Database tokens                  *
    **************************************************** */

    private val tokensReference: DatabaseReference by lazy {
        database.getReference("tokens")
    }

    fun insertToken(uid: String, token: String) = tokensReference.child(uid).child("deviceToken").setValue(token)
}
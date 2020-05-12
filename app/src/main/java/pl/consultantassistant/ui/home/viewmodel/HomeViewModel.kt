package pl.consultantassistant.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.consultantassistant.data.firebase.FirebaseQueryLiveData
import pl.consultantassistant.data.models.Partner
import pl.consultantassistant.data.repository.Repository
import pl.mymonat.models.Customer

class HomeViewModel(application: Application, private val repository: Repository) : AndroidViewModel(application) {

    val partnerID by lazy { repository.currentUserId() }
    private val searchQuery = MutableLiveData("")
    private val partnerLiveData: LiveData<Partner>
    private val customersLiveData: LiveData<List<Customer>>

    private val partnerDataSnapshotLiveData: LiveData<DataSnapshot> by lazy {
        FirebaseQueryLiveData(repository.getPartnerReference(partnerID!!))
    }

    private val customersDataSnapshotLiveData: LiveData<DataSnapshot> by lazy {
        FirebaseQueryLiveData(repository.getSpecificPartnerCustomersReference(partnerID!!))
    }

    init {
        partnerLiveData = Transformations.map(partnerDataSnapshotLiveData) { PartnerDeserializer().apply(it) }
        customersLiveData = Transformations.map(customersDataSnapshotLiveData) { CustomersDeserializer().apply(it) }
    }

    /**
     * Partner
     */

    fun getPartner() : LiveData<Partner> {
        return partnerLiveData
    }

    fun getPartnerReference(): DatabaseReference {
        return repository.getPartnerReference(partnerID!!)
    }

    /**
     * Customers
     */

    fun getCustomers(): LiveData<List<Customer>> {
        return customersLiveData
    }

    fun getSpecificPartnerCustomersReference(uid: String) = repository.getSpecificPartnerCustomersReference(uid)

    fun insertCustomer(uid: String, customerID: String, customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertCustomer(uid, customerID, customer)
    }

    fun updateCustomer(uid: String, customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCustomer(uid, customer)
    }

    fun deleteCustomer(uid: String, customerID: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllCustomerData(uid, customerID)
    }

    fun search(query: String) {
        searchQuery.value = query
    }

    /**
     * Use to insert user's device token into database.
     */
    fun insertToken(uid: String, token: String) = repository.insertToken(uid, token)

    /**
     * Log out
     */
    fun logout() = repository.logout()

    inner class PartnerDeserializer : Function<DataSnapshot> {
        fun apply(dataSnapshot: DataSnapshot): Partner? {
            return dataSnapshot.getValue(Partner::class.java)
        }
    }

    inner class CustomersDeserializer : Function<DataSnapshot> {
        fun apply(dataSnapshot: DataSnapshot): List<Customer> {
            val productsList = arrayListOf<Customer>()
            for (child in dataSnapshot.children) {
                val currentCustomer = child.getValue(Customer::class.java)!!
                productsList.add(currentCustomer)
            }
            return productsList
        }
    }
}
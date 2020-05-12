package pl.consultantassistant.ui.customer_details_activity.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.consultantassistant.data.repository.Repository

@Suppress("UNCHECKED_CAST")
class CustomerDetailsViewModelFactory(val application: Application, val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CustomerDetailsViewModel(application, repository) as T
    }

}
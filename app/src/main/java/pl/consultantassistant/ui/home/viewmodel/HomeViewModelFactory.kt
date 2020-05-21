package pl.consultantassistant.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.consultantassistant.data.repository.Repository

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val application: Application,
    private val repository: Repository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(application, repository) as T
    }

}
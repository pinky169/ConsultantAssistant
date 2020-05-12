package pl.consultantassistant

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import pl.consultantassistant.data.firebase.FirebaseAuthHelper
import pl.consultantassistant.data.firebase.FirebaseDatabaseHelper
import pl.consultantassistant.data.firebase.FirebaseStorageHelper
import pl.consultantassistant.data.repository.Repository
import pl.consultantassistant.ui.home.viewmodel.HomeViewModelFactory
import pl.consultantassistant.ui.auth.AuthViewModelFactory
import pl.consultantassistant.ui.customer_details_activity.viewmodel.CustomerDetailsViewModelFactory

class ConsultantAssistantApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {

        import(androidXModule(this@ConsultantAssistantApplication))

        FirebaseDatabaseHelper().database.setPersistenceEnabled(true)

        bind() from singleton { FirebaseAuthHelper(instance()) }
        bind() from singleton { FirebaseDatabaseHelper() }
        bind() from singleton { FirebaseStorageHelper() }
        bind() from singleton { Repository(instance(), instance(), instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { HomeViewModelFactory(instance(), instance()) }
        bind() from provider { CustomerDetailsViewModelFactory(instance(), instance()) }
    }
}
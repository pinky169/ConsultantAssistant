package pl.consultantassistant.utils

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import es.dmoral.toasty.Toasty
import pl.consultantassistant.R
import pl.consultantassistant.data.models.Customer
import pl.consultantassistant.data.models.Photo
import pl.consultantassistant.ui.auth.LoginActivity
import pl.consultantassistant.ui.auth.SignUpActivity
import pl.consultantassistant.ui.full_screen_photo_activity.FullScreenPhotoActivity
import pl.consultantassistant.ui.home.HomeActivity

fun Context.startHomeActivity() =
    Intent(this, HomeActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        Toasty.success(this, getString(R.string.welcome_message), Toast.LENGTH_LONG, false).show()
    }

fun Context.startLoginActivity() =
    Intent(this, LoginActivity::class.java).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }

fun Context.startSignUpActivity() =
    Intent(this, SignUpActivity::class.java).also {
        startActivity(it)
    }

fun Context.startFullScreenPhotoActivity(photo: Photo) =
    Intent(this, FullScreenPhotoActivity::class.java).also {
        it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        it.putExtra("photoURL", photo.photoURL)
        it.putExtra("photoLastModified", photo.photoLastModifiedDate)
        startActivity(it)
    }

interface CustomerItemListener {
    fun createPopupMenu(view: View, position: Int, customer: Customer)
    fun onItemClicked(customer: Customer)
}

interface GalleryItemListener {
    fun createPopupMenu(view: View, photo: Photo)
    fun onItemClicked(photo: Photo)
}

interface LoadingListener {
    fun onStarted()
    fun onSuccess()
    fun onCanceled()
}
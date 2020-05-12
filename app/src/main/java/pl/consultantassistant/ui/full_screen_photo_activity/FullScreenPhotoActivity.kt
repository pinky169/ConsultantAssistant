package pl.mymonat.activities.full_screen_photo_activity

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.full_screen_photo_layout.*
import pl.consultantassistant.R

class FullScreenPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_screen_photo_layout)

        loadFullScreenPhoto()
    }

    private fun loadFullScreenPhoto() {

        val photoURL = intent.getStringExtra("photoURL")
        val photoURI = Uri.parse(photoURL)

        Glide.with(this)
            .load(photoURI)
            .into(full_screen_photo_image_view)
    }
}
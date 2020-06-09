package pl.consultantassistant.ui.full_screen_photo_activity

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.full_screen_photo_layout.*
import pl.consultantassistant.R
import java.text.SimpleDateFormat
import java.util.*

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

        val photoLastModifiedDate = intent.getStringExtra("photoLastModified")

        val parseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'", Locale.getDefault())
        val date: Date = parseFormat.parse(photoLastModifiedDate)

        val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val dateString: String = displayFormat.format(date)
        last_modified_date.text = dateString
    }
}
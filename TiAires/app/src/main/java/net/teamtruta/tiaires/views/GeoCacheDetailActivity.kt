package net.teamtruta.tiaires.views

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.microsoft.appcenter.analytics.Analytics
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheInTour
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModel
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModelFactory
import org.honorato.multistatetogglebutton.MultiStateToggleButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GeoCacheDetailActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    private var currentGeoCacheInTour: GeoCacheInTour? = null
    private var soundPool: SoundPool? = null
    private var soundID = 0
    private var currentPhotoPath: String = ""
    private var TAG: String = GeoCacheDetailActivity::class.java.simpleName

    private val viewModel: GeoCacheDetailViewModel by viewModels{
        GeoCacheDetailViewModelFactory((application as App).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_cache_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_geo_cache_detail)
        setSupportActionBar(toolbar)
        val geoCacheID = intent.getLongExtra(App.GEOCACHE_ID_EXTRA, -1L)

        // Setup Action Bar
        val ab = supportActionBar!!
        ab.setDisplayHomeAsUpEnabled(true)

        // Observe geocache that was clicked on
        viewModel.getGeoCacheInTourFromID(geoCacheID).observe(this,
                { geoCacheInTour: GeoCacheInTourWithDetails -> setupCacheSpecificData(geoCacheInTour) })


        //  Setup ping sound
        setupAudio()
    }

    private fun setupCacheSpecificData(geoCacheInTour: GeoCacheInTourWithDetails) {
        currentGeoCacheInTour = geoCacheInTour.geoCacheInTour
        val currentGeoCache = geoCacheInTour.geoCache.geoCache

        // Set GeoCache Title
        val ab = supportActionBar
        ab!!.title = currentGeoCache.name


        // Set Not Found / Found / DNF toggle and appropriate onClickListener
        val geoCacheVisitButton = findViewById<MultiStateToggleButton>(R.id.geo_cache_visit_button)
        var buttonStates = booleanArrayOf(true, false, false)
        if (currentGeoCacheInTour!!.currentVisitOutcome === VisitOutcomeEnum.Found) buttonStates = booleanArrayOf(false, true, false) else if (currentGeoCacheInTour!!.currentVisitOutcome === VisitOutcomeEnum.DNF) buttonStates = booleanArrayOf(false, false, true)
        geoCacheVisitButton.states = buttonStates

        geoCacheVisitButton.setOnValueChangedListener { position: Int ->
            when(position){
                0 -> viewModel.setGeoCacheInTourVisit(currentGeoCacheInTour!!, VisitOutcomeEnum.NotAttempted)
                1 -> {
                    viewModel.setGeoCacheInTourVisit(currentGeoCacheInTour!!, VisitOutcomeEnum.Found)
                    playPing()}
                2-> {viewModel.setGeoCacheInTourVisit(currentGeoCacheInTour!!, VisitOutcomeEnum.DNF)
                    playPing()}
            }
        }

        // Set Checkboxes
        // 1. Needs Maintenance Checkbox
        val needsMaintenanceCheckBox = findViewById<CheckBox>(R.id.needsMaintenanceCheckBox)
        needsMaintenanceCheckBox.isChecked = currentGeoCacheInTour!!.needsMaintenance

        // 2. FoundTrackable Checkbox and EditText
        val foundTrackableCheckBox = findViewById<CheckBox>(R.id.foundTrackableCheckBox)
        if (currentGeoCacheInTour!!.foundTrackable != null) {
            foundTrackableCheckBox.isChecked = true
            val foundTrackableEditText = findViewById<EditText>(R.id.foundTrackableEditText)
            foundTrackableEditText.setText(currentGeoCacheInTour!!.foundTrackable)
        }

        // 3. DroppedTrackable Checkbox and EditText
        val droppedTrackableCheckBox = findViewById<CheckBox>(R.id.droppedTrackableCheckBox)
        if (currentGeoCacheInTour!!.droppedTrackable != null) {
            droppedTrackableCheckBox.isChecked = true
            val droppedTrackableEditText = findViewById<EditText>(R.id.droppedTrackableEditText)
            droppedTrackableEditText.setText(currentGeoCacheInTour!!.droppedTrackable)
        }

        // 4. Favourite Point Checkbox
        val favouritePointCheckBox = findViewById<CheckBox>(R.id.favouritePointCheckBox)
        if (currentGeoCacheInTour!!.favouritePoint) favouritePointCheckBox.isChecked = true

        // Set my notes
        val notesSection = findViewById<EditText>(R.id.notes)
        val myNotes = currentGeoCacheInTour!!.notes
        if (myNotes != "") notesSection.setText(myNotes)

        // Setup photo
        val photoCheckBox = findViewById<CheckBox>(R.id.photo_checkbox)
        photoCheckBox.isChecked = currentGeoCacheInTour!!.pathToImage != null
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, TourActivity::class.java)

        setResult(RESULT_OK, intent)
        finish()
        return true
    }

    override fun onBackPressed() {
        saveChanges()
        onSupportNavigateUp()
    }


    /**
     * Handle Saving in the geoCache in tour activity
     */
    private fun saveChanges() {
        Analytics.trackEvent("GeoCacheDetailActivity.saveChanges")

        // save changes
        val needsMaintenanceCheckBox = findViewById<CheckBox>(R.id.needsMaintenanceCheckBox)
        val favouritePointCheckBox = findViewById<CheckBox>(R.id.favouritePointCheckBox)
        val foundTrackableEditText = findViewById<EditText>(R.id.foundTrackableEditText)
        val droppedTrackableEditText = findViewById<EditText>(R.id.droppedTrackableEditText)
        val notesView = findViewById<EditText>(R.id.notes)

        viewModel.updateGeocaCheInTourDetails(currentGeoCacheInTour!!,
                needsMaintenanceCheckBox.isChecked,
                favouritePointCheckBox.isChecked,
                foundTrackableEditText.text.toString(),
                droppedTrackableEditText.text.toString(),
                notesView.text.toString())
    }


    fun onFoundTrackableCheckboxClicked(view: View?) {
        val foundTrackableCheckBox = findViewById<CheckBox>(R.id.foundTrackableCheckBox)
        val foundTrackableEditText = findViewById<EditText>(R.id.foundTrackableEditText)
        if (foundTrackableCheckBox.isChecked) {
            // Focus attention on text box to fill in value
            foundTrackableEditText.requestFocus()
        } else {
            // Delete inputted trackable code in editText area
            foundTrackableEditText.setText("")
        }
    }

    fun onDroppedTrackableCheckboxClicked(view: View?) {
        val droppedTrackableCheckBox = findViewById<CheckBox>(R.id.droppedTrackableCheckBox)
        val droppedTrackableEditText = findViewById<EditText>(R.id.droppedTrackableEditText)
        if (droppedTrackableCheckBox.isChecked) {
            // Focus attention on text box to fill in value
            droppedTrackableEditText.requestFocus()
        } else {
            droppedTrackableEditText.setText("")
        }
    }


    override fun onPause() {
        saveChanges()
        super.onPause()
    }

    fun playPing() {
        // AudioManager audio settings for adjusting the volume
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        soundPool!!.play(soundID, volume, volume, 1, 0, 1f)
    }

    private fun setupAudio() {
        val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        soundPool = SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build()
        soundID = soundPool?.load(this, R.raw.ping, 1)!!
    }

    fun takePhoto(view: View?) {
        takePhoto()
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // TODO: Error occurred while creating the File
                ex.printStackTrace()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this,
                        "net.teamtruta.tiaires.fileprovider",
                        photoFile)
                Log.d(TAG, photoURI.toString())
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
        val imageFileName = "GEOCACHEID_" + currentGeoCacheInTour!!.id + "_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        currentGeoCacheInTour!!.pathToImage = currentPhotoPath
        return image
    }

    private fun galleryAddPic() {
        //invoke the system's media scanner to add your photo to the Media Provider's database,
        // making it available in the Android Gallery application and to other apps.
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(currentPhotoPath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    fun showPhotoPopup(view: View?) {

        // If the current geoCache has no photo then go straight into the photo taking
        if (currentGeoCacheInTour!!.pathToImage == null) {
            takePhoto()
            // Set the checkbox to checked
            val photoCheckBox = findViewById<CheckBox>(R.id.photo_checkbox)
            photoCheckBox.isChecked = true
            return
        }

        // Set the checkbox to checked
        val photoCheckBox = findViewById<CheckBox>(R.id.photo_checkbox)
        photoCheckBox.isChecked = true
        val popup = PopupMenu(this, view)
        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this)
        popup.inflate(R.menu.photo_actions)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.take_new_photo) {
            takePhoto()
            return true
        }
        if (id == R.id.show_geo_cache_photo) {
            showGeoCachePhoto()
            return true
        }
        if (id == R.id.delete_geo_cache_photo) {
            deleteGeoCachePhoto()
            return true
        }
        return false
    }

    private fun deleteGeoCachePhoto() {
        val file = File(Objects.requireNonNull(currentGeoCacheInTour!!.pathToImage))
        if (file.exists()) {
            if (file.delete()) {
                println("File Deleted :")
            } else {
                println("File not Deleted :")
            }
        }
        currentGeoCacheInTour!!.pathToImage = null
        val photoCheckbox = findViewById<CheckBox>(R.id.photo_checkbox)
        photoCheckbox.isChecked = false
    }

    /*  // Throws an exception because it exposes App files to the system
    void showCachePhoto(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + currentGeoCache.getPathToImage()), "image/ *");
        startActivity(intent);
    }
*/
    private fun showGeoCachePhoto() {
        val builder = Dialog(this)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.window!!.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT))
        builder.setOnDismissListener { }
        val imageView = ImageView(this)
        imageView.setImageURI(Uri.parse(currentGeoCacheInTour!!.pathToImage))
        builder.addContentView(imageView, RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT))
        builder.show()
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}
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
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.microsoft.appcenter.analytics.Analytics
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheInTour
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import androidx.fragment.app.viewModels
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.databinding.FragmentGeoCacheDetailBinding
import net.teamtruta.tiaires.viewModels.GeoCacheDetailViewModelFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "geoCacheID"

class GeoCacheDetailFragment : Fragment(), PopupMenu.OnMenuItemClickListener{

    private var geoCacheID: Long = -1L

    private var currentGeoCacheInTour: GeoCacheInTour? = null
    private var soundPool: SoundPool? = null
    private var soundID = 0
    private var currentPhotoPath: String = ""
    private var TAG: String = GeoCacheDetailActivity::class.java.simpleName

    private val viewModel: GeoCacheDetailViewModel by viewModels{
            GeoCacheDetailViewModelFactory((requireActivity().application as App).repository)}

    private var _binding: FragmentGeoCacheDetailBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            geoCacheID = it.getLong(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment - this method is called only after onCreate
        _binding = FragmentGeoCacheDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Method called when the Fragment's view has been created
        super.onViewCreated(view, savedInstanceState)

        // Set button's onClick methods
        view.findViewById<CheckBox>(R.id.foundTrackableCheckBox).setOnClickListener{
            onFoundTrackableCheckboxClicked(it)
        }
        view.findViewById<CheckBox>(R.id.droppedTrackableCheckBox).setOnClickListener{
            onDroppedTrackableCheckboxClicked(it)
        }
        view.findViewById<ImageView>(R.id.photo_iv).setOnClickListener{
            takePhoto(it)
        }
        view.findViewById<CheckBox>(R.id.photo_checkbox).setOnClickListener{
            showPhotoPopup(it)
        }

        // Observe geocache that was clicked on
        viewModel.getGeoCacheInTourFromID(geoCacheID).observe(requireActivity(),
                { geoCacheInTour: GeoCacheInTourWithDetails -> setupCacheSpecificData(geoCacheInTour) })

        //  Setup ping sound
        setupAudio()
    }

    private fun setupCacheSpecificData(geoCacheInTour: GeoCacheInTourWithDetails) {
        currentGeoCacheInTour = geoCacheInTour.geoCacheInTour

        val currentGeoCache = geoCacheInTour.geoCache.geoCache

        // Set Not Found / Found / DNF toggle and appropriate onClickListener
        var buttonStates = booleanArrayOf(true, false, false)
        if (currentGeoCacheInTour!!.currentVisitOutcome === VisitOutcomeEnum.Found) buttonStates = booleanArrayOf(false, true, false) else if (currentGeoCacheInTour!!.currentVisitOutcome === VisitOutcomeEnum.DNF) buttonStates = booleanArrayOf(false, false, true)
        binding.geoCacheVisitButton.states = buttonStates

        binding.geoCacheVisitButton.setOnValueChangedListener { position: Int ->
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
        binding.needsMaintenanceCheckBox.isChecked = currentGeoCacheInTour!!.needsMaintenance

        // 2. FoundTrackable Checkbox and EditText
        if (currentGeoCacheInTour!!.foundTrackable != null) {
            binding.foundTrackableCheckBox.isChecked = true
            binding.foundTrackableEditText.setText(currentGeoCacheInTour!!.foundTrackable)
        }

        // 3. DroppedTrackable Checkbox and EditText
        if (currentGeoCacheInTour!!.droppedTrackable != null) {
            binding.droppedTrackableCheckBox.isChecked = true
            binding.droppedTrackableEditText.setText(currentGeoCacheInTour!!.droppedTrackable)
        }

        // 4. Favourite Point Checkbox
        if (currentGeoCacheInTour!!.favouritePoint) binding.favouritePointCheckBox?.isChecked = true

        // Set my notes
        val myNotes = currentGeoCacheInTour!!.notes
        if (myNotes != "") binding.notes.setText(myNotes)

        // Setup photo
        binding.photoCheckbox.isChecked = currentGeoCacheInTour!!.pathToImage != null
    }

    /**
     * Handle Saving in the geoCache in tour activity
     */
    private fun saveChanges() {
        Analytics.trackEvent("GeoCacheDetailActivity.saveChanges")

        // save changes
        if( binding.needsMaintenanceCheckBox != null && binding.favouritePointCheckBox != null
                && binding.foundTrackableEditText != null && binding.droppedTrackableEditText != null && binding.notes != null){
        viewModel.updateGeocaCheInTourDetails(currentGeoCacheInTour!!,
            binding.needsMaintenanceCheckBox.isChecked,
            binding.favouritePointCheckBox.isChecked,
            binding.foundTrackableEditText.text.toString(),
            binding.droppedTrackableEditText.text.toString(),
            binding.notes.text.toString())
            }
    }


    private fun onFoundTrackableCheckboxClicked(view: View?) {
        if (binding.foundTrackableCheckBox?.isChecked == true) {
            // Focus attention on text box to fill in value
            binding.foundTrackableEditText?.requestFocus()
        } else {
            // Delete inputted trackable code in editText area
            binding.foundTrackableEditText?.setText("")
        }
    }

    private fun onDroppedTrackableCheckboxClicked(view: View?) {
        if (binding.droppedTrackableCheckBox?.isChecked == true) {
            // Focus attention on text box to fill in value
            binding.droppedTrackableEditText?.requestFocus()
        } else {
            binding.droppedTrackableEditText?.setText("")
        }
    }


    override fun onPause() {
        saveChanges()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun playPing() {
        // AudioManager audio settings for adjusting the volume
        val audioManager = activity?.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
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
        soundID = soundPool?.load(activity, R.raw.ping, 1)!!
    }

    private fun takePhoto(view: View?) {
        takePhoto()
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
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
                val photoURI = FileProvider.getUriForFile(requireActivity(),
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            galleryAddPic()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
        val imageFileName = "GEOCACHEID_" + currentGeoCacheInTour!!.id + "_" + timeStamp + "_"
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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
        requireActivity().sendBroadcast(mediaScanIntent)
    }

    private fun showPhotoPopup(view: View?) {

        // If the current geoCache has no photo then go straight into the photo taking
        if (currentGeoCacheInTour!!.pathToImage == null) {
            takePhoto()
            // Set the checkbox to checked
            binding.photoCheckbox.isChecked = true
            return
        }
        val context = activity
        if(context != null && isAdded){
            // Set the checkbox to checked
            binding.photoCheckbox.isChecked = true
            val popup = PopupMenu(context, view)
            // This activity implements OnMenuItemClickListener
            popup.setOnMenuItemClickListener(this)
            popup.inflate(R.menu.photo_actions)
            popup.show()
        }
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
        binding.photoCheckbox.isChecked = false
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

        val context = activity
        if(context != null){
            val builder = Dialog(context)
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
            builder.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT))
            builder.setOnDismissListener { }
            val imageView = ImageView(context)
            imageView.setImageURI(Uri.parse(currentGeoCacheInTour!!.pathToImage))
            builder.addContentView(imageView, RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT))
            builder.show()
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment GeoCacheDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Long) =
                GeoCacheDetailFragment().apply {
                    arguments = Bundle().apply {
                        putLong(ARG_PARAM1, param1)
                    }
                }
    }
}
package net.teamtruta.tiaires

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.microsoft.appcenter.analytics.Analytics
import net.teamtruta.tiaires.GeoCacheListAdapter.*
import net.teamtruta.tiaires.data.GeoCache
import net.teamtruta.tiaires.data.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.GeocachingTourWithCaches
import net.teamtruta.tiaires.viewModels.TourViewModel
import net.teamtruta.tiaires.viewModels.TourViewModelFactory
import java.util.*

class TourActivity : AppCompatActivity(), EditOnClickListener, GoToOnClickListener, OnVisitListener {

    private var _tour: GeocachingTourWithCaches? = null

    private var soundPool: SoundPool? = null
    private var soundID = 0
    private val TAG = TourActivity::class.java.simpleName
    private var draftsToUpload: List<GeoCacheInTourWithDetails>? = null

    private val viewModel: TourViewModel by viewModels{
        TourViewModelFactory((application as App).repository)
    }

    private val geoCAcheDetailActivityRequestCode = 1

    // Connect to tour view model
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayHomeAsUpEnabled(true)

        // Set GeoCache List
        val geoCacheListView = findViewById<RecyclerView>(R.id.tour_view)
        val layoutManager = LinearLayoutManager(this)
        geoCacheListView.layoutManager = layoutManager
        onVisitListener = this
        val geoCacheListAdapter = GeoCacheListAdapter(this,
                this, applicationContext, viewModel)
        geoCacheListView.adapter = geoCacheListAdapter

        // Get tour from ID
        viewModel.getCurrentTour().observe(this,
            { tour ->

                if(tour!= null){ // otherwise the app crashes when tour is deleted
                _tour = tour

                // Set title
                val tourName = _tour?.tour?.name
                ab.title = tourName

                // Set progress
                setProgressBar()

                // Add data to recyclerView
                geoCacheListAdapter.setGeoCacheInTourList(_tour?.tourGeoCaches)

                // Focus recyclerView on last visited geocache
                var lastVisitedCacheIndex: Int = _tour!!.getLastVisitedGeoCache()
                if (lastVisitedCacheIndex > 2) lastVisitedCacheIndex -= 2
                layoutManager.scrollToPosition(lastVisitedCacheIndex)

                // Add swipe to visit action
                val itemTouchHelper = ItemTouchHelper(GeoCacheInteractionCallback(geoCacheListAdapter))
                itemTouchHelper.attachToRecyclerView(geoCacheListView)
            }})


        // Create diving line between elements
        val dividerItemDecoration = DividerItemDecoration(geoCacheListView.context, LinearLayout.VERTICAL)
        dividerItemDecoration.setDrawable(ColorDrawable(getColor(R.color.black)))
        geoCacheListView.addItemDecoration(dividerItemDecoration)


        //  Setup ping sound
        setupAudio()
    }

    private fun reloadTourGeoCaches() {
        val progressBar = findViewById<ConstraintLayout>(R.id.progress_layout)
        progressBar.visibility = View.VISIBLE

        _tour?.let { viewModel.refreshTourGeoCacheDetails(it) }
    }

    private fun setProgressBar() {
        val progressText = findViewById<TextView>(R.id.tour_progress)
        val progress = "${_tour?.getNumFound()} + ${_tour?.getNumDNF()} / ${_tour?.getSize()}"
        progressText.text = progress
    }

    fun editTour(view: View?) {
        val intent = Intent(this, TourCreationActivity::class.java)
        //intent.putExtra(App.TOUR_ID_EXTRA, tourID)
        intent.putExtra(App.EDIT_EXTRA, true)
        startActivity(intent)
    }

    fun deleteTour(view: View?) {
        val tourName = _tour!!.tour.name
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete tour?")
        builder.setMessage("This will delete tour $tourName")
        builder.setPositiveButton("Confirm") { _: DialogInterface?, _: Int ->
            val properties: MutableMap<String, String> = HashMap()
            properties["TourName"] = tourName
            properties["UserConfirmed"] = "true"
            Analytics.trackEvent("TourActivity.deleteTour", properties)

            // Delete Tour
            viewModel.deleteTour(_tour!!)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { _: DialogInterface?, _: Int ->
            // User cancelled the dialog
            val properties: MutableMap<String, String> = HashMap()
            properties["TourName"] = tourName
            properties["UserConfirmed"] = "false"
            Analytics.trackEvent("TourActivity.deleteTour", properties)
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun goToMap(view: View?) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    override fun onEditClick(geoCacheID: Long) {
        val intent = Intent(this, GeoCacheDetailActivity::class.java)
        intent.putExtra(App.GEOCACHE_ID_EXTRA, geoCacheID)
        intent.putExtra(App.TOUR_ID_EXTRA, _tour?.tour?.id)
        startActivityForResult(intent, geoCAcheDetailActivityRequestCode)
    }

    override fun onGoToClick(geoCache: GeoCache) {

        // Open geocache in Geocache app or website
        val chooser = AlertDialog.Builder(this)
                .setMessage("Which app would you like to use to go to this geocache?")
                .setPositiveButton("Geocaching") { _: DialogInterface?, _: Int ->
                    val url = "https://coord.info/" + geoCache.code
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
                .setNegativeButton("Google Maps") { _: DialogInterface?, _: Int ->
                    val gmmIntentUri = Uri.parse(String.format(resources.getString(R.string.coordinates_format),
                            geoCache.latitude.value, geoCache.longitude.value))
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    startActivity(mapIntent)
                }
                .setNeutralButton("Cancel") { _: DialogInterface?, _: Int -> }
        chooser.create().show()
    }

    override fun onBackPressed() {
        // do what you want to do when the "back" button is pressed.
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun share(view: View?) {
        var tourGeoCacheCodesString = _tour?.getTourGeoCacheCodes().toString()
        tourGeoCacheCodesString = tourGeoCacheCodesString.substring(1, tourGeoCacheCodesString.length - 1)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "vnd.android.cursor.dir/email"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, _tour!!.tour.name)
        shareIntent.putExtra(Intent.EXTRA_TEXT, tourGeoCacheCodesString)
        this.startActivity(shareIntent)
    }

    override fun onVisit(visit: String) {

        // Play ping
        playPing()
        val snackbar = Snackbar.make(findViewById(R.id.tour_view), "Geocache was marked as: $visit", Snackbar.LENGTH_LONG)
        snackbar.show()
        setProgressBar()
    }

    private fun playPing() {
        // AudioManager audio settings for adjusting the volume
        val audioManager = (getSystemService(AUDIO_SERVICE) as AudioManager)
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

    fun reloadTour(view: View?) {
        reloadTourGeoCaches()
    }

    fun showAttributeInfo(view: View?) {

        val allAttributesList = _tour?.tourGeoCaches
                ?.flatMap { x -> x.geoCache.attributes }?.map { x -> x.attributeType }
                ?.distinct()
                ?.filter { x -> x != GeoCacheAttributeEnum.NeedsMaintenance } ?: return

        if(allAttributesList.isEmpty()){

            // Tell user that this tour doesn't need anything special
            val builder : AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Looks like this tour doesn't have any special requirements!")
            builder.setPositiveButton("OK"){ _: DialogInterface, _: Int -> }
            builder.show()

        } else {

            val dialogView = layoutInflater.inflate(R.layout.attribute_dialog, null)
            val listView = dialogView.findViewById<ListView>(R.id.attribute_list_dialog)
            val listAdapter = GeoCacheAttributeListAdapter(this, allAttributesList)
            listView.adapter = listAdapter

            val dialog = Dialog(this)
            dialog.setContentView(dialogView)
            dialog.show()
        }

    }

    fun uploadDrafts(view: View?) {

        // Show dialog asking user if they really want tp upload their drafts right now
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Draft Upload")
                .setMessage("Are you sure you want to upload drafts for the caches you visited? " +
                        "You won't be able to upload new drafts for those caches.")
                .setPositiveButton("OK") { _: DialogInterface?, _: Int -> uploadDrafts() }
                .setNegativeButton(getString(R.string.cancel)
                ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        dialogBuilder.create().show()
    }

    private fun uploadDrafts() {

        // Get list of caches that have already been visited (but that have not been logged before)
        val visitedGeoCaches = _tour?.tourGeoCaches
                ?.filter { x ->  (x.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.DNF &&
                        x.geoCache.geoCache.previousVisitOutcome != VisitOutcomeEnum.DNF)
                        || (x.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.Found &&
                        x.geoCache.geoCache.previousVisitOutcome != VisitOutcomeEnum.Found)}
                ?.filter { x -> !x.geoCacheInTour.draftUploaded }
                ?.toList()

        draftsToUpload = visitedGeoCaches

        // Get authentication cookie from shared preferences
        val sharedPreferences = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val authCookie = sharedPreferences.getString(getString(R.string.authentication_cookie_key), "")

        if(authCookie.equals("")) {
            if (authCookie != null) {
                Log.d(TAG, authCookie)
            }
            // TODO request user to login
            return
        }

        val draftUploadTask = DraftUploadTask(authCookie,
                visitedGeoCaches, this)
        draftUploadTask.execute()
    }

    fun onDraftUpload(message: String?, success: Boolean?) {

        if (success == true){
            draftsToUpload?.forEach {
                gcit ->
                    gcit.geoCacheInTour.draftUploaded = true
                viewModel.updateGeoCacheInTour(gcit.geoCacheInTour)
            }
        }

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Draft Upload")
                .setMessage(message)
                .setPositiveButton("OK") { _: DialogInterface?, _: Int -> }
        dialogBuilder.create().show()
    }
}
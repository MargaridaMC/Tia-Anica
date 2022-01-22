package net.teamtruta.tiaires.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.microsoft.appcenter.analytics.Analytics
import net.teamtruta.tiaires.*
import net.teamtruta.tiaires.adapters.GeoCacheAttributeListAdapter
import net.teamtruta.tiaires.adapters.GeoCacheListAdapter
import net.teamtruta.tiaires.adapters.GeoCacheListAdapter.*
import net.teamtruta.tiaires.callbacks.GeoCacheInteractionCallback
import net.teamtruta.tiaires.data.models.*
import net.teamtruta.tiaires.viewModels.TourViewModel
import net.teamtruta.tiaires.viewModels.TourViewModelFactory
import java.util.*

class TourActivity : AppCompatActivity(),
        EditOnClickListener,
        GoToOnClickListener,
        OnVisitListener,
        ViewHolder.ClickListener{

    private var _tour: GeocachingTourWithCaches? = null

    private var soundPool: SoundPool? = null
    private var soundID = 0
    private val TAG = TourActivity::class.java.simpleName

    private val viewModel: TourViewModel by viewModels{
        TourViewModelFactory((application as App).repository)
    }

    lateinit var geoCacheListAdapter: GeoCacheListAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    var actionMode: ActionMode? = null


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
        //val onVisitListener = this
        geoCacheListAdapter = GeoCacheListAdapter(this,
                this, this,
            applicationContext, viewModel, this)
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
                _tour?.tourGeoCaches?.let { geoCacheListAdapter.setGeoCacheInTourList(it) }

                // Focus recyclerView on last visited geocache
                var lastVisitedCacheIndex: Int = _tour!!.getLastVisitedGeoCache()
                if (lastVisitedCacheIndex > 2) lastVisitedCacheIndex -= 2
                layoutManager.scrollToPosition(lastVisitedCacheIndex)

                // Add swipe to visit action
                itemTouchHelper = ItemTouchHelper(
                        GeoCacheInteractionCallback(this, geoCacheListAdapter))
                itemTouchHelper.attachToRecyclerView(geoCacheListView)
            }})


        // Create diving line between elements
        val dividerItemDecoration = DividerItemDecoration(geoCacheListView.context, LinearLayout.VERTICAL)
        dividerItemDecoration.setDrawable(ColorDrawable(getColor(R.color.black)))
        geoCacheListView.addItemDecoration(dividerItemDecoration)

        //  Setup ping sound
        setupAudio()

        // Setup Bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_toolbar)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.refresh_button -> {
                    reloadTour()
                    true
                }
                R.id.delete_button -> {
                    deleteTour()
                    true
                }
                R.id.edit_tour_button -> {
                    editTour()
                    true
                }
                R.id.share_button -> {
                    share()
                    true
                }
                R.id.map_button -> {
                    goToMap()
                    true
                }
                    else -> false
                }
        }

    }

    private fun reloadTourGeoCaches() {
        viewModel.gettingTour.observe(this) { value: Boolean? -> setTourLoadingWidgetVisibility(value) }
        _tour?.let { viewModel.refreshTourGeoCacheDetails(it) }
    }

    private fun setProgressBar() {
        val progressText = findViewById<TextView>(R.id.tour_progress)
        val progress = "${_tour?.getNumFound()} + ${_tour?.getNumDNF()} / ${_tour?.getSize()}"
        progressText.text = progress
    }

    private fun editTour() {
        val intent = Intent(this, TourCreationActivity::class.java)
        intent.putExtra(App.EDIT_EXTRA, true)
        startActivity(intent)
    }

    private fun deleteTour() {
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

    private fun goToMap() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    override fun onEditClick(geoCacheInTour: GeoCacheInTourWithDetails) {
        if(geoCacheInTour.geoCache.waypoints.isNotEmpty()){
            val intent = Intent(this, GeoCacheDetailWithWaypointActivity::class.java)
            intent.putExtra(App.GEOCACHE_IN_TOUR_ID_EXTRA, geoCacheInTour.geoCacheInTour.id)
            startActivity(intent)
        } else {
            val intent = Intent(this, GeoCacheDetailActivity::class.java)
            intent.putExtra(App.GEOCACHE_IN_TOUR_ID_EXTRA, geoCacheInTour.geoCacheInTour.id)
            startActivity(intent)
        }


    }

    override fun onGoToClick(geoCache: GeoCache?) {

        // Open geocache in Geocache app or website
        val chooser = AlertDialog.Builder(this)
                .setMessage("Which app would you like to use to go to this geocache?")
                .setPositiveButton("Geocaching") { _: DialogInterface?, _: Int ->
                    val url = "https://coord.info/" + geoCache?.code
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                }
                .setNegativeButton("Google Maps") { _: DialogInterface?, _: Int ->
                    val gmmIntentUri = Uri.parse(String.format(resources.getString(R.string.coordinates_format),
                            geoCache?.latitude?.value, geoCache?.longitude?.value))
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

    fun share() {
        var tourGeoCacheCodesString = _tour?.getTourGeoCacheCodes().toString()
        tourGeoCacheCodesString = tourGeoCacheCodesString.substring(1, tourGeoCacheCodesString.length - 1)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "vnd.android.cursor.dir/email"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, _tour!!.tour.name)
        shareIntent.putExtra(Intent.EXTRA_TEXT, tourGeoCacheCodesString)
        this.startActivity(shareIntent)
    }

    override fun onVisit(visit: String?) {

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

    private fun reloadTour() {
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

        viewModel.draftUploadResult.observe(this, {
            draftUploadResult -> draftUploadResult.getContentIfNotHandled()?.let {
            (_, message, _) ->
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Draft Upload")
                    .setMessage(message)
                    .setPositiveButton("OK") { _: DialogInterface?, _: Int -> }
            dialogBuilder.create().show()
        }
        })

        viewModel.uploadTourDrafts(_tour)
    }


    private fun setTourLoadingWidgetVisibility(value: Boolean?) {
        val progressBar = findViewById<ConstraintLayout>(R.id.progress_layout)
        if (value != null) {
            if (value) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    // Action / Edit Mode
    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.tour_edit_mode, menu)
            mode?.title = "1 selected"
            isInActionMode = true
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    // Remove selected geoCaches from tour
                    for(geoCacheCode in selectedGeoCacheCodes){
                        val geoCacheInTourToRemove: GeoCacheInTourWithDetails? =
                            _tour?.tourGeoCaches?.filter { x -> x.geoCache.geoCache.code == geoCacheCode }
                                ?.get(0)
                        if (geoCacheInTourToRemove != null) {
                            viewModel.removeGeoCacheFromTour(geoCacheInTourToRemove )
                        }
                    }
                    selectedGeoCacheCodes = ArrayList()
                    mode?.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            isInActionMode = false
            selectedGeoCacheCodes.clear()
        }
    }


    override fun onItemClicked(position: Int) {
        selectItem(position)
    }

    override fun onItemLongClicked(position: Int): Boolean {
        if(actionMode != null){
            return false
        }
        actionMode = startSupportActionMode(callback)
        geoCacheListAdapter.notifyDataSetChanged()
        selectItem(position)
        return true
    }

    private fun selectItem(position: Int) {
        val selectedGeoCacheCode: String = _tour?.tourGeoCaches?.get(position)?.geoCache?.geoCache?.code ?: return

        if (!selectedGeoCacheCodes.contains(selectedGeoCacheCode)) {
            selectedGeoCacheCodes.add(selectedGeoCacheCode)
        } else {
            selectedGeoCacheCodes.remove(selectedGeoCacheCode)
        }
        updateViewCounter()
        geoCacheListAdapter.notifyItemChanged(position)
    }

    private fun updateViewCounter() {
        val counter: Int = selectedGeoCacheCodes.size
        if (counter >= 1) {
            actionMode?.title = "$counter item(s) selected"
        } else {
            actionMode?.finish()
            geoCacheListAdapter.notifyDataSetChanged()
        }

    }

    companion object{
        var isInActionMode = false
        var selectedGeoCacheCodes: ArrayList<String> = ArrayList()
    }

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }
}
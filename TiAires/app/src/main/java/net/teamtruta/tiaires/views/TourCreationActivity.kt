package net.teamtruta.tiaires.views

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.microsoft.appcenter.analytics.Analytics
import net.teamtruta.tiaires.databinding.ActivityTourCreationBinding
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.viewModels.LoginViewModel
import net.teamtruta.tiaires.viewModels.LoginViewModelFactory
import net.teamtruta.tiaires.viewModels.TourCreationViewModel
import net.teamtruta.tiaires.viewModels.TourCreationViewModelFactory
import java.util.*
import java.util.regex.Pattern

class TourCreationActivity : AppCompatActivity() {
    private var _tour: GeocachingTourWithCaches? = null // when activity is initially open
    private var _geoCacheCodesList: MutableList<String> = ArrayList()

    private val viewModel: TourCreationViewModel by viewModels{
        TourCreationViewModelFactory((application as App).repository)
    }
    private val loginViewModel: LoginViewModel by viewModels{
        LoginViewModelFactory((application as App).groundspeakRepository)
    }

    private lateinit var binding: ActivityTourCreationBinding

    /**
     * Activity initializer
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourCreationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        // if editing an existing tour
        val intent = intent
        val edit = intent.getBooleanExtra(App.EDIT_EXTRA, false)
        if (edit) // && !_originalTourName.equals(""))
        {
            /*Map<String, String> properties = new HashMap<>();
            properties.put("TourName", _originalTourName);
            properties.put("Operation", "Edit");
            Analytics.trackEvent("TourCreationActivity.onCreate", properties);*/

            // Get tour from ID
            viewModel.currentTour.observe(this, { tour: GeocachingTourWithCaches? -> setupTourSpecificData(tour) })
            val enterButton = findViewById<Button>(R.id.create_tour_button)
            enterButton.setText(R.string.save_changes)
            //TODO: enterButton.setOnClickListener(); -- just to do changes and don't get everything again
        } else {
            val properties: MutableMap<String, String> = HashMap()
            //properties.put("TourName", _originalTourName);
            properties["Operation"] = "Create"
            Analytics.trackEvent("TourCreationActivity.onCreate", properties)
        }
    }

    private fun setupTourSpecificData(tour: GeocachingTourWithCaches?) {
        _tour = tour
        val tourTitleView = findViewById<EditText>(R.id.tour_name)
        tourTitleView.setText(_tour!!.tour.name)

        // get the codes of the caches to put in the text field
        val geoCacheCodesView = findViewById<EditText>(R.id.geo_cache_codes)
        val allCodesString = _tour!!.getTourGeoCacheCodes().toString()
        geoCacheCodesView.setText(allCodesString.substring(1, allCodesString.length - 1))
    }

    /**
     * When user clicks to create a new tour or save changes to an existing one
     */
    fun createTour(view: View?) {


        // Get cache codes from UI
        val tourNameField = findViewById<EditText>(R.id.tour_name)
        val newTourName = tourNameField.text.toString()
        val tourGeoCacheCodesField = findViewById<EditText>(R.id.geo_cache_codes)
        var tourGeoCacheCodes = tourGeoCacheCodesField.text.toString()
        tourGeoCacheCodes = tourGeoCacheCodes.toUpperCase()

        // Extract all codes
        val m = Pattern.compile("GC[0-9A-Z]+").matcher(tourGeoCacheCodes)
        while (m.find()) {
            _geoCacheCodesList.add(m.group())
        }
        val properties: MutableMap<String, String> = HashMap()
        properties["TourName"] = newTourName
        properties["NumGeoCaches"] = _geoCacheCodesList.size.toString()
        Analytics.trackEvent("TourCreationActivity.createTour", properties)

        // go get the details of each geocache
        getTour(newTourName)
    }

    /**
     * Use GeocachingScrapper to get information of the caches in the specified tour.
     * This name can possibly have been changed, from the original name it had when the activity was initially opened.
     * @param tourName Name of Tour.
     */
    fun getTour(tourName: String) {

        // If we have caches to get, check for internet access
         if (!isNetworkConnected) {
            Log.e("TAG", "Not connected")
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Unable to get caches. Please make sure you are connected to the internet.")
                    .setPositiveButton("Ok.") { _: DialogInterface?, _: Int -> }
            val dialog = builder.create()
            dialog.show()
            return
        }

        // Check that user is properly logged in
        loginViewModel.loginSuccessful.observe(this, { loginEventContent ->
            loginEventContent.getContentIfNotHandled()?.let {
                    (success, _) ->
                if(!success){
                    // Login not success
                    Log.e("TAG", "Not logged in")
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Unable to get caches. Please refresh your Geocaching credentials in the Authentication page.")
                        .setPositiveButton("Ok.") { _: DialogInterface?, _: Int ->
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)}
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    // Need new tour and add these caches
                    if (_tour == null) {
                        viewModel.createNewTourWithCaches(tourName, _geoCacheCodesList)
                    } else {
                        if (_tour!!.tour.name != tourName) {
                            _tour!!.tour.name = tourName
                            viewModel.updateGeoCachingTour(_tour!!)
                        }
                        viewModel.setGeoCachesInExistingTour(_geoCacheCodesList, _tour!!)
                    }
                    //_progressBar = findViewById(R.id.progress_layout)
                    binding.progressLayout.root.visibility = View.VISIBLE
                    viewModel.gettingTour.observe(this, { value: Boolean? -> setTourLoadingWidgetVisibility(value) })

                }
            }
        })
        loginViewModel.userIsLoggedIn()

    }

    private fun setTourLoadingWidgetVisibility(value: Boolean?) {
        val progressBar = findViewById<ConstraintLayout>(R.id.progress_layout)
        if (value != null) {
            if (value) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.INVISIBLE
                onTourCreated()
            }
        }
    }

    private fun onTourCreated() {
        val intent = Intent(this, TourActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        // do what you want to do when the "back" button is pressed.
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private val isNetworkConnected: Boolean
        get() {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }
}
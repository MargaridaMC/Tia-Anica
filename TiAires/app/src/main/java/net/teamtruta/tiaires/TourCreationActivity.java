package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.microsoft.appcenter.analytics.Analytics;

import net.teamtruta.tiaires.db.DbConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TourCreationActivity extends AppCompatActivity
{
    GeocachingTour _tour = null; // when activity is initially open
    List<String> _geoCacheCodesList = new ArrayList<>();
    ConstraintLayout _progressBar;
    long tourID = -1;

    DbConnection _dbConnection;

    /**
     * Activity initializer
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_creation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        // Setup Database Connection
        _dbConnection = new DbConnection(this);

        Intent intent = getIntent();
        // final String tourName = intent.getStringExtra("_tourName");
        //_originalTourName = intent.getStringExtra("_tourName");
        boolean edit = intent.getBooleanExtra(App.EDIT_EXTRA, false);

        // if editing an existing tour
        if(edit)// && !_originalTourName.equals(""))
        {
            /*Map<String, String> properties = new HashMap<>();
            properties.put("TourName", _originalTourName);
            properties.put("Operation", "Edit");
            Analytics.trackEvent("TourCreationActivity.onCreate", properties);*/

            tourID = intent.getLongExtra(App.TOUR_ID_EXTRA, -1L);
            if(tourID == -1L){
                // TODO: Something went wrong
            }

            // Get tour from ID
            _tour = GeocachingTour.getGeocachingTourFromID(tourID, _dbConnection);

            EditText tourTitleView = findViewById(R.id.tour_name);
            tourTitleView.setText(_tour.getName());

            Button enterButton = findViewById(R.id.create_tour_button);
            enterButton.setText(R.string.save_changes);
            //TODO: enterButton.setOnClickListener(); -- just to do changes and don't get everything again

            // read the tour from file
           /* String rootPath = App.getTourRoot();
            _tour = GeocachingTour.read(rootPath, _originalTourName);//GeocachingTour.fromFile(rootPath, _originalTourName);
*/
            // get the codes of the caches to put in the text field
            EditText geoCacheCodesView = findViewById(R.id.geo_cache_codes);
            String allCodesString = _tour.getTourGeoCacheCodes().toString();
            geoCacheCodesView.setText(allCodesString.substring(1, allCodesString.length() - 1));
        }
        else
        {
            Map<String, String> properties = new HashMap<>();
            //properties.put("TourName", _originalTourName);
            properties.put("Operation", "Create");
            Analytics.trackEvent("TourCreationActivity.onCreate", properties);
        }

    }

    /**
     * When user clicks to create a new tour or save changes to an existing one
     */
    public void createTour(View view) {


        // Get cache codes from UI and save to file
        EditText tourNameField = findViewById(R.id.tour_name);
        String _newTourName = tourNameField.getText().toString();

        EditText tourGeoCacheCodesField = findViewById(R.id.geo_cache_codes);
        String tourGeoCacheCodes = tourGeoCacheCodesField.getText().toString();
        tourGeoCacheCodes = tourGeoCacheCodes.toUpperCase();

        // Extract all codes
        Matcher m = Pattern.compile("GC[0-9A-Z]+").matcher(tourGeoCacheCodes);
        while (m.find()) {
            _geoCacheCodesList.add(m.group());
        }

        Map<String, String> properties = new HashMap<>();
        properties.put("TourName", _newTourName);
        properties.put("NumGeoCaches", Integer.toString(_geoCacheCodesList.size()));
        Analytics.trackEvent("TourCreationActivity.createTour", properties);

        // go get the details of each geocache
        getTour(_newTourName);
    }

    /**
     * Use GeocachingScrapper to get information of the caches in the specified tour.
     * This name can possibly have been changed, from the original name it had when the activity was initially opened.
     * @param tourName Name of Tour.
     */
    public void getTour(String tourName) {

        // If we have caches to get, check for internet access
        if(!isNetworkConnected()){

            Log.e("TAG", "Not connected");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Unable to get caches. Please make sure you are connected to the internet.")
                    .setPositiveButton("Ok.", (dialog, id) -> {
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            return;
        }

        // Need new tour and add these caches
        if(_tour == null){
            _tour = new GeocachingTour(tourName, false, _dbConnection);
        } else {
            _tour.changeName(tourName);
        }


        _progressBar = findViewById(R.id.progress_layout);
        _progressBar.setVisibility(View.VISIBLE);

        _tour.tourCreationActivityDelegate = this;
        _tour.addToTour(_geoCacheCodesList);

    }

    void onTourCreated(){
        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, _tour._id);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // do what you want to do when the "back" button is pressed.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
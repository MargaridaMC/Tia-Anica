package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.appcenter.analytics.Analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TourCreationActivity extends AppCompatActivity implements PostGeocachingScrapping
{
    GeocachingTour _tour = null; // when activity is initially open
    String _originalTourName;
    String _newTourName;
    List<String> _geocacheCodesList = new ArrayList<>();
    ConstraintLayout _progressBar;
    List<String> cachesToGet;

    /**
     * Activity initializer
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_creation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        Intent intent = getIntent();
        // final String tourName = intent.getStringExtra("_tourName");
        _originalTourName = intent.getStringExtra("_tourName");
        boolean edit = intent.getBooleanExtra("edit", false);

        // if editing an existing tour
        if(edit && !_originalTourName.equals(""))
        {
            Map<String, String> properties = new HashMap<>();
            properties.put("TourName", _originalTourName);
            properties.put("Operation", "Edit");
            Analytics.trackEvent("TourCreationActivity.onCreate", properties);

            EditText tourTitleView = findViewById(R.id.tour_name);
            tourTitleView.setText(_originalTourName);

            Button enterButton = findViewById(R.id.create_tour_button);
            enterButton.setText("Save Changes");
            //TODO: enterButton.setOnClickListener(); -- just to do changes and don't get everything again

            // read the tour from file
            String rootPath = App.getTourRoot();
            _tour = GeocachingTour.read(rootPath, _originalTourName);//GeocachingTour.fromFile(rootPath, _originalTourName);

            // get the codes of the caches to put in the text field
            List<String> allCodes = _tour.getTourCacheCodes();
            EditText geocacheCodesView = findViewById(R.id.geocache_codes);
            String allCodesString = allCodes.toString();
            geocacheCodesView.setText(allCodesString.substring(1, allCodesString.length() - 1));
        }
        else
        {
            Map<String, String> properties = new HashMap<>();
            properties.put("TourName", _originalTourName);
            properties.put("Operation", "Create");
            Analytics.trackEvent("TourCreationActivity.onCreate", properties);
        }

    }

    /**
     * When user clicks to create a new tour or save changes to an existing one
     * @param view
     */
    public void createTour(View view) {


        // Get cache codes from UI and save to file
        EditText tourNameField = findViewById(R.id.tour_name);
        _newTourName = tourNameField.getText().toString();

        EditText tourGeocacheCodesField = findViewById(R.id.geocache_codes);
        String tourGeocacheCodes = tourGeocacheCodesField.getText().toString();
        tourGeocacheCodes = tourGeocacheCodes.toUpperCase();

        // Extract all codes
        Matcher m = Pattern.compile("GC[0-9A-Z]+").matcher(tourGeocacheCodes);
        while (m.find()) {
            _geocacheCodesList.add(m.group());
        }

        Map<String, String> properties = new HashMap<>();
        properties.put("TourName", _newTourName);
        properties.put("NumCaches", Integer.toString(_geocacheCodesList.size()));
        Analytics.trackEvent("TourCreationActivity.createTour", properties);

        /* COMMENTED FOR NOW / HAVE TO DECIDE IF WE DO THIS OR ADD A CLONE FEATURE

        // Check if there doesn't exist a tour with this name
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        ArrayList<GeocachingTourSummary> allTours = TourList.read(rootPath);

        for (int i = 0; i < allTours.size(); i++) {
            String n = allTours.get(i).getName();
            if (n.equals(_newTourName)) {
                // There already exists an entry for this tour.
                // Ask if user wants to overwrite it
                getPermissionOverwrite();
                return;
            }
        }
         */

        // go get the details of each geocache
        getTour(_newTourName);
    }

    /**
     * Use GeocachingScrapper to get information of the caches in the specified tour.
     * This name can possibly have been changed, from the original name it had when the activity was initially opened.
     * @param tourName Name of Tour.
     */
    public void getTour(String tourName) {

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authCookie = sharedPreferences.getString(getString(R.string.authentication_cookie_key), "");

        cachesToGet = new ArrayList<>(_geocacheCodesList);
        // Process the deltas from the old list to the new list
        if (_tour != null) {

            // 1. Remove from the original tour caches that are not in the new one
            for (String currentTourCache : _tour.getTourCacheCodes()) {
                if (!_geocacheCodesList.contains(currentTourCache)) {
                    _tour.removeFromTour(currentTourCache);
                }
            }

            // 2. Remove from the list of caches to fetch, those we already have loaded
            for (String loadedCache : _tour.getTourCacheCodes()) {
                cachesToGet.remove(loadedCache); // don't get the information again. If the list doens't containt the cache nothing will happen
            }
        }

        // If we have caches to get, check for internet access
        if(!isNetworkConnected()){

            Log.e("TAG", "Not connected");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Unable to get caches. Please make sure you are connected to the internet.")
                    .setPositiveButton("Ok.", (dialog, id) -> {
                        return;
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            return;
        }

        // we need to do this even if there are no new caches, because there's additional code to account for tour rename in the task

        if (authCookie.equals("")) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Login information is missing. Please input your credentials in the login screen.");
            builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        return;
                    });
            builder.setNegativeButton(getString(R.string.cancel), ((dialog, which) -> {}));

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {

            _progressBar = findViewById(R.id.progress_layout);
            _progressBar.setVisibility(View.VISIBLE);

            GeocachingScrapper scrapper = new GeocachingScrapper(authCookie);
            GeocachingScrappingTask geocachingScrappingTask = new GeocachingScrappingTask(scrapper, cachesToGet);
            geocachingScrappingTask.delegate = this;
            geocachingScrappingTask.execute(tourName);
        }
    }

    /**
     * Not used. Re-evaluate in the future vs a Tour Clone feature.
     */
    /*
    public void getPermissionOverwrite(){

        // final String[] newTourName = {null};
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_with_input, null);

        final EditText input = inflatedView.findViewById(R.id.input);
        input.setText(_newTourName);

        builder.setView(inflatedView);

        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
                _newTourName = input.getText().toString();
                Log.d("TAG", "NEW name " + _newTourName);
                getTour(_newTourName);
            });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }
    */
    @Override
    public void onGeocachingScrappingTaskResult(List<Geocache> newlyLoadedCaches) {

        Toast t = Toast.makeText(this, "Tour created. Saving.", Toast.LENGTH_SHORT);
        t.show();

        // Append entry to tour list file or alter it if we just want the tour
        String rootPath = App.getTourRoot();

        // If this is a new tour we need to create one instance
        if(_tour == null)
        {
            _tour = new GeocachingTour(_newTourName);
            _tour.addToTour(newlyLoadedCaches);

        }
        else
        {
            // if it already exists we need to make sure the name is correct

            GeocachingTour newTour = new GeocachingTour(_newTourName);
            int indexInOriginalTour = 0;
            int indexFromLoadedCaches = 0;
            for(String code : _geocacheCodesList){

                // If cache is in previous tour, just add it to the new one
                if(indexInOriginalTour < _tour.getSize()) {
                    Geocache cacheFromOriginalTour = _tour.getCacheInTour(indexInOriginalTour).getGeocache();
                    if (code.equals(cacheFromOriginalTour.getCode())) {
                        newTour.addToTour(cacheFromOriginalTour);
                        indexInOriginalTour++;
                    } // Else get it from the newly loaded caches
                    else {
                        newTour.addToTour(newlyLoadedCaches.get(indexFromLoadedCaches));
                        indexFromLoadedCaches++;
                    }
                } else { // Since we've reached the end of the caches in original tour just add the remaining loaded ones to the new tour
                    List toAdd = newlyLoadedCaches.subList(indexFromLoadedCaches, newlyLoadedCaches.size());//.toArray(new Geocache[0]);
                    newTour.addToTour(toAdd);
                }
            }

            _tour = newTour;

        }

        // Write tour to file -- will  be overwritten if there is a file with same name
        //_tour.toFile(rootPath);
        GeocachingTour.write(rootPath, _tour);

        // Add it to tour list
        ArrayList<GeocachingTourSummary> gts;
        GeocachingTourSummary summary;
        String allToursFilePath = App.getAllToursFilePath();
        if(TourList.exists(allToursFilePath))
        {
            // if the tour was renamed we need to remove the old name
            gts = TourList.read(allToursFilePath);
            gts.removeIf( tour -> tour.getName().equals(_originalTourName));
        } else {
            gts = new ArrayList<>();
        }

        summary = _tour.getSummary();
        gts.add(summary);
        TourList.write(allToursFilePath, gts);


        // Check if we were unable to get any of the requested caches
        List<String> newlyLoadedCachesCodes = newlyLoadedCaches.stream().map(gc -> gc.getCode()).collect(Collectors.toList());
        List<String> geocachesNotObtained = new ArrayList<>();
        for(String code: cachesToGet){
            if(!newlyLoadedCachesCodes.contains(code)) geocachesNotObtained.add(code);
        }

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("_tourName", _newTourName);
        if(geocachesNotObtained.size() != 0){
            String geocachesNotObtainedString = geocachesNotObtained.toString();
            geocachesNotObtainedString = geocachesNotObtainedString.substring(1, geocachesNotObtainedString.length() - 1);
            intent.putExtra("geocachesNotObtained", geocachesNotObtainedString);
        }
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
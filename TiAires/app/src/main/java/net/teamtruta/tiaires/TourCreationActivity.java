package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.appcenter.analytics.Analytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TourCreationActivity extends AppCompatActivity implements PostGeocachingScrapping {

    GeocachingTour _tour = null; // when activity is initially open
    String _originalTourName;
    String _newTourName;
    List<String> _geocacheCodesList = new ArrayList<>();
    ConstraintLayout _progressBar;

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
            Analytics.trackEvent("Entered screen to editing existing tour");
            EditText tourTitleView = findViewById(R.id.tour_name);
            tourTitleView.setText(_originalTourName);

            Button enterButton = findViewById(R.id.create_tour_button);
            enterButton.setText("Save Changes");
            //TODO: enterButton.setOnClickListener(); -- just to do changes and don't get everything again

            // read the tour from file
            String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
            _tour = GeocachingTour.fromFile(rootPath, _originalTourName);

            // get the codes of the caches to put in the text field
            List<String> allCodes = _tour.getTourCacheCodes();
            EditText geocacheCodesView = findViewById(R.id.geocache_codes);
            String allCodesString = allCodes.toString();
            geocacheCodesView.setText(allCodesString.substring(1, allCodesString.length() - 1));
        }
        else
        {
            Analytics.trackEvent("Entered screen to create new tour");
        }

    }

    /**
     * When user clicks to create a new tour or save changes to an existing one
     * @param view
     */
    public void createTour(View view) {
        Analytics.trackEvent("User clicked to created/Update tour");

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

        // Check if there doesn't exist a tour with this name
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        ArrayList<GeocachingTourSummary> allTours = TourList.read(rootPath);

        /* COMMENTED FOR NOW / HAVE TO DECIDE IF WE DO THIS OR ADD A CLONE FEATURE
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
    public void getTour(String tourName){

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authCookie = sharedPreferences.getString(getString(R.string.authentication_cookie_key), "");

        // Process the deltas from the old list to the new list
        if (_tour != null) {

            // 1. Remove from the original tour caches that are not in the new one
            for (String currentTourCache : _tour.getTourCacheCodes()) {
                if (!_geocacheCodesList.contains(currentTourCache)) {
                    _tour.removeFromTour(currentTourCache);
                }
            }

            // 2. Remove from the list of caches to fetch, those we already have loaded
            for (String loadedCache : _tour.getTourCacheCodes() )
            {
                if(_geocacheCodesList.contains(loadedCache))
                {
                    _geocacheCodesList.remove(loadedCache); // don't get the information again
                }
            }
        }

        // only scrape if there's any new cache in the tour
        if(_geocacheCodesList.size() > 0) {
            if (authCookie.equals("")) {
                // TODO: prompt for login
            } else {

                _progressBar = findViewById(R.id.progress_layout);
                _progressBar.setVisibility(View.VISIBLE);

                GeocachingScrapper scrapper = new GeocachingScrapper(authCookie);
                GeocachingScrappingTask geocachingScrappingTask = new GeocachingScrappingTask(scrapper, _geocacheCodesList);
                geocachingScrappingTask.delegate = this;
                geocachingScrappingTask.execute(tourName);
            }
        }
    }

    /**
     * Not used. Re-evaluate in the future vs a Tour Clone feature.
     */
    public void getPermissionOverwrite(){

        // final String[] newTourName = {null};
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_with_input, null);

        final EditText input = inflatedView.findViewById(R.id.input);
        input.setText(_newTourName);

        builder.setView(inflatedView);

        // Set up the buttons
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // newTourName[0] = input.getText().toString();
                _newTourName = input.getText().toString();
                Log.d("TAG", "NEW name " + _newTourName);
                getTour(_newTourName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    @Override
    public void onGeocachingScrappingTaskResult(List<Geocache> newlyLoadedCaches) {

        Toast t = Toast.makeText(this, "Tour created. Saving to file.", Toast.LENGTH_SHORT);
        t.show();

        // Append entry to tour list file or alter it if we just want the tour
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        File allToursFile = new File(rootPath, getString(R.string.all_tours_filename));

        // If this is a new tour we need to create one instance
        if(_tour == null)
        {
            _tour = new GeocachingTour(_newTourName);
        }
        else
        {
            // if it already exists we need to make sure the name is correct
            _tour.setName(_newTourName);
        }

        // Add loaded caches to tour
        for(Geocache loadedCache : newlyLoadedCaches)
        {
            _tour.addToTour(loadedCache);
        }

        // Write tour to file -- will  be overwritten if there is a file with same name
        _tour.toFile(rootPath);

        // Add it to tour list
        if(allToursFile.exists())
        {
            // if the tour was renamed we need to remove the old name
            ArrayList<GeocachingTourSummary> gts = TourList.read(rootPath);
            gts.removeIf( tour -> tour.getName().equals(_originalTourName));
            gts.add(_tour);
            TourList.write(rootPath, gts);
        } else {
            ArrayList<GeocachingTourSummary> gts = new ArrayList<GeocachingTourSummary>();
            gts.add(_tour);
            TourList.write(rootPath, gts);
        }

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("_tourName", _newTourName);
        startActivity(intent);
    }
}

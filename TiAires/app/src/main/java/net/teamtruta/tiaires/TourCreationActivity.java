package net.teamtruta.tiaires;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.microsoft.appcenter.analytics.Analytics;

import net.teamtruta.tiaires.data.GeocachingTourWithCaches;
import net.teamtruta.tiaires.viewModels.TourCreationViewModel;
import net.teamtruta.tiaires.viewModels.TourCreationViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TourCreationActivity extends AppCompatActivity
{
    GeocachingTourWithCaches _tour = null; // when activity is initially open
    List<String> _geoCacheCodesList = new ArrayList<>();
    ConstraintLayout _progressBar;

    TourCreationViewModel viewModel;

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

        // Setup ViewModel
        viewModel = new TourCreationViewModelFactory(((App) getApplication()).getRepository())
                .create(TourCreationViewModel.class);

        Intent intent = getIntent();
        boolean edit = intent.getBooleanExtra(App.EDIT_EXTRA, false);

        // if editing an existing tour
        if(edit)// && !_originalTourName.equals(""))
        {
            /*Map<String, String> properties = new HashMap<>();
            properties.put("TourName", _originalTourName);
            properties.put("Operation", "Edit");
            Analytics.trackEvent("TourCreationActivity.onCreate", properties);*/

            // Get tour from ID
            viewModel.getCurrentTour().observe(this, this::setupTourSpecificData);


            Button enterButton = findViewById(R.id.create_tour_button);
            enterButton.setText(R.string.save_changes);
            //TODO: enterButton.setOnClickListener(); -- just to do changes and don't get everything again

        }
        else
        {
            Map<String, String> properties = new HashMap<>();
            //properties.put("TourName", _originalTourName);
            properties.put("Operation", "Create");
            Analytics.trackEvent("TourCreationActivity.onCreate", properties);
        }

    }

    void setupTourSpecificData(GeocachingTourWithCaches tour){
        _tour = tour;
        EditText tourTitleView = findViewById(R.id.tour_name);
        tourTitleView.setText(_tour.getTour().getName());

        // get the codes of the caches to put in the text field
        EditText geoCacheCodesView = findViewById(R.id.geo_cache_codes);
        String allCodesString = _tour.getTourGeoCacheCodes().toString();
        geoCacheCodesView.setText(allCodesString.substring(1, allCodesString.length() - 1));
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
            viewModel.createNewTourWithCaches(tourName, _geoCacheCodesList);
        } else {
            if(!_tour.getTour().getName().equals(tourName)) {
                _tour.getTour().setName(tourName);
                viewModel.updateGeoCachingTour(_tour);
            }

            viewModel.setGeoCachesInExistingTour(_geoCacheCodesList, _tour);
        }

        _progressBar = findViewById(R.id.progress_layout);
        _progressBar.setVisibility(View.VISIBLE);

        viewModel.getGettingTour().observe(this, this::setTourLoadingWidgetVisibility);

    }

    private void setTourLoadingWidgetVisibility(Boolean value) {
        ConstraintLayout progressBar = findViewById(R.id.progress_layout);

        if(value != null){
            if(value){
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
                onTourCreated();
            }
        }
    }

    public void onTourCreated(){

        Intent intent = new Intent(this, TourActivity.class);
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
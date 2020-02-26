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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TourCreationActivity extends AppCompatActivity implements PostGeocachingScrapping {

    String _tourName;
    List<String> geocacheCodesList = new ArrayList<>();
    ConstraintLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_creation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        Intent intent = getIntent();
        final String tourName = intent.getStringExtra("_tourName");
        boolean edit = intent.getBooleanExtra("edit", false);

        if(edit && !tourName.equals("")){

            EditText tourTitleView = findViewById(R.id.tour_name);
            tourTitleView.setText(tourName);

            Button enterButton = findViewById(R.id.create_tour_button);
            enterButton.setText("Save Changes");
            //TODO: enterButton.setOnClickListener(); -- just to do changes and don't get everything again

            String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
            File tourFolder = new File(rootPath);
            GeocachingTour tour = GeocachingTour.fromFile(tourFolder, tourName);

            List<String> allCodes = tour.getTourCacheCodes();
            EditText geocacheCodesView = findViewById(R.id.geocache_codes);
            String allCodesString = allCodes.toString();
            geocacheCodesView.setText(allCodesString.substring(1, allCodesString.length() - 1));

        }

    }

    public void createTour(View view){

        // Get cache codes from UI and save to file
        EditText tourNameField = findViewById(R.id.tour_name);
        _tourName = tourNameField.getText().toString();

        EditText tourGeocacheCodesField = findViewById(R.id.geocache_codes);
        String tourGeocacheCodes = tourGeocacheCodesField.getText().toString();
        tourGeocacheCodes = tourGeocacheCodes.toUpperCase();

        // Extract all codes

        Matcher m = Pattern.compile("GC[0-9A-Z]+").matcher(tourGeocacheCodes);
        while(m.find()){
            geocacheCodesList.add(m.group());
        }

        // Check if there doesn't exist a tour with this name
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        File allToursFile = new File(rootPath, getString(R.string.all_tours_filename));
        ArrayList<GeocachingTour> allTours = TourList.fromFile(allToursFile);

        for(int i = 0; i < allTours.size(); i++){
            String n = allTours.get(i).getName();
            if(n.equals(_tourName)){
                // There already exists an entry for this tour.
                // Ask if user wants to overwrite it
                getPermissionOverwrite();
                return;
            }
        }

        getTour(this._tourName);

    }

    public void getTour(String tourName){

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authCookie = sharedPreferences.getString(getString(R.string.authentication_cookie_key), "");

        if(authCookie.equals("")){
            // TODO: prompt for login
        } else {

            progressBar = findViewById(R.id.progress_layout);
            progressBar.setVisibility(View.VISIBLE);

            GeocachingScrapper scrapper = new GeocachingScrapper(authCookie);
            GeocachingScrappingTask geocachingScrappingTask = new GeocachingScrappingTask(scrapper, geocacheCodesList);
            geocachingScrappingTask.delegate = this;
            geocachingScrappingTask.execute(tourName);
        }
    }

    public void getPermissionOverwrite(){

        final String[] newTourName = {null};
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_with_input, null);

        final EditText input = inflatedView.findViewById(R.id.input);
        input.setText(_tourName);

        builder.setView(inflatedView);

        // Set up the buttons
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newTourName[0] = input.getText().toString();
                Log.d("TAG", "NEW name " + newTourName[0]);
                getTour(newTourName[0]);
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
    public void onGeocachingScrappingTaskResult(GeocachingTour tour) {

        Toast t = Toast.makeText(this, "List created. Saving to file.", Toast.LENGTH_SHORT);
        t.show();

        // Append entry to tour list file or alter it if we just want the tour
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        File allToursFile = new File(rootPath, getString(R.string.all_tours_filename));

        // Write tour to file -- will  be overwritten if there is a file with same name
        File root = new File(rootPath);
        tour.toFile(root);

        // Add it to tour list
        if(allToursFile.exists()){
            // If the file already exists just append a new entry to it
            TourList.appendToFile(tour, allToursFile);
        } else {
            // Else create a tour list file
            String newTourString = ";" + tour.getMetaDataJSON().toString();
            TourList.toFile(newTourString, allToursFile);
        }

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("_tourName", _tourName);
        startActivity(intent);

    }
}

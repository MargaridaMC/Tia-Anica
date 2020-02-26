package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TourCreationActivity extends AppCompatActivity implements PostGeocachingScrapping {

    String tourName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_creation);

        Intent intent = getIntent();
        final String tourName = intent.getStringExtra("tourName");
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

            // Back button should lead back to Tour Activity
            Button backButton = findViewById(R.id.back_button);
            final Context context = this;
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TourActivity.class);
                    intent.putExtra("tourName", tourName);
                    startActivity(intent);
                }
            });

        }

    }

    public void goBack(View view){
        Intent intent  = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void createTour(View view){

        // Get cache codes from UI and save to file
        EditText tourNameField = findViewById(R.id.tour_name);
        tourName = tourNameField.getText().toString();

        EditText tourGeocacheCodesField = findViewById(R.id.geocache_codes);
        String tourGeocacheCodes = tourGeocacheCodesField.getText().toString();
        tourGeocacheCodes = tourGeocacheCodes.toUpperCase();

        // Extract all codes
        List<String> geocacheCodesList = new ArrayList<>();
        Matcher m = Pattern.compile("GC[0-9A-Z]+").matcher(tourGeocacheCodes);
        while(m.find()){
            geocacheCodesList.add(m.group());
        }

        // Check that we are logged in
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authCookie = sharedPreferences.getString(getString(R.string.authentication_cookie_key), "");
        if(authCookie.equals("")){
            // TODO: prompt for login
        } else {
            GeocachingScrapper scrapper = new GeocachingScrapper(authCookie);
            GeocachingScrappingTask geocachingScrappingTask = new GeocachingScrappingTask(scrapper, geocacheCodesList);
            geocachingScrappingTask.delegate = this;
            geocachingScrappingTask.execute(tourName);
        }

    }

    @Override
    public void onGeocachingScrappingTaskResult(GeocachingTour tour) {

        Toast t = Toast.makeText(this, "List created. Saving to file.", Toast.LENGTH_SHORT);
        t.show();

        // Write tour to file
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        File root = new File(rootPath);
        tour.toFile(root);

        // Append entry to tour list file
        File allToursFile = new File(rootPath, getString(R.string.all_tours_filename));
        String newTourString = ";" + tour.getMetaDataJSON().toString();

        if(allToursFile.exists()){
            // If the file already exists just append a new entry to it
            try {
                FileOutputStream os = new FileOutputStream(allToursFile, true);
                os.write(newTourString.getBytes(), 0, newTourString.length());
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // Else create a tour list file
            tourList.toFile(newTourString, allToursFile);
        }

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("tourName", tourName);
        startActivity(intent);

        // TODO: open tour page
    }
}

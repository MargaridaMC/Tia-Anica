package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TourCreationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_creation);

    }

    public void goBack(View view){
        Intent intent  = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void createTour(View view){

        // Get cache codes from UI and save to file
        EditText tourNameField = findViewById(R.id.tour_name);
        String tourName = tourNameField.getText().toString();

        EditText tourGeocacheCodesField = findViewById(R.id.geocache_codes);
        String tourGeocacheCodes = tourGeocacheCodesField.getText().toString();
        tourGeocacheCodes = tourGeocacheCodes.toUpperCase();

        // Extract all codes
        List<String> geocacheCodesList = new ArrayList<>();
        Matcher m = Pattern.compile("GC[0-9A-Z]+").matcher(tourGeocacheCodes);
        while(m.find()){
            geocacheCodesList.add(m.group());
        }

        File path = this.getFilesDir();
        GeocachingTour tour = createTour(tourName, geocacheCodesList);
        tour.toFile(path);

        // TODO: open tour page
        // Toast t = Toast.makeText(this, "List Created.", Toast.LENGTH_SHORT);
        // t.show();
        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("tourName", tourName);
        startActivity(intent);
    }

    GeocachingTour createTour(String tourName, List<String> geocacheCodesList){

        GeocachingTour tour = new GeocachingTour(tourName);

        // Check that we are logged in
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authCookie = sharedPreferences.getString(getString(R.string.authentication_cookie_key), "");
        if(authCookie.equals("")){

            // TODO: prompt for login
        } else {
            GeocachingScrapper scrapper = new GeocachingScrapper(authCookie);
            for(String code:geocacheCodesList){

                try {
                    Geocache gc = scrapper.getGeocacheDetails(code);
                    tour.addToTour(gc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return tour;
    }



}

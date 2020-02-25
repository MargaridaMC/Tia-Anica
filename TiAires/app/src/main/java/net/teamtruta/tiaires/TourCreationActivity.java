package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

        GeocachingTour tour = createTour(tourName, geocacheCodesList);
        saveTourToFile(tourName, tour);

        // TODO: open tour page
        Toast t = Toast.makeText(this, "List Created.", Toast.LENGTH_SHORT);
        t.show();
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

    void saveTourToFile(String tourName, GeocachingTour tour){

        JSONArray tourJSON = tour.toJSON();

        // Save tour to file
        String filename = tourName + "_tour.json";
        try (FileWriter file = new FileWriter(filename)) {

            file.write(tourJSON.toString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    GeocachingTour tourFromFile(String tourName){

        JSONParser jsonParser = new JSONParser();
        GeocachingTour newTour = new GeocachingTour(tourName);

        String filename = tourName + "_tour.json";
        try (FileReader reader = new FileReader(filename))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray newCacheArray = (JSONArray) obj;
            int size = newCacheArray.length();

            for(int i = 0; i<size; i++){
                JSONObject cacheObject = (JSONObject) newCacheArray.get(i);
                Geocache gc = new Geocache();
                gc.fromJSON(cacheObject);
                newTour.addToTour(gc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newTour;

    }

}

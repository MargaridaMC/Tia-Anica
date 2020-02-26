package net.teamtruta.tiaires;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class CacheDetailActivity extends AppCompatActivity {

    GeocachingTour tour;
    int currentCacheIndex;
    GeocacheInTour currentCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_cache_detail);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        // Get the tour info
        String currentTourString = intent.getStringExtra("currentTour");
        try {
            tour = GeocachingTour.fromJSON(new JSONObject(currentTourString));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get the clicked cache info
        currentCacheIndex = intent.getIntExtra("currentCacheIndex", -1);
        if(currentCacheIndex == -1){
            // TODO: Something went wrong -- check
            return;
        }

        currentCache = tour.getCacheInTour(currentCacheIndex);

        // Set Cache Title
        ActionBar ab = getSupportActionBar();
        ab.setTitle(currentCache.geocache.name);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set Found / DNF switches
        Switch foundSwitch = findViewById(R.id.found_switch);
        Switch dnfSwitch = findViewById(R.id.dnf_switch);
        if(currentCache.getVisit() == FoundEnumType.Found){
            foundSwitch.setChecked(true);
            dnfSwitch.setChecked(false);
        } else if(currentCache.getVisit() == FoundEnumType.DNF) {
            foundSwitch.setChecked(false);
            dnfSwitch.setChecked(true);
        } else {
            foundSwitch.setChecked(false);
            dnfSwitch.setChecked(false);
        }

        // Set my notes
        EditText notesSection = findViewById(R.id.notes);
        String myNotes = currentCache.getNotes();
        if(!myNotes.equals("")) notesSection.setText(myNotes);


    }

    @Override
    public boolean onSupportNavigateUp() {
        //onBackPressed();

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("tourName", tour.getName());
        startActivity(intent);

        return true;
    }

    public void cacheFound(View view){
        // Cache was found

        if(currentCache.getVisit() == FoundEnumType.Found){
            // We want to reverse this -- set cache as not Attempted

            currentCache.setVisit(FoundEnumType.NotAttempted);
            tour._numFound -= 1;

        } else {
            // We want to set this cache as found
            // Can't have both switches on at the same time. The clicked switch will be on automatically
            Switch dnfSwitch = findViewById(R.id.dnf_switch);
            dnfSwitch.setChecked(false);

            currentCache.setVisit(FoundEnumType.Found);
            tour._numFound += 1;
        }

       // Replace position of this cache in tour
        tour.setCacheInTour(currentCacheIndex, currentCache);

        // Finally save changes to file
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        File root = new File(rootPath);
        tour.toFile(root);

        File allToursFile = new File(root, getString(R.string.all_tours_filename));
        ArrayList<GeocachingTour> tourList = TourList.fromFile(allToursFile);
        for(int i = 0; i< tourList.size(); i++){
            if(tourList.get(i).getName().equals(tour.getName())){
                tourList.set(i, tour);
                break;
            }
        }
        TourList.toFile(tourList, allToursFile);

        Toast t = Toast.makeText(this, "Changes saved.", Toast.LENGTH_SHORT);
        t.show();


    }

    public void cacheNotFound(View view){
        // DNF

        if(currentCache.getVisit() == FoundEnumType.DNF){
            // We want to reverse this -- set cache as not Attempted

            currentCache.setVisit(FoundEnumType.NotAttempted);
            tour._numDNF -= 1;

        } else {
            // We want to set this cache as DNF
            // Can't have both switches on at the same time. The clicked switch will be on automatically
            // Can't have both switches on at the same time. The clicked switch will be on automatically
            Switch foundSwitch = findViewById(R.id.found_switch);
            foundSwitch.setChecked(false);

            currentCache.setVisit(FoundEnumType.DNF);
            tour._numDNF += 1;
        }

        // Replace position of this cache in tour
        tour.setCacheInTour(currentCacheIndex, currentCache);

        // Finally save changes to file
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        File root = new File(rootPath);
        tour.toFile(root);

        File allToursFile = new File(root, getString(R.string.all_tours_filename));
        ArrayList<GeocachingTour> tourList = TourList.fromFile(allToursFile);
        for(int i = 0; i< tourList.size(); i++){
            if(tourList.get(i).getName().equals(tour.getName())){
                tourList.set(i, tour);
                break;
            }
        }
        TourList.toFile(tourList, allToursFile);

        Toast t = Toast.makeText(this, "Changes saved.", Toast.LENGTH_SHORT);
        t.show();

    }
}

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

import com.microsoft.appcenter.analytics.Analytics;

import org.json.JSONException;
import org.json.JSONObject;

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
        /*try {
            tour = GeocachingTour.fromJSON(new JSONObject(currentTourString));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        tour = GeocachingTour.fromString(currentTourString);

        // Get the clicked cache info
        currentCacheIndex = intent.getIntExtra("currentCacheIndex", -1);
        if(currentCacheIndex == -1){
            // TODO: Something went wrong -- check
            return;
        }

        currentCache = tour.getCacheInTour(currentCacheIndex);

        // Set Cache Title
        ActionBar ab = getSupportActionBar();
        ab.setTitle(currentCache.getGeocache().getName());
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
        intent.putExtra("_tourName", tour.getName());
        startActivity(intent);

        return true;
    }




    public void cacheFound(View view){
        // Cache was found

        if(currentCache.getVisit() == FoundEnumType.Found){
            // We want to reverse this -- set cache as not Attempted

            currentCache.setVisit(FoundEnumType.NotAttempted);
            //tour._numFound -= 1;

        } else {
            // We want to set this cache as found
            // Can't have both switches on at the same time. The clicked switch will be on automatically
            Switch dnfSwitch = findViewById(R.id.dnf_switch);
            if(dnfSwitch.isChecked()){
                //tour._numDNF -= 1;
                dnfSwitch.setChecked(false);
            }

            currentCache.setVisit(FoundEnumType.Found);
            //tour._numFound += 1;
        }

    }

    public void cacheNotFound(View view){
        // DNF

        if(currentCache.getVisit() == FoundEnumType.DNF){
            // We want to reverse this -- set cache as not Attempted

            currentCache.setVisit(FoundEnumType.NotAttempted);
            //tour._numDNF -= 1;

        } else {
            // We want to set this cache as DNF
            // Can't have both switches on at the same time. The clicked switch will be on automatically
            Switch foundSwitch = findViewById(R.id.found_switch);
            if(foundSwitch.isChecked()){
                //tour._numFound -= 1;
                foundSwitch.setChecked(false);
            }

            currentCache.setVisit(FoundEnumType.DNF);
            //tour._numDNF += 1;
        }

    }

    /**
     * Handle Saving in the cache in tour activity
     * @param view
     */
    public void saveChanges(View view)
    {
        Analytics.trackEvent("CacheDetailActivity.saveChanges");

        // Get notes and save changes
        EditText notesView = findViewById(R.id.notes);
        String myNotes = notesView.getText().toString();
        currentCache.setNotes(myNotes);

        // Replace position of this cache in tour
        tour.setCacheInTour(currentCacheIndex, currentCache);

        // Finally save changes to file
        String rootPath = App.getTourRoot();
        //tour.toFile(rootPath);
        GeocachingTour.write(rootPath, tour);

        // update the element we just changed
        String allToursFilePath = App.getAllToursFilePath();
        TourList.update(allToursFilePath, tour.getSummary());

        Toast t = Toast.makeText(this, "Changes saved.", Toast.LENGTH_SHORT);
        t.show();

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("_tourName", tour.getName());
        startActivity(intent);
    }
}

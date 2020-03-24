package net.teamtruta.tiaires;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.microsoft.appcenter.analytics.Analytics;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.util.Calendar;

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

        // Set Not Found / Found / DNF toggle and appropriate onClickListener
        MultiStateToggleButton cacheVisitButton = this.findViewById(R.id.cache_visit_button);
        boolean[] buttonStates = new boolean[] {true, false, false};

        if(currentCache.getVisit() == FoundEnumType.Found)
            buttonStates = new boolean[] {false, true, false};
        else if(currentCache.getVisit() == FoundEnumType.DNF)
            buttonStates = new boolean[] {false, false, true};

        cacheVisitButton.setStates(buttonStates);

        cacheVisitButton.setOnValueChangedListener(position -> {
            if(position == 0){
                currentCache.setVisit(FoundEnumType.NotAttempted);
            }
            else if(position == 1) cacheFound();
            else if(position == 2) cacheNotFound();
        });

        // Set Checkboxes
        CheckBox needsMaintenanceCheckBox = findViewById(R.id.needsMaintenanceCheckBox);
        if(currentCache.getNeedsMaintenance()) needsMaintenanceCheckBox.setChecked(true);
        CheckBox foundTrackableCheckBox = findViewById(R.id.foundTrackableCheckBox);
        if(currentCache.getFoundTrackable()) foundTrackableCheckBox.setChecked(true);
        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        if(currentCache.getDroppedTrackable()) droppedTrackableCheckBox.setChecked(true);
        CheckBox favouritePointCheckBox = findViewById(R.id.favouritePointCheckBox);
        if(currentCache.getFavouritePoint()) favouritePointCheckBox.setChecked(true);

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



    public void cacheFound(){

        // Cache was found

        if(currentCache.getVisit() == FoundEnumType.Found){
            // If cache has already been found and we are clicking on Found again
            // We want to reverse this -- set cache as not Attempted
            currentCache.setVisit(FoundEnumType.NotAttempted);
            currentCache.setFoundDate(null);

            MultiStateToggleButton cacheVisitButton = this.findViewById(R.id.cache_visit_button);
            cacheVisitButton.setStates(new boolean[] {true, false, false});

        } else {
            // We want to set this cache as found
            currentCache.setVisit(FoundEnumType.Found);
            currentCache.setFoundDate(Calendar.getInstance().getTime());
        }


    }

    public void cacheNotFound(){
        // DNF

        if(currentCache.getVisit() == FoundEnumType.DNF){
            // If cache is already a DNF and we are clicking on DNF again
            // We want to reverse this -- set cache as not Attempted
            currentCache.setVisit(FoundEnumType.NotAttempted);
            currentCache.setFoundDate(null);

            MultiStateToggleButton cacheVisitButton = this.findViewById(R.id.cache_visit_button);
            cacheVisitButton.setStates(new boolean[] {true, false, false});

        } else {
            // We want to set this cache as DNF
            currentCache.setVisit(FoundEnumType.DNF);
            currentCache.setFoundDate(Calendar.getInstance().getTime());
        }

    }

    /**
     * Handle Saving in the cache in tour activity
     */
    public void saveChanges()
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

        // Toast t = Toast.makeText(this, "Changes saved.", Toast.LENGTH_SHORT);
        // t.show();

        // Intent intent = new Intent(this, TourActivity.class);
        // intent.putExtra("_tourName", tour.getName());
        // startActivity(intent);
    }

    public void onNeedsMaintenanceCheckboxClicked(View view) {

        CheckBox needsMaintenanceCheckBox = findViewById(R.id.needsMaintenanceCheckBox);
        if(needsMaintenanceCheckBox.isChecked())
            currentCache.setNeedsMaintenance(true);
        else currentCache.setNeedsMaintenance(false);

    }

    public void onFoundTrackableCheckboxClicked(View view) {

        CheckBox foundTrackableCheckBox = findViewById(R.id.foundTrackableCheckBox);
        if(foundTrackableCheckBox.isChecked())
            currentCache.setFoundTrackable(true);
        else currentCache.setFoundTrackable(false);

    }

    public void onDroppedTrackableCheckboxClicked(View view) {

        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        if(droppedTrackableCheckBox.isChecked())
            currentCache.setDroppedTrackable(true);
        else currentCache.setDroppedTrackable(false);

    }

    public void onFavouritePointCheckboxClicked(View view) {

        CheckBox favouritePointCheckBox = findViewById(R.id.favouritePointCheckBox);
        if(favouritePointCheckBox.isChecked())
            currentCache.setFavouritePoint(true);
        else currentCache.setFavouritePoint(false);

    }

    @Override
    protected void onPause(){
        saveChanges();
        super.onPause();
    }
}

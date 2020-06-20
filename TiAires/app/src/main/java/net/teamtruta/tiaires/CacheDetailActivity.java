package net.teamtruta.tiaires;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.widget.Toolbar;

import com.microsoft.appcenter.analytics.Analytics;

import net.teamtruta.tiaires.db.DbConnection;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.util.Calendar;

public class CacheDetailActivity extends AppCompatActivity {

    GeocacheInTour currentGeocache;

    SoundPool soundPool;
    int soundID;

    DbConnection _dbConnection;
    long _tourID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_cache_detail);
        setSupportActionBar(toolbar);

        // Setup connection to database
        _dbConnection = new DbConnection(this);

        Intent intent = getIntent();
        long cacheID = intent.getLongExtra(App.CACHE_ID_EXTRA,  -1L);
        _tourID = intent.getLongExtra(App.TOUR_ID_EXTRA, -1L);
        /*if(cacheID == -1L){
            // TODO: Something went wrong
        } else {

        }*/

        currentGeocache = GeocacheInTour.Companion.getGeocacheFromID(cacheID, _dbConnection);

        // Set Cache Title
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(currentGeocache.getGeocache().getName());
        ab.setDisplayHomeAsUpEnabled(true);

        // Set Not Found / Found / DNF toggle and appropriate onClickListener
        MultiStateToggleButton cacheVisitButton = this.findViewById(R.id.cache_visit_button);
        boolean[] buttonStates = new boolean[] {true, false, false};

        if(this.currentGeocache.getVisit() == FoundEnumType.Found)
            buttonStates = new boolean[] {false, true, false};
        else if(this.currentGeocache.getVisit() == FoundEnumType.DNF)
            buttonStates = new boolean[] {false, false, true};

        cacheVisitButton.setStates(buttonStates);

        cacheVisitButton.setOnValueChangedListener(position -> {
            if(position == 0){
                this.currentGeocache.setVisit(FoundEnumType.NotAttempted);
            }
            else if(position == 1) cacheFound();
            else if(position == 2) cacheNotFound();
        });

        // Set Checkboxes
        CheckBox needsMaintenanceCheckBox = findViewById(R.id.needsMaintenanceCheckBox);
        if(this.currentGeocache.getNeedsMaintenance()) needsMaintenanceCheckBox.setChecked(true);
        CheckBox foundTrackableCheckBox = findViewById(R.id.foundTrackableCheckBox);
        if(this.currentGeocache.getFoundTrackable()) foundTrackableCheckBox.setChecked(true);
        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        if(this.currentGeocache.getDroppedTrackable()) droppedTrackableCheckBox.setChecked(true);
        CheckBox favouritePointCheckBox = findViewById(R.id.favouritePointCheckBox);
        if(this.currentGeocache.getFavouritePoint()) favouritePointCheckBox.setChecked(true);

        // Set my notes
        EditText notesSection = findViewById(R.id.notes);
        String myNotes = this.currentGeocache.getNotes();
        if(!myNotes.equals("")) notesSection.setText(myNotes);

        //  Setup ping sound
        setupAudio();

    }

    @Override
    public boolean onSupportNavigateUp() {
        //onBackPressed();

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, _tourID);
        startActivity(intent);

        return true;
    }

    @Override
    public void onBackPressed() {
        saveChanges();
        onSupportNavigateUp();
    }



    public void cacheFound(){

        // Cache was found

        if(currentGeocache.getVisit() == FoundEnumType.Found){
            // If cache has already been found and we are clicking on Found again
            // We want to reverse this -- set cache as not Attempted
            currentGeocache.setVisit(FoundEnumType.NotAttempted);
            currentGeocache.setFoundDate(null);

            MultiStateToggleButton cacheVisitButton = this.findViewById(R.id.cache_visit_button);
            cacheVisitButton.setStates(new boolean[] {true, false, false});

        } else {
            // We want to set this cache as found
            currentGeocache.setVisit(FoundEnumType.Found);
            currentGeocache.setFoundDate(Calendar.getInstance().getTime());
            playPing();
        }


    }

    public void cacheNotFound(){
        // DNF

        if(currentGeocache.getVisit() == FoundEnumType.DNF){
            // If cache is already a DNF and we are clicking on DNF again
            // We want to reverse this -- set cache as not Attempted
            currentGeocache.setVisit(FoundEnumType.NotAttempted);
            currentGeocache.setFoundDate(null);

            MultiStateToggleButton cacheVisitButton = this.findViewById(R.id.cache_visit_button);
            cacheVisitButton.setStates(new boolean[] {true, false, false});

        } else {
            // We want to set this cache as DNF
            currentGeocache.setVisit(FoundEnumType.DNF);
            currentGeocache.setFoundDate(Calendar.getInstance().getTime());

            playPing();
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
        currentGeocache.setNotes(myNotes);

        currentGeocache.saveChanges();
    }

    public void onNeedsMaintenanceCheckboxClicked(View view) {

        CheckBox needsMaintenanceCheckBox = findViewById(R.id.needsMaintenanceCheckBox);
        if(needsMaintenanceCheckBox.isChecked())
            currentGeocache.setNeedsMaintenance(true);
        else currentGeocache.setNeedsMaintenance(false);

    }

    public void onFoundTrackableCheckboxClicked(View view) {

        CheckBox foundTrackableCheckBox = findViewById(R.id.foundTrackableCheckBox);
        if(foundTrackableCheckBox.isChecked())
            currentGeocache.setFoundTrackable(true);
        else currentGeocache.setFoundTrackable(false);

    }

    public void onDroppedTrackableCheckboxClicked(View view) {

        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        if(droppedTrackableCheckBox.isChecked())
            currentGeocache.setDroppedTrackable(true);
        else currentGeocache.setDroppedTrackable(false);

    }

    public void onFavouritePointCheckboxClicked(View view) {

        CheckBox favouritePointCheckBox = findViewById(R.id.favouritePointCheckBox);
        if(favouritePointCheckBox.isChecked())
            currentGeocache.setFavouritePoint(true);
        else currentGeocache.setFavouritePoint(false);

    }

    @Override
    protected void onPause(){
        saveChanges();
        super.onPause();
    }

    void playPing(){
        // AudioManager audio settings for adjusting the volume
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundPool.play(soundID, volume, volume, 1, 0, 1f);
    }

    void setupAudio(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();

        soundID = soundPool.load(this, R.raw.ping, 1);


    }
}
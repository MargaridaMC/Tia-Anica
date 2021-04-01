package net.teamtruta.tiaires;

import androidx.appcompat.app.ActionBar;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.microsoft.appcenter.analytics.Analytics;

import net.teamtruta.tiaires.db.DbConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TourActivity extends AppCompatActivity implements GeoCacheListAdapter.EditOnClickListener, GeoCacheListAdapter.GoToOnClickListener, GeoCacheListAdapter.OnVisitListener {

    GeocachingTour _tour;
    Long tourID = -1L;

    SoundPool soundPool;
    int soundID;

    DbConnection dbConnection;

    private final String TAG = TourActivity.class.getSimpleName();

    List<GeoCacheInTour> draftsToUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        // Setup connection to database
        dbConnection = new DbConnection(this);

        // Get the ID of the tour that was clicked on
        Intent intent = getIntent();
        tourID = intent.getLongExtra(App.TOUR_ID_EXTRA, -1);
        if(tourID == -1){
            // Something went wrong
            Log.e(TAG, "Could not get clicked tour.");
            Toast.makeText(this, "An error occurred: couldn't get the requested tour", Toast.LENGTH_LONG).show();
            return;
        }

        _tour = GeocachingTour.getGeocachingTourFromID(tourID, dbConnection);
        String tourName = _tour.getName();

        // Set title
        ab.setTitle(tourName);

        // Set progress
        setProgressBar();

        // Set List
        RecyclerView geoCacheListView = findViewById(R.id.tour_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        geoCacheListView.setLayoutManager(layoutManager);
        GeoCacheListAdapter geoCacheListAdapter = new GeoCacheListAdapter(_tour, this, this);
        GeoCacheListAdapter.onVisitListener = this;
        geoCacheListView.setAdapter(geoCacheListAdapter);

        // Focus recyclerView on last visited geocache
        int lastVisitedCacheIndex = _tour.getLastVisitedGeoCache();
        //int centerOfScreen = getResources().getDisplayMetrics().heightPixels / 2;
        //layoutManager.scrollToPositionWithOffset(lastVisitedCacheIndex - 1, centerOfScreen);
        if(lastVisitedCacheIndex > 2) lastVisitedCacheIndex -= 2;
        layoutManager.scrollToPosition(lastVisitedCacheIndex);

        // Create diving line between elements
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(geoCacheListView.getContext(), LinearLayout.VERTICAL);
        dividerItemDecoration.setDrawable(new ColorDrawable(this.getColor(R.color.black)));
        geoCacheListView.addItemDecoration(dividerItemDecoration);

        // Add swipe to visit action
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new GeoCacheInteractionCallback(geoCacheListAdapter));
        itemTouchHelper.attachToRecyclerView(geoCacheListView);

        //  Setup ping sound
        setupAudio();

    }

    private void reloadTourGeoCaches() {
        ConstraintLayout progressBar = findViewById(R.id.progress_layout);
        progressBar.setVisibility(View.VISIBLE);

        //SwipeRefreshLayout swipeToRefreshLayout = findViewById(R.id.swiperefresh);
        //swipeToRefreshLayout.setRefreshing(false);

        _tour.tourActivityDelegate = this;
        _tour.reloadTourCaches();
    }

    public void setProgressBar(){
        TextView progressText = findViewById(R.id.tour_progress);
        String progress = _tour.getNumFound() + " + " + _tour.getNumDNF() + " / " + _tour.getSize();
        progressText.setText(progress);
    }

    public void editTour(View view){
        Intent intent = new Intent(this, TourCreationActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, tourID);
        intent.putExtra(App.EDIT_EXTRA, true);
        startActivity(intent);
    }

    public void deleteTour(View view){


        final Context context = this;

        String tourName = _tour.getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete tour?");
        builder.setMessage("This will delete tour " + tourName);
        builder.setPositiveButton("Confirm", (dialog, which) -> {

            Map<String, String> properties = new HashMap<>();
            properties.put("TourName", tourName);
            properties.put("UserConfirmed", "true");
            Analytics.trackEvent("TourActivity.deleteTour", properties);

            // Delete Tour
            _tour.deleteTour();

            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);

        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog

            Map<String, String> properties = new HashMap<>();
            properties.put("TourName", tourName);
            properties.put("UserConfirmed", "false");
            Analytics.trackEvent("TourActivity.deleteTour", properties);

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void goToMap(View view){

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, tourID);
        startActivity(intent);

    }

    @Override
    public void onEditClick(long geoCacheID) {
        Intent intent = new Intent(this, GeoCacheDetailActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, tourID);
        intent.putExtra(App.GEOCACHE_ID_EXTRA, geoCacheID);
        startActivity(intent);
    }

    @Override
    public void onGoToClick(GeoCache geoCache){

        // Open geocache in Geocache app or website
        AlertDialog.Builder chooser = new AlertDialog.Builder(this)
                .setMessage("Which app would you like to use to go to this geocache?")
                .setPositiveButton("Geocaching", (dialog, which) -> {
                    String url = "https://coord.info/" + geoCache.getCode();
                    Intent i = new Intent(Intent.ACTION_VIEW);

                    i.setData(Uri.parse(url));

                    startActivity(i);
                })
                .setNegativeButton("Google Maps", (dialog, which) -> {
                    Uri gmmIntentUri = Uri.parse(String.format(getResources().getString(R.string.coordinates_format),
                            geoCache.getLatitude().getValue(), geoCache.getLongitude().getValue()));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    startActivity(mapIntent);
                })
                .setNeutralButton("Cancel", (dialog, which) -> {});

        chooser.create().show();


    }

    @Override
    public void onBackPressed() {
        // do what you want to do when the "back" button is pressed.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void share(View view){

        String tourGeoCacheCodesString = _tour.getTourGeoCacheCodes().toString();
        tourGeoCacheCodesString = tourGeoCacheCodesString.substring(1, tourGeoCacheCodesString.length() - 1);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("vnd.android.cursor.dir/email");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, _tour.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, tourGeoCacheCodesString);
        this.startActivity(shareIntent);

    }

    @Override
    public void onVisit(String visit) {

        // Play ping
        playPing();

        Snackbar snackbar = Snackbar.make(findViewById(R.id.tour_view), "Geocache was marked as: " + visit, Snackbar.LENGTH_LONG);
        snackbar.show();

        setProgressBar();

    }

    void playPing(){
        // AudioManager audio settings for adjusting the volume
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        assert audioManager != null;
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

    public void onFinishedReloadingGeoCaches() {
        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, tourID);
        startActivity(intent);
        finish();
    }

    public void reloadTour(View view) {
        reloadTourGeoCaches();
    }

    public void showAttributeInfo(View view){

        ArrayList<GeoCacheAttributeEnum> allAttributesList = _tour._tourGeoCaches.stream()
                .flatMap(x -> x.getGeoCache().getAttributes().stream()).distinct()
                .filter(x -> x != GeoCacheAttributeEnum.NeedsMaintenance)
                .collect(Collectors.toCollection(ArrayList::new));

        if(allAttributesList.size() == 0){

            // Tell user that this tour doesn't need anything special
            AlertDialog.Builder builder = new AlertDialog.Builder( this);
            builder.setMessage("Looks like this tour doesn't have any special requirements!");
            builder.setPositiveButton("OK", (dialog, which) -> {});
            builder.show();

        } else {
            final Dialog dialog = new Dialog(this);
            View dialogView = getLayoutInflater().inflate(R.layout.attribute_dialog, null);
            ListView lv = dialogView.findViewById(R.id.attribute_list_dialog);

            GeoCacheAttributeListAdapter listAdapter = new GeoCacheAttributeListAdapter(this, allAttributesList);
            lv.setAdapter(listAdapter);
            dialog.setContentView(dialogView);
            dialog.show();
        }


    }

    public void uploadDrafts(View view) {

        // Show dialog asking user if they really want tp upload their drafts right now
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Draft Upload")
                .setMessage("Are you sure you want to upload drafts for the caches you visited? " +
                        "You won't be able to upload new drafts for those caches.")
                .setPositiveButton("OK", ((dialog, which) -> uploadDrafts()))
                .setNegativeButton(getString(R.string.cancel),
                        ((dialog, which) -> dialog.cancel()));
        dialogBuilder.create().show();

    }

    private void uploadDrafts(){
        // Get list of caches that have already been visited (but that have not been logged before)
        List<GeoCacheInTour> visitedGeoCaches = _tour._tourGeoCaches.stream()
                .filter(x -> (x.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF &&
                        x.getGeoCache().getPreviousVisitOutcome() != VisitOutcomeEnum.DNF)
                        || (x.getCurrentVisitOutcome() == VisitOutcomeEnum.Found &&
                        x.getGeoCache().getPreviousVisitOutcome() != VisitOutcomeEnum.Found))
                .filter(x -> !x.getDraftUploaded())
                .collect(Collectors.toList());

        draftsToUpload = visitedGeoCaches;

        // Get authentication cookie from shared preferences
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String authCookie = sharedPref.getString(getString(R.string.authentication_cookie_key), "");
        if(authCookie.equals("")) {
            Log.d(TAG, authCookie);
            // TODO request user to login
            return;
        }

        DraftUploadTask draftUploadTask = new DraftUploadTask(authCookie,
                visitedGeoCaches, this);
        draftUploadTask.execute();
    }

    public void onDraftUpload(String message, Boolean success) {

        if (success){
            for(GeoCacheInTour gcit : draftsToUpload){
                gcit.setDraftUploaded(true);
                gcit.saveChanges();
            }
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Draft Upload")
                .setMessage(message)
                .setPositiveButton("OK", ((dialog, which) -> {}));
        dialogBuilder.create().show();
    }
}

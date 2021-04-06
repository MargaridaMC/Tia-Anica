package net.teamtruta.tiaires;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.microsoft.appcenter.analytics.Analytics;

import net.teamtruta.tiaires.data.GeoCache;
import net.teamtruta.tiaires.data.GeoCacheInTourWithDetails;
import net.teamtruta.tiaires.data.GeoCacheInTour;
import net.teamtruta.tiaires.viewModels.TourViewModel;
import net.teamtruta.tiaires.viewModels.TourViewModelFactory;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class GeoCacheDetailActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    GeoCacheInTour currentGeoCacheInTour;

    SoundPool soundPool;
    int soundID;

    long _tourID;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    String TAG = GeoCacheDetailActivity.class.getSimpleName();

    TourViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_cache_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_geo_cache_detail);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        long geoCacheID = intent.getLongExtra(App.GEOCACHE_ID_EXTRA,  -1L);
        _tourID = intent.getLongExtra(App.TOUR_ID_EXTRA, -1L);

        /*if(geoCacheID == -1L){
            // TODO: Something went wrong
        } else {

        }*/

        // Setup Action Bar
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        // Get ViewModel
        viewModel = new TourViewModelFactory(((App) getApplication()).getRepository())
                .create(TourViewModel.class);

        // Observe geocache that was clicked on
        viewModel.getGeoCacheInTourFromID(geoCacheID).observe(this,
                this::setupCacheSpecificDate
                );


        //  Setup ping sound
        setupAudio();

    }

    void setupCacheSpecificDate(GeoCacheInTourWithDetails geoCacheInTour){

        currentGeoCacheInTour = geoCacheInTour.getGeoCacheInTour();
        GeoCache currentGeoCache = geoCacheInTour.getGeoCache().getGeoCache();

        // Set GeoCache Title
        ActionBar ab = getSupportActionBar();
        ab.setTitle(currentGeoCache.getName());


        // Set Not Found / Found / DNF toggle and appropriate onClickListener
        MultiStateToggleButton geoCacheVisitButton = this.findViewById(R.id.geo_cache_visit_button);
        boolean[] buttonStates = new boolean[] {true, false, false};

        if(this.currentGeoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Found)
            buttonStates = new boolean[] {false, true, false};
        else if(this.currentGeoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF)
            buttonStates = new boolean[] {false, false, true};

        geoCacheVisitButton.setStates(buttonStates);

        geoCacheVisitButton.setOnValueChangedListener(position -> {
            if(position == 0){
                this.currentGeoCacheInTour.setCurrentVisitOutcome(VisitOutcomeEnum.NotAttempted);
            }
            else if(position == 1) geoCacheFound();
            else if(position == 2) geoCacheNotFound();
        });

        // Set Checkboxes
        // 1. Needs Maintenace Checkbox
        CheckBox needsMaintenanceCheckBox = findViewById(R.id.needsMaintenanceCheckBox);
        if(this.currentGeoCacheInTour.getNeedsMaintenance()) needsMaintenanceCheckBox.setChecked(true);

        // 2. FoundTrackable Checkbox and EditText
        CheckBox foundTrackableCheckBox = findViewById(R.id.foundTrackableCheckBox);
        if(currentGeoCacheInTour.getFoundTrackable() != null){
            foundTrackableCheckBox.setChecked(true);
            EditText foundTrackableEditText = findViewById(R.id.foundTrackableEditText);
            foundTrackableEditText.setText(currentGeoCacheInTour.getFoundTrackable());
        }

        // 3. DroppedTrackable Checkbox and EditText
        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        if(currentGeoCacheInTour.getDroppedTrackable() != null){
            droppedTrackableCheckBox.setChecked(true);
            EditText droppedTrackableEditText = findViewById(R.id.droppedTrackableEditText);
            droppedTrackableEditText.setText(currentGeoCacheInTour.getDroppedTrackable());
        }

        // 4. Favourite Point Checkbox
        CheckBox favouritePointCheckBox = findViewById(R.id.favouritePointCheckBox);
        if(this.currentGeoCacheInTour.getFavouritePoint()) favouritePointCheckBox.setChecked(true);

        // Set my notes
        EditText notesSection = findViewById(R.id.notes);
        String myNotes = this.currentGeoCacheInTour.getNotes();
        if(!myNotes.equals("")) notesSection.setText(myNotes);

        // Setup photo
        CheckBox photoCheckBox = findViewById(R.id.photo_checkbox);
        photoCheckBox.setChecked(currentGeoCacheInTour.getPathToImage() != null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //onBackPressed();

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, _tourID);
        //startActivity(intent);
        setResult(RESULT_OK, intent);
        finish();

        return true;
    }

    @Override
    public void onBackPressed() {
        saveChanges();
        onSupportNavigateUp();
    }

    public void geoCacheFound(){

        // Geocache was found

        if(currentGeoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.Found){
            // If geocache has already been found and we are clicking on Found again
            // We want to reverse this -- set geocache as not Attempted
            currentGeoCacheInTour.setCurrentVisitOutcome(VisitOutcomeEnum.NotAttempted);
            currentGeoCacheInTour.setCurrentVisitDatetime(null);

            MultiStateToggleButton geoCacheVisitButton = this.findViewById(R.id.geo_cache_visit_button);
            geoCacheVisitButton.setStates(new boolean[] {true, false, false});

        } else {
            // We want to set this geocache as found
            currentGeoCacheInTour.setCurrentVisitOutcome(VisitOutcomeEnum.Found);
            currentGeoCacheInTour.setCurrentVisitDatetime(Instant.now());
            playPing();
        }


    }

    public void geoCacheNotFound(){
        // DNF

        if(currentGeoCacheInTour.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF){
            // If geocache is already a DNF and we are clicking on DNF again
            // We want to reverse this -- set geocache as not Attempted
            currentGeoCacheInTour.setCurrentVisitOutcome(VisitOutcomeEnum.NotAttempted);
            currentGeoCacheInTour.setCurrentVisitDatetime(null);

            MultiStateToggleButton geoCacheVisitButton = this.findViewById(R.id.geo_cache_visit_button);
            geoCacheVisitButton.setStates(new boolean[] {true, false, false});

        } else {
            // We want to set this geoCache as DNF
            currentGeoCacheInTour.setCurrentVisitOutcome(VisitOutcomeEnum.DNF);
            currentGeoCacheInTour.setCurrentVisitDatetime(Instant.now());

            playPing();
        }

    }

    /**
     * Handle Saving in the geoCache in tour activity
     */
    public void saveChanges()
    {
        Analytics.trackEvent("GeoCacheDetailActivity.saveChanges");

        // Get notes
        EditText notesView = findViewById(R.id.notes);
        String myNotes = notesView.getText().toString();
        currentGeoCacheInTour.setNotes(myNotes);

        // Get trackable information
        EditText foundTrackableEditText = findViewById(R.id.foundTrackableEditText);
        String foundTrackableString = foundTrackableEditText.getText().toString().trim();
        currentGeoCacheInTour.setFoundTrackable(foundTrackableString.isEmpty() ? null : foundTrackableString);

        EditText droppedTrackableEditText = findViewById(R.id.droppedTrackableEditText);
        String droppedTrackableString = droppedTrackableEditText.getText().toString().trim();
        currentGeoCacheInTour.setDroppedTrackable(droppedTrackableString.isEmpty() ? null : droppedTrackableString);

        // save changes
        viewModel.updateGeoCacheInTour(currentGeoCacheInTour);
    }

    public void onNeedsMaintenanceCheckboxClicked(View view) {

        CheckBox needsMaintenanceCheckBox = findViewById(R.id.needsMaintenanceCheckBox);
        currentGeoCacheInTour.setNeedsMaintenance(needsMaintenanceCheckBox.isChecked());

    }

    public void onFoundTrackableCheckboxClicked(View view) {

        CheckBox foundTrackableCheckBox = findViewById(R.id.foundTrackableCheckBox);
        EditText foundTrackableEditText = findViewById(R.id.foundTrackableEditText);

        if(foundTrackableCheckBox.isChecked()){

            // Focus attention on text box to fill in value
            foundTrackableEditText.requestFocus();

            //currentGeoCache.setFoundTrackable(true);
        }

        else{
            currentGeoCacheInTour.setFoundTrackable(null);

            // Delete inputted trackable code in editText area
            foundTrackableEditText.setText("");
        }

    }

    public void onDroppedTrackableCheckboxClicked(View view) {

        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        EditText droppedTrackableEditText = findViewById(R.id.droppedTrackableEditText);

        if(droppedTrackableCheckBox.isChecked()){
            //currentGeoCache.setDroppedTrackable(true);

            // Focus attention on text box to fill in value
            droppedTrackableEditText.requestFocus();
        }

        else{
            currentGeoCacheInTour.setDroppedTrackable(null);
            droppedTrackableEditText.setText("");
        }


    }

    public void onFavouritePointCheckboxClicked(View view) {

        CheckBox favouritePointCheckBox = findViewById(R.id.favouritePointCheckBox);
        currentGeoCacheInTour.setFavouritePoint(favouritePointCheckBox.isChecked());

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

    public void takePhoto(View view){
        takePhoto();
    }

    public void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // TODO: Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "net.teamtruta.tiaires.fileprovider",
                        photoFile);
                Log.d(TAG, String.valueOf(photoURI));
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "GEOCACHEID_" + currentGeoCacheInTour.getId() + "_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        currentGeoCacheInTour.setPathToImage(currentPhotoPath);

        return image;
    }

    private void galleryAddPic() {
        //invoke the system's media scanner to add your photo to the Media Provider's database,
        // making it available in the Android Gallery application and to other apps.
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

    }

    public void showPhotoPopup(View view){

        // If the current geoCache has no photo then go straight into the photo taking
        if(currentGeoCacheInTour.getPathToImage() == null){
            takePhoto();
            // Set the checkbox to checked
            CheckBox photoCheckBox = findViewById(R.id.photo_checkbox);
            photoCheckBox.setChecked(true);
            return;
        }

        // Set the checkbox to checked
        CheckBox photoCheckBox = findViewById(R.id.photo_checkbox);
        photoCheckBox.setChecked(true);

        PopupMenu popup = new PopupMenu(this, view);
        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.photo_actions);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.take_new_photo){
            takePhoto();
            return true;
        }
        if (id == R.id.show_geo_cache_photo){
            showGeoCachePhoto();
            return true;
        }
        if(id == R.id.delete_geo_cache_photo){
            deleteGeoCachePhoto();
            return true;
        }
        return false;
    }
    private void deleteGeoCachePhoto() {
        File file = new File(Objects.requireNonNull(currentGeoCacheInTour.getPathToImage()));

        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File Deleted :" );
            } else {
                System.out.println("File not Deleted :");
            }
        }
        currentGeoCacheInTour.setPathToImage(null);
        CheckBox photoCheckbox = findViewById(R.id.photo_checkbox);
        photoCheckbox.setChecked(false);
    }

/*  // Throws an exception because it exposes App files to the system
    void showCachePhoto(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + currentGeoCache.getPathToImage()), "image/*");
        startActivity(intent);
    }
*/

    public void showGeoCachePhoto() {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(dialogInterface -> {
            //nothing;
        });

        ImageView imageView = new ImageView(this);
        imageView.setImageURI(Uri.parse(currentGeoCacheInTour.getPathToImage()));
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }
}
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

import net.teamtruta.tiaires.db.DbConnection;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CacheDetailActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    GeocacheInTour currentGeocache;

    SoundPool soundPool;
    int soundID;

    DbConnection _dbConnection;
    long _tourID;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    String TAG = CacheDetailActivity.class.getSimpleName();

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
        // 1. Needs Maintenace Checkbox
        CheckBox needsMaintenanceCheckBox = findViewById(R.id.needsMaintenanceCheckBox);
        if(this.currentGeocache.getNeedsMaintenance()) needsMaintenanceCheckBox.setChecked(true);

        // 2. FoundTrackable Checkbox and EditText
        CheckBox foundTrackableCheckBox = findViewById(R.id.foundTrackableCheckBox);
        if(currentGeocache.getFoundTrackable() != null){
            foundTrackableCheckBox.setChecked(true);
            EditText foundTrackableEditText = findViewById(R.id.foundTrackableEditText);
            foundTrackableEditText.setText(currentGeocache.getFoundTrackable());
        }

        // 3. DroppedTrackable Checkbox and EditText
        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        if(currentGeocache.getDroppedTrackable() != null){
            droppedTrackableCheckBox.setChecked(true);
            EditText droppedTrackableEditText = findViewById(R.id.droppedTrackableEditText);
            droppedTrackableEditText.setText(currentGeocache.getDroppedTrackable());
        }

        // 4. Favourite Point Checkbox
        CheckBox favouritePointCheckBox = findViewById(R.id.favouritePointCheckBox);
        if(this.currentGeocache.getFavouritePoint()) favouritePointCheckBox.setChecked(true);

        // Set my notes
        EditText notesSection = findViewById(R.id.notes);
        String myNotes = this.currentGeocache.getNotes();
        if(!myNotes.equals("")) notesSection.setText(myNotes);

        //  Setup ping sound
        setupAudio();

        // Setup photo
        CheckBox photoCheckBox = findViewById(R.id.photo_checkbox);
        if(currentGeocache.getPathToImage() != null){
            photoCheckBox.setChecked(true);
        } else {
            photoCheckBox.setChecked(false);
        }
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

        // Get notes
        EditText notesView = findViewById(R.id.notes);
        String myNotes = notesView.getText().toString();
        currentGeocache.setNotes(myNotes);

        // Get trackable information
        EditText foundTrackableEditText = findViewById(R.id.foundTrackableEditText);
        String foundTrackableString = foundTrackableEditText.getText().toString().trim();
        currentGeocache.setFoundTrackable(foundTrackableString.isEmpty() ? null : foundTrackableString);

        EditText droppedTrackableEditText = findViewById(R.id.droppedTrackableEditText);
        String droppedTrackableString = droppedTrackableEditText.getText().toString().trim();
        currentGeocache.setDroppedTrackable(droppedTrackableString.isEmpty() ? null : droppedTrackableString);

        // save changes
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
        EditText foundTrackableEditText = findViewById(R.id.foundTrackableEditText);

        if(foundTrackableCheckBox.isChecked()){

            // Focus attention on text box to fill in value
            foundTrackableEditText.requestFocus();

            //currentGeocache.setFoundTrackable(true);
        }

        else{
            currentGeocache.setFoundTrackable(null);

            // Delete inputted trackable code in editText area
            foundTrackableEditText.setText("");
        }

    }

    public void onDroppedTrackableCheckboxClicked(View view) {

        CheckBox droppedTrackableCheckBox = findViewById(R.id.droppedTrackableCheckBox);
        EditText droppedTrackableEditText = findViewById(R.id.droppedTrackableEditText);

        if(droppedTrackableCheckBox.isChecked()){
            //currentGeocache.setDroppedTrackable(true);

            // Focus attention on text box to fill in value
            droppedTrackableEditText.requestFocus();
        }

        else{
            currentGeocache.setDroppedTrackable(null);
            droppedTrackableEditText.setText("");
        }


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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "GEOCACHEID_" + currentGeocache.get_id() + "_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        currentGeocache.setPathToImage(currentPhotoPath);

        return image;
    }

    private void galleryAddPic() {
        //invoke the system's media scanner to add your photo to the Media Provider's database, making it available in the Android Gallery application and to other apps.
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

    }

    public void showPhotoPopup(View view){

        // If the current cache has no photo then go straight into the photo taking
        if(currentGeocache.getPathToImage() == null){
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
        switch (item.getItemId()) {
            case R.id.take_new_photo:
                takePhoto();
                return true;
            case R.id.show_cache_photo:
                showCachePhoto();
                return true;
            case R.id.delete_cache_photo:
                deleteCachePhoto();
                return true;
            default:
                return false;
        }
    }

    private void deleteCachePhoto() {
        File fdelete = new File(currentGeocache.getPathToImage());

        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" );
            } else {
                System.out.println("file not Deleted :");
            }
        }
        currentGeocache.setPathToImage(null);
        CheckBox photoCheckbox = findViewById(R.id.photo_checkbox);
        photoCheckbox.setChecked(false);
    }

/*  // Throws an exception because it exposes App files to the system
    void showCachePhoto(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + currentGeocache.getPathToImage()), "image/*");
        startActivity(intent);
    }
*/

    public void showCachePhoto() {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(dialogInterface -> {
            //nothing;
        });

        ImageView imageView = new ImageView(this);
        imageView.setImageURI(Uri.parse(currentGeocache.getPathToImage()));
        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }
}
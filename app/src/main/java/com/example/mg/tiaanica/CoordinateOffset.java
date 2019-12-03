package com.example.mg.tiaanica;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


public class CoordinateOffset extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Coordinate coordinate;
    LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            TextView txtLat = findViewById(R.id.coordinates);
            String fullCoordinates = "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude();
            txtLat.setText(fullCoordinates);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Latitude", "status");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Latitude", "enable");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Latitude", "disable");
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate_offset);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(infoFabListener);

        FloatingActionButton locationFab = findViewById(R.id.myLocationButton);
        locationFab.setAlpha(0.9f);
        locationFab.setOnClickListener(locationFabListener);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final EditText finalTextField = findViewById(R.id.distance);
        finalTextField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    compute(v); // parse the coordinate
                    return true;
                }
                return false;
            }
        });

        // Set background
        ConstraintLayout base_layout = findViewById(R.id.base_layout);
        Resources res = getResources();

        WindowManager window = (WindowManager)getSystemService(WINDOW_SERVICE);
        assert window != null;
        Display display = window.getDefaultDisplay();

        int num = display.getRotation();
        if (num == 0){
            base_layout.setBackgroundDrawable(res.getDrawable(R.drawable.portrait_background));
        }else if (num == 1 || num == 3){
            base_layout.setBackgroundDrawable(res.getDrawable(R.drawable.landscape_background));
        }else{
            base_layout.setBackgroundDrawable(res.getDrawable(R.drawable.portrait_background));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.coordinate_offset, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_alpha_sum) {
            Intent intent = new Intent(this, AlphaSum.class);
            startActivity(intent);
        } else if (id == R.id.nav_vigenere) {
            Intent intent = new Intent(this, VigenereCipher.class);
            startActivity(intent);
        } else if (id == R.id.nav_coord_calculator) {
            Intent intent = new Intent(this, CoordCalculator.class);
            startActivity(intent);
        } else if (id == R.id.nav_coord_offset) {
            Intent intent = new Intent(this, CoordinateOffset.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void compute(View view){

        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        assert mgr != null;
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        EditText coordinates = findViewById(R.id.coordinates);
        String initialCoordinatesString = coordinates.getText().toString();

        EditText angle = findViewById(R.id.angle);
        String angleString = angle.getText().toString();

        EditText distance = findViewById(R.id.distance);
        String distanceString = distance.getText().toString();

        if(initialCoordinatesString.equals("") || angleString.equals("") || distanceString.equals("")){

            AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateOffset.this);
            builder.setTitle("Error")
                    .setMessage("Please fill in all the values.")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

            builder.create().show();

            return;
        }

        double angleDeg;
        double distanceInMeters;

        try {
            angleDeg = Double.parseDouble(angleString);
            distanceInMeters = Double.parseDouble(distanceString);
        }
        catch(NumberFormatException e){

            AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateOffset.this);
            builder.setTitle("Error")
                    .setMessage("The angle and the distance should be numbers.")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

            builder.create().show();

            return;
        }

        coordinate = new Coordinate(initialCoordinatesString);
        coordinate.Offset(angleDeg, distanceInMeters);

        TextView result = findViewById(R.id.result);
        result.setVisibility(View.VISIBLE);
        String message = "The final coordinates are: \n" + coordinate.getFullCoordinates();
        result.setText(message);

    }

    void requestLocationAccessPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationAccessPermission();
            }
        }
    }

    void requestGPSEnable(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateOffset.this);
        builder.setMessage(R.string.gps_network_not_enabled);
        builder.setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                CoordinateOffset.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.Cancel,null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void locationNotAccessibleAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml("<b>Error</b>"));
        builder.setMessage("Sorry.. Can't access your location.");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    View.OnClickListener infoFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateOffset.this);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setTitle(R.string.help).setMessage(Html.fromHtml(getString(R.string.coord_offset_info)));

            // Add OK button
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });


            // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    View.OnClickListener locationFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            boolean locationAccessPermitted = ContextCompat.checkSelfPermission(CoordinateOffset.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if(!locationAccessPermitted) requestLocationAccessPermission();

            boolean gpsSignalAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!gpsSignalAvailable) requestGPSEnable();

            if (locationAccessPermitted && gpsSignalAvailable) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location == null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location == null){
                        locationNotAccessibleAlert();
                    }
                    else{
                        TextView txtLat = findViewById(R.id.coordinates);
                        coordinate = new Coordinate(location.getLatitude(), location.getLongitude());
                        txtLat.setText(coordinate.getFullCoordinates());

                        new AlertDialog.Builder(CoordinateOffset.this)
                                .setMessage("Used coordinates from Network signal.")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                }
                else{
                    TextView txtLat = findViewById(R.id.coordinates);
                    coordinate = new Coordinate(location.getLatitude(), location.getLongitude());
                    txtLat.setText(coordinate.getFullCoordinates());

                    new AlertDialog.Builder(CoordinateOffset.this)
                            .setMessage("Used coordinates from GPS signal.")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
                }
            }

        }
    };
}

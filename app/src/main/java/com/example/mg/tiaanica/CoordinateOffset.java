package com.example.mg.tiaanica;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Html;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoordinateOffset extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Coordinate coordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate_offset);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateOffset.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.help).setMessage(Html.fromHtml("<b><i>Coordinate Offset: </i></b>" + getString(R.string.coord_offset_info), Html.FROM_HTML_MODE_LEGACY));

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
        });

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

    @SuppressWarnings("StatementWithEmptyBody")
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

        EditText latitude = findViewById(R.id.latitude);
        String initialLatitudeString = latitude.getText().toString();

        EditText longitude = findViewById(R.id.longitude);
        String initialLongitudeString = longitude.getText().toString();

        EditText angle = findViewById(R.id.angle);
        String angleString = angle.getText().toString();

        EditText distance = findViewById(R.id.distance);
        String distanceString = distance.getText().toString();

        if(initialLatitudeString.equals("") || initialLongitudeString.equals("") || angleString.equals("") || distanceString.equals("")){

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

        coordinate = new Coordinate(initialLatitudeString, initialLongitudeString);
        coordinate.Offset(angleDeg, distanceInMeters);

        TextView result = findViewById(R.id.result);
        result.setVisibility(View.VISIBLE);
        String message = "The final coordinates are: \n" + coordinate.getFinalCoordinates();
        result.setText(message);

    }

}

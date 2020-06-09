package com.example.mg.tiaanica;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Html;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.HashMap;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;

public class CoordinateFormulaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DoneWithInput {

    CoordinateFormula coordinate;
    String originalCoordinate;

    LetterInputAdapter letterInputAdapter;
    HashMap<String, Double> variableValues = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coord_calculator);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set version number in navigation drawer
        TextView version = findViewById(R.id.version);
        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionStr = "Version: " + versionName;
        version.setText(versionStr);

        // Setup navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // When pushing enter on the keyboard on the formula text field, it parses the coordinates right away (as if pushing the enter button)
        final EditText formula = findViewById(R.id.formula);
        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        formula.setImeOptions(IME_ACTION_GO);
        formula.setOnEditorActionListener((view, actionId, event) -> {
            // If the event is a key-down event on the "enter" button
            if (actionId == EditorInfo.IME_ACTION_GO) {
                // Perform action on key press
                parseCoordFormula(view); // parse the coordinate
                if (mgr != null) mgr.hideSoftInputFromWindow(formula.getWindowToken(), 0);// make the keyboard disappear
                return true;
            }
            return false;
        });

        //variables = new HashMap<>();
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
        getMenuInflater().inflate(R.menu.toolbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            getHelp();
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
            Intent intent = new Intent(this, AlphaSumActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_vigenere) {
            Intent intent = new Intent(this, VigenereCipherActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_coord_calculator) {
            Intent intent = new Intent(this, CoordinateFormulaActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_coord_offset) {
            Intent intent = new Intent(this, CoordinateOffsetActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void parseCoordFormula(View view) {

        // Keyboard manager -- allows keyboard to disappear
        final InputMethodManager mgr;
        mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assert mgr != null;
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        EditText editText = findViewById(R.id.formula);
        String coord = editText.getText().toString();
        originalCoordinate = coord;
        coordinate = new CoordinateFormula(coord);

        if (!coordinate.successfulParsing) {
            if (coordinate.Es != 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);
                builder.setTitle("Error")
                        .setMessage("The letter E shows up both in the formula and as a cardinal direction. This means the app can't separate the latitude and longitude in the formula. Please replace this for another letter in the formula.")
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());

                builder.create().show();
                return;
            }

            if (coordinate.Ws != 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);
                builder.setTitle("Error")
                        .setMessage("The letter W shows up both in the formula and as a cardinal direction. This means the app can't separate the latitude and longitude in the formula. Please replace this for another letter in the formula.")
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());

                builder.create().show();
                return;
            }
        }

        TextView textView = findViewById(R.id.NeededLetters);
        textView.setVisibility(View.VISIBLE);

        String message = "The required variables are: " + coordinate.getNeededVariables() +
                "\n\nPlease fill in the value for each variable.";
        textView.setText(message);


        // Setup area to input letter values
        RecyclerView neededLetterInputView = findViewById(R.id.letter_inputs);
        neededLetterInputView.setVisibility(View.VISIBLE);

        // Compute number of columns to use:
        // TODO:  make column width not show up out the blue
        int noColumns = calculateNoOfColumns(this, 100);
        neededLetterInputView.setLayoutManager(new GridLayoutManager(this, noColumns));

        letterInputAdapter = new LetterInputAdapter(coordinate.neededLetters, variableValues, this);
        neededLetterInputView.setAdapter(letterInputAdapter);


        Button compute = findViewById(R.id.button);
        compute.setVisibility(View.VISIBLE);

    }

    public void computeCoordinates(View view){

        computeCoordinates();
    }

    public void computeCoordinates(){

        // Hide keyboard -- https://medium.com/@rmirabelle/close-hide-the-soft-keyboard-in-android-db1da22b09d2
        Activity activity = this;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


        coordinate = new CoordinateFormula(originalCoordinate);

        if(variableValues.size() != coordinate.neededLetters.size()){
            AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);
            builder.setTitle("Error")
                    .setMessage("Please fill in the values for all the variables.")
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());

            builder.create().show();
            return;
        }

        coordinate.setVariables(variableValues);
        coordinate.evaluate();

        TextView result = findViewById(R.id.result);
        result.setVisibility(View.VISIBLE);

        boolean resultAreProperCoordinates = coordinate.resultAreProperCoordinates();

        String resultString;
        if(resultAreProperCoordinates) {
            resultString = "The final coordinates are:\n" + coordinate.getFullCoordinates();

            FloatingActionButton directionsFab = findViewById(R.id.direction);
            directionsFab.setOnClickListener(directionsFabListener);
            directionsFab.show();
        } else {
            resultString = "Result is: " + coordinate.getFullCoordinates();
        }

        result.setText(resultString);

        // Scroll down to view result
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.scrollTo(0, scrollView.getBottom());
        } else {
            NestedScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.scrollTo(0, scrollView.getBottom());
        }
    }

    View.OnClickListener directionsFabListener = (view) -> {

                //Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                //        Uri.parse("https://www.google.com/maps/dir/?api=1?destination=" + coordinate.getLatitude() + "," + coordinate.getLongitude()));
                //startActivity(intent);

                // Create a Uri from an intent string. Use the result to create an Intent.
                //Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%f,0%f", coordinate.getLatitude(), coordinate.getLongitude()));
                Coordinate c = new Coordinate(coordinate.getLatDir() + coordinate.getLatitude(), coordinate.getLonDir() + coordinate.getLongitude());
                @SuppressLint("DefaultLocale") Uri gmmIntentUri = Uri.parse(String.format("geo:0,0?q=%f,0%f", c.getLatitude(), c.getLongitude()));

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                // Make the Intent explicit by setting the Google Maps package
                // If this is not set the user will be asked to choose between available apps
                //mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);
            };


    void getHelp(){
        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this,  R.style.MyAlertDialogTheme);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.help).setMessage(Html.fromHtml(getString(R.string.coord_calculator_info)));

        // Add OK button
        builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel());


        // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public int calculateNoOfColumns(Context context, float columnWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / columnWidthDp + 0.5);
    }

    @Override
    public void doneWithInput() {
        computeCoordinates();
    }
}

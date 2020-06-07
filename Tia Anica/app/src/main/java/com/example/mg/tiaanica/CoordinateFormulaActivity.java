package com.example.mg.tiaanica;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.generateViewId;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;

public class CoordinateFormulaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    CoordinateFormula coordinate;
    String originalCoordinate;
    HashMap<String, Double> variables;
    ConstraintLayout constraintLayout;

    HashMap<String, Integer> neededLetterIds = new HashMap<>();

    int orientation;

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

        //FloatingActionButton fab = findViewById(R.id.fab);
        //fab.setOnClickListener((view) -> getHelp());

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

        constraintLayout = findViewById(R.id.constraintLayout);
        variables = new HashMap<>();
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

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

        int nNeededLetters = coordinate.neededLetters.size();
        int maxColumns;

        // In landscape mode put 5 fields in each row
        if (orientation == 0) maxColumns = 4;
        else if (orientation == 1 || orientation == 3) maxColumns = 6;
        else maxColumns = 4;


        LinearLayout letterInputLayout = findViewById(R.id.letter_inputs);
        letterInputLayout.removeAllViews();
        letterInputLayout.setVisibility(View.VISIBLE);

        // Get layout for letter inputs and inflate it
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout horizontalLine = (LinearLayout) inflater.inflate(R.layout.layout_letter_input, null, false);


        int i= 0;
        int column = 0;

        while(i < nNeededLetters) {

            String currentLetter = coordinate.neededLetters.get(i);

            // This will allow us to get the values later on
            int letterId = generateViewId();
            neededLetterIds.put(currentLetter, letterId);

            // If we've the maximum number of letters per line, add the last line to the Vew and reset it to a new line
            if(column == maxColumns){

                letterInputLayout.addView(horizontalLine);

                // Get a new line
                horizontalLine = (LinearLayout) inflater.inflate(R.layout.layout_letter_input, null, false);

                column = 0;
            }


            TextView letterTag = null;
            EditText letterInputArea = null;

            switch (column){
                case(0):
                    letterTag  = horizontalLine.findViewById(R.id.letterTag0);
                    letterInputArea = horizontalLine.findViewById(R.id.letter0);
                    break;
                case (1):
                    letterTag  = horizontalLine.findViewById(R.id.letterTag1);
                    letterInputArea = horizontalLine.findViewById(R.id.letter1);
                    break;
                case(2):
                    letterTag  = horizontalLine.findViewById(R.id.letterTag2);
                    letterInputArea = horizontalLine.findViewById(R.id.letter2);
                    break;
                case(3):
                    letterTag  = horizontalLine.findViewById(R.id.letterTag3);
                    letterInputArea = horizontalLine.findViewById(R.id.letter3);
                    break;
                case(4):
                    letterTag  = horizontalLine.findViewById(R.id.letterTag4);
                    letterInputArea = horizontalLine.findViewById(R.id.letter4);
                    break;
                case(5):
                    letterTag  = horizontalLine.findViewById(R.id.letterTag5);
                    letterInputArea = horizontalLine.findViewById(R.id.letter5);
                    break;
            }


            letterTag.setText(currentLetter);
            letterInputArea.setVisibility(View.VISIBLE);
            letterInputArea.setId(letterId);
            letterInputArea.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

            if (i == nNeededLetters - 1) {
                letterInputArea.setImeOptions(IME_ACTION_DONE);
                letterInputArea.setOnEditorActionListener((view1, actionId, event) -> {
                    // If the event is a key-down event on the "enter" button
                    if (actionId == IME_ACTION_DONE) {
                        // Perform action on key press
                        computeCoordinates(view1); // parse the coordinate
                        mgr.hideSoftInputFromWindow(view1.getWindowToken(), 0); // make the keyboard disappear
                        return true;
                    }
                    return false;
                });
            } else {
                letterInputArea.setImeOptions(IME_ACTION_NEXT);
            }


            column++;
            i++;
        }

        // Add the final one
        letterInputLayout.addView(horizontalLine);

        Button compute = findViewById(R.id.button);
        compute.setVisibility(View.VISIBLE);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void computeCoordinates(View view){

        // Keyboard manager -- allows keyboard to disappear
        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        coordinate = new CoordinateFormula(originalCoordinate);

        assert mgr != null;
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        for (Map.Entry<String, Integer> entry : neededLetterIds.entrySet()) {

            String letter = entry.getKey();
            Integer id = entry.getValue();

            TextView valueField = findViewById(id);
            String valueString = valueField.getText().toString();

            if(valueString.equals("")){

                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);
                builder.setTitle("Error")
                        .setMessage("Please fill in the values for all the variables.")
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.cancel());

                builder.create().show();
                return;
            }

            double value = Double.parseDouble(valueString);
            variables.put(letter, value);
        }

        coordinate.setVariables(variables);
        coordinate.evaluate();

        TextView result = findViewById(R.id.result);
        result.setVisibility(View.VISIBLE);

        boolean resultAreProperCoordinates = coordinate.resultAreProperCoordinates();

        String resultString;
        if(resultAreProperCoordinates) {
            resultString = "The final coordinates are:\n" + coordinate.getFullCoordinates();

            FloatingActionButton directionsFab = findViewById(R.id.direction);
            directionsFab.setOnClickListener(directionsFabListener);
            //directionsFab.setVisibility(View.VISIBLE);
            directionsFab.show();
        } else {
            resultString = "Result is: " + coordinate.getFullCoordinates();
        }

        result.setText(resultString);

    }

    View.OnClickListener directionsFabListener;

    {
        directionsFabListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
            }
        };
    }

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
}

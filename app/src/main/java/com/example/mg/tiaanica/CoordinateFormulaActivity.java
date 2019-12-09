package com.example.mg.tiaanica;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.KeyEvent;
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
import android.content.res.Resources;

import java.util.HashMap;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;

public class CoordinateFormulaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    CoordinateFormula coordinate;
    String originalCoordinate;
    HashMap<String, Integer> variables;
    LinearLayout resultAndVariableLayout;
    LinearLayout resultLine;
    int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coord_calculator);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.help).setMessage(Html.fromHtml(getString(R.string.coord_calculator_info)));

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

        // When pushing enter on the keyboard on the formula text field, it parses the coordinates right away (as if pushing the enter button)
        final EditText formula = findViewById(R.id.formula);
        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        formula.setImeOptions(IME_ACTION_GO);
        formula.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event){
                // If the event is a key-down event on the "enter" button
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    // Perform action on key press
                    parseCoordFormula(view); // parse the coordinate
                    if (mgr != null) mgr.hideSoftInputFromWindow(formula.getWindowToken(), 0);// make the keyboard disappear
                    return true;
                }
                return false;
            }
        });

        // Set background
        ConstraintLayout base_layout = findViewById(R.id.base_layout);
        // ScrollView base_layout = findViewById(R.id.scroll_layout);
        Resources res = getResources();

        WindowManager window = (WindowManager)getSystemService(WINDOW_SERVICE);
        assert window != null;
        Display display = window.getDefaultDisplay();

        orientation = display.getRotation();
        if (orientation == 0){
            base_layout.setBackground(res.getDrawable(R.drawable.portrait_background));
        }else if (orientation == 1 || orientation == 3){
            base_layout.setBackground(res.getDrawable(R.drawable.landscape_background));
        }else{
            base_layout.setBackground(res.getDrawable(R.drawable.portrait_background));
        }

        resultAndVariableLayout = findViewById(R.id.resultAndVariableLayout);
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
        getMenuInflater().inflate(R.menu.coord_calculator, menu);
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

        resultAndVariableLayout.removeAllViews();

        if (!coordinate.successfulParsing) {
            if (coordinate.Es != 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);
                builder.setTitle("Error")
                        .setMessage("The letter E shows up both in the formula and as a cardinal direction. This means the app can't separate the latitude and longitude in the formula. Please replace this for another letter in the formula.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                builder.create().show();
                return;
            }

            if (coordinate.Ws != 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);
                builder.setTitle("Error")
                        .setMessage("The letter W shows up both in the formula and as a cardinal direction. This means the app can't separate the latitude and longitude in the formula. Please replace this for another letter in the formula.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                builder.create().show();
                return;
            }
        }

        TextView textView = findViewById(R.id.textView2);
        textView.setVisibility(View.VISIBLE);

        String message = "The required variables are: " + coordinate.getNeededVariables();
        textView.setText(message);

        TextView inputSentence = new TextView(this);
        String inputString = "Input the values for each variable.";
        inputSentence.setText(inputString);
        inputSentence.setTextSize(18);
        inputSentence.setTextColor(getResources().getColor(R.color.gray));

        resultAndVariableLayout.addView(inputSentence);
        int nNeededLetters = coordinate.neededLetters.size();
        int columns;

        // In landscape mode put 5 fields in each row
        if (orientation == 0) columns = 3;
        else if (orientation == 1 || orientation == 3) columns = 5;
        else columns = 3;

        TextView temp;
        EditText tempValue;
        TextView blankSpace;

        LinearLayout horizontalLine = new LinearLayout(this);
        horizontalLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalLine.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLine.setAlpha(0.9f);
        horizontalLine.setBackground(this.getDrawable(R.drawable.text_field));
        horizontalLine.setPadding(32,32,32,32);

        int i= 0;
        int c = 0;

        while(i < nNeededLetters) {

            String currentLetter = coordinate.neededLetters.get(i);

            if (c == columns) {
                c = 0;

                // Add this line to the view and get a new one
                resultAndVariableLayout.addView(horizontalLine);

                horizontalLine = new LinearLayout(this);
                horizontalLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                horizontalLine.setOrientation(LinearLayout.HORIZONTAL);
                horizontalLine.setAlpha(0.9f);
                horizontalLine.setBackground(this.getDrawable(R.drawable.text_field));
                horizontalLine.setPadding(32,32,32,32);
            }

            temp = new TextView(this);
            temp.setText(currentLetter);
            temp.setTextSize(18);
            temp.setWidth(80);
            temp.setTextColor(getResources().getColor(R.color.gray));

            blankSpace = new TextView(this);
            blankSpace.setWidth(100);

            tempValue = new EditText(this);
            tempValue.setInputType(3);
            tempValue.setWidth(150);
            tempValue.setId(i);
            tempValue.setTextColor(getResources().getColor(R.color.gray));
            if(variables.keySet().contains(currentLetter)){
                int keyValue = variables.get(currentLetter);
                tempValue.setText(Integer.toString(keyValue));
            }

            // If we are inputting the value of the last coordinate compute the result and hide the keyboard
            if (i == nNeededLetters - 1) {
                tempValue.setImeOptions(IME_ACTION_DONE);
                tempValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if (actionId == IME_ACTION_DONE) {
                            // Perform action on key press
                            computeCoordinates(view); // parse the coordinate
                            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0); // make the keyboard disappear
                            return true;
                        }
                        return false;
                    }
                });
            } else {
                tempValue.setImeOptions(IME_ACTION_NEXT);
            }

            horizontalLine.addView(temp);
            horizontalLine.addView(tempValue);
            horizontalLine.addView(blankSpace);

            i++;c++;

        }

        // Add the final one
        resultAndVariableLayout.addView(horizontalLine);

        Button compute = new Button(this);
        compute.setText(R.string.compute);
        compute.setRight(0);
        compute.setOnClickListener(new View.OnClickListener(){ public void onClick(View view) { computeCoordinates(view);}});
        compute.setBackgroundResource(R.drawable.bt_style);
        compute.setTextColor(Color.parseColor("#ffffff"));
        compute.setTypeface(null, Typeface.BOLD);
        compute.setTextSize(15);
        compute.setWidth((int) getResources().getDimension(R.dimen.one_third_width));

        resultAndVariableLayout.addView(compute);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void computeCoordinates(View view){

        // Keyboard manager -- allows keyboard to disappear
        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        coordinate = new CoordinateFormula(originalCoordinate);
        resultAndVariableLayout.removeView(resultLine);

        assert mgr != null;
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        for(int i = 0; i < coordinate.neededLetters.size(); i++){

            int id = CoordinateFormulaActivity.this.getResources().getIdentifier(
                    String.valueOf(i),
                    "id",
                    CoordinateFormulaActivity.this.getPackageName()
            );

            TextView valueField = findViewById(id);
            String valueString = valueField.getText().toString();

            if(valueString.equals("")){

                AlertDialog.Builder builder = new AlertDialog.Builder(CoordinateFormulaActivity.this);
                builder.setTitle("Error")
                        .setMessage("Please fill in the values for all the variables.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                builder.create().show();
                return;
            }

            int value = Integer.parseInt(valueString);
            variables.put(coordinate.neededLetters.get(i), value);
        }

        coordinate.setVariables(variables);
        coordinate.evaluate();

        resultLine = new LinearLayout(this);
        resultLine.setOrientation(LinearLayout.HORIZONTAL);
        resultLine.setAlpha(0.9f);
        resultLine.setBackground(this.getDrawable(R.drawable.text_field));
        resultLine.setPadding(8, 8, 8, 8);

        TextView result = new TextView(this);
        String resultString = "The final coordinates are:\n" + coordinate.getFullCoordinates();

        result.setText(resultString);
        result.setTextSize(18);
        result.setTextColor(getResources().getColor(R.color.gray));
        result.setTextIsSelectable(true);
        resultLine.addView(result);

        FloatingActionButton directionsFab = new FloatingActionButton(this);
        directionsFab.setImageResource(R.drawable.ic_directions_black_24dp);
        directionsFab.setSize(android.support.design.widget.FloatingActionButton.SIZE_MINI);
        directionsFab.setFocusable(true);
        directionsFab.setOnClickListener(directionsFabListener);

        RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lay.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        lay.setMargins(2,2,2,2);
        directionsFab.setLayoutParams(lay);
        resultLine.addView(directionsFab);

        resultAndVariableLayout.addView(resultLine);
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
                Uri gmmIntentUri = Uri.parse(String.format("geo:0,0?q=%f,0%f", c.getLatitude(), c.getLongitude()));

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
}

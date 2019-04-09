package com.example.mg.tiaanica;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;
import static android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED;

public class CoordCalculator extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String coord;
    List<String> neededLetters = new ArrayList<>();
    Map<String, Integer> variables = new HashMap<>();
    int lastRowUsed;
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
                AlertDialog.Builder builder = new AlertDialog.Builder(CoordCalculator.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.help).setMessage(Html.fromHtml("<b><i>Coordinate Calculator: </i></b>" + getString(R.string.coord_calculator_info), Html.FROM_HTML_MODE_LEGACY));

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
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event){
                // If the event is a key-down event on the "enter" button
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    // Perform action on key press
                    parseCoordFormula(view); // parse the coordinate
                    mgr.hideSoftInputFromWindow(formula.getWindowToken(), 0);// make the keyboard disappear
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
            base_layout.setBackgroundDrawable(res.getDrawable(R.drawable.portrait_background));
        }else if (orientation == 1 || orientation == 3){
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void parseCoordFormula(View view){

        // Keyboard manager -- allows keyboard to disappear
        final InputMethodManager mgr;
        mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        assert mgr != null;
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        LinearLayout row0 = findViewById(R.id.row0);
        row0.removeAllViews();
        LinearLayout row1 = findViewById(R.id.row1);
        row1.removeAllViews();
        LinearLayout row2 = findViewById(R.id.row2);
        row2.removeAllViews();
        LinearLayout row3 = findViewById(R.id.row3);
        row3.removeAllViews();
        LinearLayout row4 = findViewById(R.id.row4);
        row4.removeAllViews();
        LinearLayout row5 = findViewById(R.id.row5);
        row5.removeAllViews();
        LinearLayout row6 = findViewById(R.id.row6);
        row6.removeAllViews();
        LinearLayout row7 = findViewById(R.id.row7);
        row7.removeAllViews();

        EditText editText = findViewById(R.id.formula);
        String coord = editText.getText().toString();

        coord = coord.toUpperCase();
        coord = coord.replaceAll(" ", "");
        coord = coord.replaceAll("÷", "/");


        // Obtain required letters for solution. For now, N, S, W and E are not allowed
        List<String> neededLetters = new ArrayList<>();

        Pattern p = Pattern.compile("[A-Z&&[^NSEW]]");
        Matcher m = p.matcher(coord);
        while (m.find())
            if(!neededLetters.contains(m.group()))
                neededLetters.add(m.group());

        this.coord = coord;
        this.neededLetters = neededLetters;
        Collections.sort(this.neededLetters);

        String list = neededLetters.toString();

        TextView textView = findViewById(R.id.textView2);
        textView.setVisibility(View.VISIBLE);
        String message = "The required variables are: " + list.substring(1, list.length() - 1);
        textView.setText(message);

        TextView inputSentence = new TextView(this);
        String inputString = "Input the values for each variable.";
        inputSentence.setText(inputString);
        inputSentence.setTextSize(18);
        inputSentence.setTextColor(getResources().getColor(R.color.gray));
        row0.setVisibility(View.VISIBLE);
        row0.addView(inputSentence);


        int total = neededLetters.size();
        int columns;

        // In landscape mode put 5 fields in each row
        if (orientation == 0) columns = 3;
        else if (orientation == 1 || orientation == 3) columns = 5;
        else columns = 3;

        TextView temp;
        EditText tempValue;
        TextView blankSpace;
        int r = 1;

        for(int i = 0, c = 0; i < total; i++, c++){

            if (c == columns) {
                c = 0;
                r++;
            }

            temp = new TextView(this);
            temp.setText(neededLetters.get(i));
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

            // If we are inputting the value of the last coordinate compute the result and hide the keyboard
            if(i == total - 1){
                tempValue.setImeOptions(IME_ACTION_DONE);
                tempValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            // Perform action on key press
                            computeCoordinates(view); // parse the coordinate
                            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0); // make the keyboard disappear
                            return true;
                        }
                        return false;
                    }
                });
            }
            else tempValue.setImeOptions(IME_ACTION_NEXT);

            if(r == 1){
                row1.setVisibility(View.VISIBLE);
                row1.addView(temp);
                row1.addView(tempValue);
                row1.addView(blankSpace);
            }
            else if (r == 2){
                row2.setVisibility(View.VISIBLE);
                row2.addView(temp);
                row2.addView(tempValue);
                row2.addView(blankSpace);
            }
            else if(r == 3) {
                row3.setVisibility(View.VISIBLE);
                row3.addView(temp);
                row3.addView(tempValue);
                row3.addView(blankSpace);
            }
            else if(r == 4) {
                row4.setVisibility(View.VISIBLE);
                row4.addView(temp);
                row4.addView(tempValue);
                row4.addView(blankSpace);
            }
            else if(r == 5) {
                row5.setVisibility(View.VISIBLE);
                row5.addView(temp);
                row5.addView(tempValue);
                row5.addView(blankSpace);
            }
            else if(r == 6) {
                row6.setVisibility(View.VISIBLE);
                row6.addView(temp);
                row6.addView(tempValue);
                row6.addView(blankSpace);
            }
            else {
                row7.setVisibility(View.VISIBLE);
                row7.addView(temp);
                row7.addView(tempValue);
                row7.addView(blankSpace);
            }
        }

        // For each of the input fields we want to move on to the next one when pushing the enter button on the keyboard
        for(int i = 0; i < total - 1; i++){

            int id = CoordCalculator.this.getResources().getIdentifier(
                    String.valueOf(i + 1),
                    "id",
                    CoordCalculator.this.getPackageName()
            );
            final TextView valueField = findViewById(id);

            int nextId = CoordCalculator.this.getResources().getIdentifier(
                    String.valueOf(i + 1),
                    "id",
                    CoordCalculator.this.getPackageName()
            );

            final TextView nextValueField = findViewById(nextId);
            valueField.setOnKeyListener(new View.OnKeyListener() {
                                            public boolean onKey(View view, int keyCode, KeyEvent event){

                    if ((event.getAction() == KeyEvent.ACTION_DOWN)
                            && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        nextValueField.requestFocus();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        Button compute = new Button(this);
        compute.setText(R.string.compute);
        compute.setRight(0);
        compute.setOnClickListener(new View.OnClickListener(){ public void onClick(View view) { computeCoordinates(view);}});
        compute.setBackgroundResource(R.drawable.bt_style);
        compute.setTextColor(Color.parseColor("#ffffff"));
        compute.setTypeface(null, Typeface.BOLD);
        compute.setTextSize(15);

        r++;

        if (r == 2){ row2.setVisibility(View.VISIBLE); row2.addView(compute);}
        else if(r == 3) { row3.setVisibility(View.VISIBLE); row3.addView(compute);}
        else if(r == 4) { row4.setVisibility(View.VISIBLE); row4.addView(compute);}
        else if(r == 5) { row5.setVisibility(View.VISIBLE); row5.addView(compute);}
        else { row6.setVisibility(View.VISIBLE); row6.addView(compute);}

        lastRowUsed = r;

    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        default:
                            throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static String tokenize(String coord) {

        StringBuilder tokenizedCoord = new StringBuilder();
        String[] parts = coord.split("");
        int maxLength = coord.length() + 1;

        int i = 0;
        String currentChar;
        String nextChar;
        String nextToNextChar;
        StringBuilder toAdd;

        while(i < maxLength) {

            currentChar = parts[i];
            toAdd = new StringBuilder();

            if(currentChar.equals("(")) {

                toAdd.append("(");

                i += 1;
                currentChar = parts[i];
                toAdd.append(currentChar);

                while(!currentChar.equals(")")){
                    i += 1;
                    currentChar = parts[i];
                    toAdd.append(currentChar);
                }
            }
            else if(currentChar.matches("[0-9\\.°NSEW]")){
                toAdd = new StringBuilder(currentChar);
            }
            else if(currentChar.matches("[A-Z]")) {

                toAdd.append("(");
                toAdd.append(currentChar);

                if(i + 2 < maxLength) {

                    nextChar = parts[i+1];
                    nextToNextChar = parts[i+2]; //Check for length

                    while(i + 2 < maxLength && nextChar.matches("[+\\-\\/\\*÷]") && nextToNextChar.matches("[A-Z0-9]")) {

                        toAdd.append(nextChar);
                        toAdd.append(nextToNextChar);

                        if(nextToNextChar.matches("[0-9]")) { // if it is a number with more than one digit we need to add them all

                            int j = i + 3;

                            if(j < maxLength) {

                                String theOneAfterThat = parts[j];
                                toAdd.append(theOneAfterThat);
                                j += 1;

                                while(j < maxLength  && theOneAfterThat.matches("[0-9]")) {
                                    theOneAfterThat = parts[j];
                                    toAdd.append(theOneAfterThat);
                                    j += 1;
                                }
                            }
                            i = j - 3;
                        }

                        i += 2;

                        if(i + 2 < maxLength) {
                            nextChar = parts[i+1];
                            nextToNextChar = parts[i+2];
                        }

                    }
                }

                toAdd.append(")");
            }

            tokenizedCoord.append(toAdd);

            i += 1;
        }

        return tokenizedCoord.toString();

    }

    public String evaluate(String tokenizedCoord){

        for (Map.Entry<String, Integer> pair : variables.entrySet()) {
            String key = pair.getKey();
            String value = Integer.toString(pair.getValue());
            tokenizedCoord = tokenizedCoord.replaceAll(key, value);
        }


        Pattern token = Pattern.compile("\\((.*?)\\)");
        Matcher latTokens = token.matcher(tokenizedCoord);
        while (latTokens.find()) {
            String toEval = latTokens.group(1);
            tokenizedCoord = tokenizedCoord.replace(latTokens.group(), Integer.toString((int) eval(toEval)));
        }

        return tokenizedCoord;
    }

    public void computeCoordinates(View view){

        // Keyboard manager -- allows keyboard to disappear
        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        assert mgr != null;
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        String tokenizedCoord = tokenize(coord);

        for(int i = 0; i < this.neededLetters.size(); i++){

            int id = CoordCalculator.this.getResources().getIdentifier(
                    String.valueOf(i),
                    "id",
                    CoordCalculator.this.getPackageName()
            );

            TextView valueField = findViewById(id);
            String valueString = valueField.getText().toString();

            if(valueString.equals("")){

                AlertDialog.Builder builder = new AlertDialog.Builder(CoordCalculator.this);
                builder.setTitle("Error")
                        .setMessage("Please fill in the values for all the variables. If the letters N, S, E or W are used as variables, please replace them with other letters.")
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
            this.variables.put(neededLetters.get(i), value);
        }

        tokenizedCoord = evaluate(tokenizedCoord);
        LinearLayout resultSpace;

        if(lastRowUsed == 1) resultSpace = findViewById(R.id.row2);
        else if(lastRowUsed == 2) resultSpace = findViewById(R.id.row3);
        else if(lastRowUsed == 3) resultSpace = findViewById(R.id.row4);
        else if(lastRowUsed == 4) resultSpace = findViewById(R.id.row5);
        else if(lastRowUsed == 5) resultSpace = findViewById(R.id.row6);
        else resultSpace = findViewById(R.id.row7);

        resultSpace.setVisibility(View.VISIBLE);
        resultSpace.removeAllViews();
        TextView result = new TextView(this);
        String resultString = "The final coordinates are " + tokenizedCoord;
        result.setText(resultString);
        result.setTextSize(18);
        result.setTextColor(getResources().getColor(R.color.gray));
        result.setTextIsSelectable(true);

        resultSpace.addView(result);

    }
}

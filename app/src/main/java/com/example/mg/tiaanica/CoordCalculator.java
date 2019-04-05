package com.example.mg.tiaanica;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.content.res.Resources;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CoordCalculator extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String coord;
    List<String> neededLetters = new ArrayList<String>();
    Map<String, Integer> variables = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coord_calculator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
    public boolean onNavigationItemSelected(MenuItem item) {
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void parseCoordFormula(View view){

        // Intent intent = new Intent(this, DisplayCoordVariables.class);

        LinearLayout linearLayout = findViewById(R.id.linearLayout);
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

        EditText editText = findViewById(R.id.formula);
        String coord = editText.getText().toString();

        coord = coord.toUpperCase();
        coord = coord.replaceAll(" ", "");
        coord = coord.replaceAll("÷", "/");


        // Obtain required letters for solution. For now, N, S, W and E are not allowed
        List<String> neededLetters = new ArrayList<String>();

        Pattern p = Pattern.compile("[A-Z&&[^NSEW]]");
        Matcher m = p.matcher(coord);
        while (m.find())
            if(!neededLetters.contains(m.group()))
                neededLetters.add(m.group());

        this.coord = coord;
        this.neededLetters = neededLetters;

        String list = neededLetters.toString();

        TextView textView = findViewById(R.id.textView2);
        textView.setText("The required variables are: " + list.substring(1, list.length() - 1));

        TextView inputSentence = new TextView(this);
        inputSentence.setText("Input the values for each variable.");
        inputSentence.setTextSize(18);
        inputSentence.setTextColor(Color.BLACK);
        row0.addView(inputSentence);


        int total = neededLetters.size();
        int columns = 3;
        int rows = total / columns;

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
            temp.setTextColor(Color.BLACK);

            blankSpace = new TextView(this);
            blankSpace.setWidth(100);

            tempValue = new EditText(this);
            tempValue.setInputType(3);
            tempValue.setWidth(150);
            tempValue.setId(i);

            if(r == 1){
                row1.addView(temp);
                row1.addView(tempValue);
                row1.addView(blankSpace);
            }
            else if (r == 2){
                row2.addView(temp);
                row2.addView(tempValue);
                row2.addView(blankSpace);
            }
            else if(r == 3) {
                row3.addView(temp);
                row3.addView(tempValue);
                row3.addView(blankSpace);
            }
            else if(r == 4) {
                row4.addView(temp);
                row4.addView(tempValue);
                row4.addView(blankSpace);
            }
            else {
                row5.addView(temp);
                row5.addView(tempValue);
                row5.addView(blankSpace);
            }
        }

        Button compute = new Button(this);
        compute.setText("Compute");
        compute.setRight(0);
        compute.setOnClickListener(new View.OnClickListener(){ public void onClick(View view) { computeCoordinates(view);}});
        compute.setBackgroundResource(R.drawable.bt_style);
        compute.setTextColor(Color.parseColor("#ffffff"));
        compute.setTypeface(null, Typeface.BOLD);

        r++;

        if (r == 2) row2.addView(compute);
        else if(r == 3) row3.addView(compute);
        else if(r == 4) row4.addView(compute);
        else row5.addView(compute);

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
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static String tokenize(String coord) {

        String tokenizedCoord = "";
        String[] parts = coord.split("");

        int i = 0;
        String currentChar;
        String nextChar;
        String nextToNextChar;
        String toAdd;

        while(i < coord.length() + 1) {

            currentChar = parts[i];
            toAdd = "";

            if(currentChar.equals("(")) {

                toAdd += "(";

                i += 1;
                currentChar = parts[i];

                while(!currentChar.equals(")")){
                    toAdd += currentChar;
                    i += 1;
                    currentChar = parts[i];
                }

                toAdd += ")";
            }
            else if(currentChar.matches("[0-9\\.°NSEW]")){
                toAdd = currentChar;
            }
            else if(currentChar.matches("[A-Z]")) {

                toAdd += "(";
                toAdd += currentChar;

                if(i + 1 < coord.length()) {

                    nextChar = parts[i+1];
                    nextToNextChar = parts[i+2]; //Check for length

                    while(nextChar.matches("[+\\-\\/\\*÷]") & nextToNextChar.matches("[A-Z0-9]") & i + 1 < coord.length()) {

                        nextChar = parts[i+1];
                        nextToNextChar = parts[i+2];

                        toAdd += nextChar;
                        toAdd += nextToNextChar;

                        if(nextToNextChar.matches("[0-9]")) { // if it is a number with more than one digit we need to add them all

                            int j = i + 3;

                            if(j < coord.length()) {
                                String theOneAfterThat = parts[j];

                                while(theOneAfterThat.matches("[0-9]")) {
                                    toAdd += theOneAfterThat;
                                    j += 1;
                                    theOneAfterThat = parts[j];
                                }
                            }
                            i = j - 1;
                        }

                        if(i + 1 < coord.length()) {
                            nextChar = parts[i+1];
                            nextToNextChar = parts[i+2];
                        }
                        else break;
                    }
                }

                toAdd += ")";
            }

            tokenizedCoord += toAdd;

            i += 1;
        }

        return tokenizedCoord;

    }

    public void computeCoordinates(View view){

        int lonInd = 0;

        if(coord.contains("E")) lonInd = coord.indexOf("E");
        else {
            try {
                lonInd = coord.indexOf("W");
            }
            catch (StringIndexOutOfBoundsException e) {
                System.out.println("The coordinates do not contain information on the longitude.");
                System.exit(0);
            }
        }

        String latitude = coord.substring(0, lonInd);
        String longitude = coord.substring(lonInd);

        latitude = tokenize(latitude);
        longitude = tokenize(longitude);

        for(int i = 0; i < this.neededLetters.size(); i++){

            int id = CoordCalculator.this.getResources().getIdentifier(
                    String.valueOf(i),
                    "id",
                    CoordCalculator.this.getPackageName()
            );

            TextView valueField = (TextView) findViewById(id);
            int value = Integer.parseInt(valueField.getText().toString());

            this.variables.put(neededLetters.get(i), value);
        }

        for (Map.Entry<String, Integer> pair : variables.entrySet()) {
            String key = pair.getKey();
            String value = Integer.toString(pair.getValue());
            latitude = latitude.replaceAll(key, value);
            longitude = longitude.replaceAll(key, value);
        }


        Pattern token = Pattern.compile("\\((.*?)\\)");
        Matcher latTokens = token.matcher(latitude.substring(1));
        while (latTokens.find()) {
            String toEval = latTokens.group(1);
            latitude = latitude.replace(latTokens.group(), Integer.toString((int) eval(toEval)));
        }

        Matcher lonTokens = token.matcher(longitude.substring(1));
        while (lonTokens.find()) {
            String toEval = lonTokens.group(1);
            longitude = longitude.replace(lonTokens.group(), Integer.toString((int) eval(toEval)));
        }

        TextView result = (TextView) findViewById(R.id.result);
        result.setText("Final latitude is " + latitude + " and final longitude is "+ longitude);
    }
}

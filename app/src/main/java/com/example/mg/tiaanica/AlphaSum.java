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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class AlphaSum extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alpha_sum);

        CharSequence text = getIntent()
                .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        String value = Integer.toString(sum(text.toString()));
        final EditText finalTextField = findViewById(R.id.editText);
        finalTextField.setText(text);

        String message = "The alpha sum value of '" + text + "' is " + value;
        TextView textView = findViewById(R.id.textView);
        textView.setVisibility(View.VISIBLE);
        textView.setText(message);
        // process the text


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(AlphaSum.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.help).setMessage(Html.fromHtml("<b><i>Alpha Sum: </i></b>" + getString(R.string.alphasum_info)));

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

        // Help button
        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        finalTextField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    calculateAlphaSum(v); // parse the coordinate
                    assert mgr != null;
                    mgr.hideSoftInputFromWindow(finalTextField.getWindowToken(), 0); // make the keyboard disappear
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
        getMenuInflater().inflate(R.menu.alpha_sum, menu);
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
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(this, Map.class);
            startActivity(intent);
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private int sum(String text){
        int value = 0;

        for (int i = 0; i < text.length(); i++){
            char letter = text.charAt(i);

            switch(letter){
                case 'a':
                case 'A':
                    value = value + 1;
                    break;

                case 'b':
                case 'B':
                    value += 2;
                    break;

                case 'c':
                case 'C':
                    value += 3;
                    break;

                case 'd':
                case 'D':
                    value += 4;
                    break;

                case 'e':
                case 'E':
                    value += 5;
                    break;

                case 'f':
                case 'F':
                    value += 6;
                    break;

                case 'g':
                case 'G':
                    value += 7;
                    break;

                case 'h':
                case 'H':
                    value += 8;
                    break;

                case 'i':
                case 'I':
                    value += 9;
                    break;

                case 'j':
                case 'J':
                    value += 10;
                    break;

                case 'k':
                case 'K':
                    value += 11;
                    break;

                case 'l':
                case 'L':
                    value += 12;
                    break;

                case 'm':
                case 'M':
                    value += 13;
                    break;

                case 'n':
                case 'N':
                    value += 14;
                    break;

                case 'o':
                case 'O':
                    value += 15;
                    break;

                case 'p':
                case 'P':
                    value += 16;
                    break;

                case 'q':
                case 'Q':
                    value += 17;
                    break;

                case 'r':
                case 'R':
                    value += 18;
                    break;

                case 's':
                case 'S':
                    value += 19;
                    break;

                case 't':
                case 'T':
                    value += 20;
                    break;

                case 'u':
                case 'U':
                    value += 21;
                    break;

                case 'v':
                case 'V':
                    value += 22;
                    break;

                case 'w':
                case 'W':
                    value += 23;
                    break;

                case 'x':
                case 'X':
                    value += 24;
                    break;

                case 'y':
                case 'Y':
                    value += 25;
                    break;

                case 'z':
                case 'Z':
                    value += 26;
                    break;

                case '0':
                    value += 0;
                    break;

                case '1':
                    value += 1;
                    break;

                case '2':
                    value += 2;
                    break;

                case '3':
                    value += 3;
                    break;

                case '4':
                    value += 4;
                    break;

                case '5':
                    value += 5;
                    break;

                case '6':
                    value += 6;
                    break;

                case '7':
                    value += 7;
                    break;

                case '8':
                    value += 8;
                    break;

                case '9':
                    value += 9;
                    break;
            }

        }

        return (value);
    }

    public void calculateAlphaSum(View view){

        EditText editText = findViewById(R.id.editText);
        String text= editText.getText().toString();

        String message = "The alpha sum value of '" + text + "' is " + Integer.toString(sum(text));

        // intent.putExtra(EXTRA_MESSAGE,message);
        //startActivity vai começar a actividade DisplayMessageActivity especificada pelo Intent
        // startActivity(intent);

        TextView textView = findViewById(R.id.textView);
        textView.setVisibility(View.VISIBLE);
        textView.setText(message);

    }

}

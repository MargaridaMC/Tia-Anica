package com.example.mg.tiaanica;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Display;
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
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

public class VigenereCipher extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vigenere_cipher);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(VigenereCipher.this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(R.string.help).setMessage(Html.fromHtml("<b><i>Vigenère Cipher: </i></b>" + getString(R.string.vigenere_info)));

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
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vigenere_cipher, menu);
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

    char[] allowedCharacters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

    char[] msg;
    char[] key;
    int msgLen;

    public void VigenereCipher(String msg, String key) {

        msg = msg.toUpperCase();
        key = key.toUpperCase();

        char[] msgArray = msg.toCharArray();
        char[] keyArray = key.toCharArray();

        int msgLen = msgArray.length;
        char[] newKey = new char[msgLen];

        //generate new key in cyclic manner equal to the length of original message
        for(int i = 0, j = 0; i < msgLen; ++i, ++j){
            if(j == keyArray.length)
                j = 0;

            if (msgArray[i] == ' ' || Arrays.binarySearch(allowedCharacters,msgArray[i])<0) {
                newKey[i] = ' ';
                j--;
                continue;
            }


            newKey[i] = keyArray[j];
        }

        this.msg = msgArray;
        this.key = newKey;
        this.msgLen = msg.length();
    }

    public void encode(View view) {

        EditText msgText = findViewById(R.id.msg);
        String msg= msgText.getText().toString();

        EditText keyText = findViewById(R.id.key);
        String key= keyText.getText().toString();

        this.VigenereCipher(msg,key);

        char[] encryptedMsg = new char[msgLen];

        for(int i = 0; i < msgLen; ++i) {

            if (this.msg[i]==' ') {
                encryptedMsg[i] = ' ';
                continue;
            }

            if (Arrays.binarySearch(allowedCharacters, this.msg[i])<0) {
                encryptedMsg[i] = this.msg[i];
                continue;
            }


            encryptedMsg[i] = (char)(((this.msg[i] + this.key[i]) % 26) + 'A');

        }

        String message = "Encoded text: " + String.valueOf(encryptedMsg);

        TextView textView = findViewById(R.id.result);
        textView.setVisibility(View.VISIBLE);
        textView.setText(message);
    }

    public void decode(View view) {

        EditText msgText = findViewById(R.id.msg);
        String msg= msgText.getText().toString();

        EditText keyText = findViewById(R.id.key);
        String key= keyText.getText().toString();

        this.VigenereCipher(msg,key);

        char[] decryptedMsg = new char[msgLen];

        for(int i = 0; i < msgLen; ++i) {

            if (this.msg[i]==' ') {
                decryptedMsg[i] = ' ';
                continue;
            }

            if ( Arrays.binarySearch(allowedCharacters,this.msg[i])<0) {
                decryptedMsg[i] = this.msg[i];
                continue;
            }

            decryptedMsg[i] = (char)(((this.msg[i] - this.key[i] + 26) % 26) + 'A');
        }

        TextView textView = findViewById(R.id.result);
        textView.setVisibility(View.VISIBLE);
        String message = "Decoded text: " + String.valueOf(decryptedMsg);
        textView.setText(message);
    }
}

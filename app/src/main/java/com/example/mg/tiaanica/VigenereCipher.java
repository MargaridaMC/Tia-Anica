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
import android.widget.EditText;

import java.util.Arrays;

public class VigenereCipher extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vigenere_cipher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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

    public char[] encode() {


        char[] encryptedMsg = new char[msgLen];

        for(int i = 0; i < msgLen; ++i) {

            if (msg[i]==' ') {
                encryptedMsg[i] = ' ';
                continue;
            }

            if (Arrays.binarySearch(allowedCharacters,msg[i])<0) {
                encryptedMsg[i] = msg[i];
                continue;
            }


            encryptedMsg[i] = (char)(((msg[i] + key[i]) % 26) + 'A');

        }

        return encryptedMsg;
    }

    public char[] decode() {

        char[] decryptedMsg = new char[msgLen];

        for(int i = 0; i < msgLen; ++i) {

            if (msg[i]==' ') {
                decryptedMsg[i] = ' ';
                continue;
            }

            if ( Arrays.binarySearch(allowedCharacters,msg[i])<0) {
                decryptedMsg[i] = msg[i];
                continue;
            }

            decryptedMsg[i] = (char)(((msg[i] - key[i] + 26) % 26) + 'A');
        }

        return decryptedMsg;
    }

    public void onClickEncode(View view){

        Intent intent = new Intent(this, DisplayCipherResult.class);

        EditText msgText = findViewById(R.id.msg);
        String msg= msgText.getText().toString();

        EditText keyText = findViewById(R.id.key);
        String key= keyText.getText().toString();

        this.VigenereCipher(msg,key);

        char[] encryptedMsg = this.encode();

        String message = "Encoded text: " + String.valueOf(encryptedMsg);

        intent.putExtra(EXTRA_MESSAGE,message);

        startActivity(intent);

    }

    public void onClickDecode(View view){

        Intent intent = new Intent(this, DisplayCipherResult.class);

        EditText msgText = findViewById(R.id.msg);
        String msg= msgText.getText().toString();

        EditText keyText = findViewById(R.id.key);
        String key= keyText.getText().toString();

        this.VigenereCipher(msg,key);

        char[] decryptedMsg = this.decode();

        String result = "Decoded text: " + String.valueOf(decryptedMsg);

        intent.putExtra(EXTRA_MESSAGE,result);

        startActivity(intent);

    }
}

package com.example.mg.tiaanica;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;

public class VigenereCipherActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vigenere_cipher);
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

        // Help FAB
        //FloatingActionButton fab = findViewById(R.id.fab);
        //fab.setOnClickListener((view) -> getHelp());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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

    public void encode(View view) {

        EditText msgText = findViewById(R.id.msg);
        String msg= msgText.getText().toString();

        EditText keyText = findViewById(R.id.key);
        String key= keyText.getText().toString();

        VigenereCipher cipher = new VigenereCipher(msg, key);

        String message = "Encoded text: " + cipher.encode();

        TextView resultTextView = findViewById(R.id.result);
        resultTextView.setVisibility(View.VISIBLE);
        resultTextView.setText(message);

    }

    public void decode(View view) {

        EditText msgText = findViewById(R.id.msg);
        String msg= msgText.getText().toString();

        EditText keyText = findViewById(R.id.key);
        String key= keyText.getText().toString();

        VigenereCipher cipher = new VigenereCipher(msg, key);

        TextView resultTextView = findViewById(R.id.result);
        resultTextView.setVisibility(View.VISIBLE);
        String message = "Decoded text: " + cipher.decode();
        resultTextView.setText(message);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) resultTextView.getLayoutParams();
        params.setMargins(params.getMarginStart(), (int) getResources().getDimension(R.dimen.margin), params.getMarginEnd(), 0);
    }

    void getHelp(){
        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(VigenereCipherActivity.this, R.style.MyAlertDialogTheme);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.help).setMessage(Html.fromHtml(getString(R.string.vigenere_info)));

        // Add OK button
        builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel());


        // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

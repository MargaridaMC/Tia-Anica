package net.teamtruta.tiaires;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements  GeocachingLogin{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         */
    }


    public void login(View view){

        // On button click get username and password from input fields
        EditText usernameField = findViewById(R.id.username);
        String username = usernameField.getText().toString();

        EditText passwordField = findViewById(R.id.password);
        String password = passwordField.getText().toString();

        // Make keyboard disappear
        InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(mgr!=null) mgr.hideSoftInputFromWindow(passwordField.getWindowToken(), 0);

        Log.d("TAG", "username: " + username);
        Log.d("TAG", "pass: " + password);

        // First check if we already have an authentication cookie in the shared preferences
        Context context = this;//getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String authCookie = sharedPref.getString(getString(R.string.authentication_cookie_key), "");

        if(! authCookie.equals("")){
            login(authCookie);
            return;
        }


        if(username.equals("") || password.equals("")){
            //One of the input fields is empty. Request filling up the fields

            TextView failedLoginText = findViewById(R.id.failed_login_message);
            failedLoginText.setVisibility(View.VISIBLE);
            return;

        }

        // Else, if both string are there, try to login
        login(username, password);

    }

    private void login(String authCookie){
        // Login to geocaching.com using authentication cookie

        Log.d("TAG", "Trying to Login using authentication cookie");

        Toast t = Toast.makeText(this, "Trying to Login", Toast.LENGTH_SHORT);
        t.show();

        GeocachingScrapper gs = new GeocachingScrapper(authCookie);

        // Run AsynTask to do login
        LoginTask loginTask = new LoginTask(this);
        loginTask.execute(gs);

    }

    private void login(String username, String password){
        //Login to geocaching.com using username and password

        Log.d("TAG", "Trying to Login using username and password");

        Toast t = Toast.makeText(this, "Trying to Login", Toast.LENGTH_SHORT);
        t.show();

        GeocachingScrapper gs = new GeocachingScrapper();

        // Run AsynTask to do login
        LoginTask loginTask = new LoginTask(username, password,this);
        loginTask.execute(gs);

    }

    public void geocachingLogin(boolean success, String authCookie){

        TextView failedLoginMessage = findViewById(R.id.failed_login_message);

        if(success){
            // Login successful!

            Toast t = Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT);
            t.show();

            failedLoginMessage.setVisibility(View.INVISIBLE);

            // Save authentication cookie in shared preferences
            Context context = this;//getActivity();
            SharedPreferences sharedPref = context.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.authentication_cookie_key), authCookie);
            editor.apply();

            Log.d("TAG", "Saved authentication key to shared preferences");

        } else {

            failedLoginMessage.setVisibility(View.VISIBLE);

            Toast t = Toast.makeText(this, "Login NOT Successful", Toast.LENGTH_SHORT);
            t.show();

        }


    }

}

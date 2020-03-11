package net.teamtruta.tiaires;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;

public class LoginActivity extends AppCompatActivity implements  GeocachingLogin{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set version
        TextView version = findViewById(R.id.version);
        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String versionString = "Version: " + versionName;
        version.setText(versionString);


        final EditText passwordField = findViewById(R.id.password);
        final InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        passwordField.setImeOptions(IME_ACTION_GO);
        passwordField.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;

            if(event.getAction() == KeyEvent.ACTION_DOWN && actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
                if(mgr!=null) mgr.hideSoftInputFromWindow(passwordField.getWindowToken(), 0);
                login(v);
                handled = true;
            }
            return handled;
        });

        // Check if we already have a authentication cookie
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authCookie = sharedPreferences.getString(getString(R.string.authentication_cookie_key), "");
        if(authCookie.equals("")){
            Button logoutButton = findViewById(R.id.logout_button);
            logoutButton.setVisibility(View.INVISIBLE);
        }

        // Fill in username field if we already have that info
        String username = sharedPreferences.getString("username", "");
        if(!username.equals("")){
            EditText usernameField = findViewById(R.id.username);
            usernameField.setText(username);
            passwordField.setText("aaaaaaaaaaaa");
        }

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

        // First check if we already have an authentication cookie in the shared preferences
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String authCookie = sharedPref.getString(getString(R.string.authentication_cookie_key), "");
        Log.d("TAG", "Cookie: " + authCookie);

        Log.d("TAG", "username: " + username);
        Log.d("TAG", "pass: " + password);

        if(username.equals("") || password.equals("")){
            //One of the input fields is empty. Request filling up the fields

            if(!authCookie.equals("")){
                Log.d("TAG", authCookie);
                login(authCookie);
                return;
            }

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

    public void geocachingLogin(String username, boolean success, String authCookie){

        TextView failedLoginMessage = findViewById(R.id.failed_login_message);

        if(success){
            // Login successful!

            Toast t = Toast.makeText(this, "Login Successful.", Toast.LENGTH_SHORT);
            t.show();

            failedLoginMessage.setVisibility(View.INVISIBLE);

            // Save authentication cookie in shared preferences
            SharedPreferences sharedPref = this.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.authentication_cookie_key), authCookie);

            if(!username.equals("")){
                editor.putString("username", username);
            }

            editor.apply();

            Log.d("TAG", "Saved authentication key to shared preferences");

            // Allow user to logout from now on
            Button logoutButton = findViewById(R.id.logout_button);
            logoutButton.setVisibility(View.VISIBLE);

            // Open home page
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        }

        // Not necessary if error message already shows on top
        /*else {

            failedLoginMessage.setVisibility(View.VISIBLE);

            Toast t = Toast.makeText(this, "Login NOT Successful.", Toast.LENGTH_SHORT);
            t.show();

        }
        */
    }

    public void logout(View view){

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.remove(getString(R.string.authentication_cookie_key));
        editor.apply();

        Toast t = Toast.makeText(this, "Logout Successful.", Toast.LENGTH_SHORT);
        t.show();

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setVisibility(View.INVISIBLE);

        EditText usernameField = findViewById(R.id.username);
        EditText passwordField = findViewById(R.id.password);
        usernameField.setText("");
        passwordField.setText("");

    }

}

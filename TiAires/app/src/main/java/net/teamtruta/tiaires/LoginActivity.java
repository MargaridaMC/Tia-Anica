package net.teamtruta.tiaires;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;

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

        if(username.equals("") || password.equals("")){
            //One of the input fields is empty. Request filling up the fields

            TextView failedLoginText = findViewById(R.id.failed_login_message);
            failedLoginText.setVisibility(View.VISIBLE);
            return;

        }

        // Else, if both string are there, try to login
        login(username, password);

    }

    private void login(String username, String password){
        //Login to geocaching.com

        Log.d("TAG", "Trying to Login");

        Toast t = Toast.makeText(this, "Trying to Login", Toast.LENGTH_SHORT);
        t.show();

        GeocachingScrapper gs = new GeocachingScrapper(username, password);

        // Run AsynTask to do login
        LoginTask loginTask = new LoginTask();
        loginTask.delegate = this;
        loginTask.execute(gs);

    }

    public void geocachingLogin(boolean success){

        TextView failedLoginMessage = findViewById(R.id.failed_login_message);

        if(success){
            // Login successful!

            Toast t = Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT);
            t.show();

            failedLoginMessage.setVisibility(View.INVISIBLE);

        } else {

            failedLoginMessage.setVisibility(View.VISIBLE);

            Toast t = Toast.makeText(this, "Login NOT Successful", Toast.LENGTH_SHORT);
            t.show();

        }


    }

}

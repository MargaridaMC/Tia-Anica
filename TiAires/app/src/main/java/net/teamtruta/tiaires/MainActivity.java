package net.teamtruta.tiaires;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class MainActivity extends AppCompatActivity implements TourListAdapter.ItemClickListener{

    RecyclerView.Adapter tourListAdapter;
    RecyclerView tourListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCenter.start(getApplication(), "67d245e6-d08d-4d74-8616-9af6c3471a09", Analytics.class, Crashes.class);


        // If Login is required, set contentView to the login page
        // Else go to home page
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");
        String authCookie = sharedPref.getString(getString(R.string.authentication_cookie_key), "");

        Map<String, String> properties = new HashMap<>();
        properties.put("Username", username);
        Analytics.trackEvent("MainActivity.onCreate", properties);

        Log.d("TAG", "Cookie: " + authCookie);

        if(authCookie.equals("")){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        String rootPath = App.getTourRoot();
        File tourFolder = new File(rootPath);
        if(!tourFolder.exists()){
            tourFolder.mkdir();
        }
        Log.d("TAG", "Tours data folder exists: " + tourFolder.exists());

        String allToursFilePath = App.getAllToursFilePath();
        if(TourList.exists(allToursFilePath))
        {
            Log.d("TAG", "TourList file exists: true");

            setContentView(R.layout.activity_main);

            ArrayList<GeocachingTourSummary> tourList = TourList.read(allToursFilePath);

            tourListView = findViewById(R.id.tour_list);
            tourListView.setLayoutManager(new LinearLayoutManager(this));
            tourListAdapter = new TourListAdapter(tourList, this);
            tourListView.setAdapter(tourListAdapter);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(tourListView.getContext(), LinearLayout.VERTICAL);
            dividerItemDecoration.setDrawable(new ColorDrawable(getColor(R.color.black)));
            tourListView.addItemDecoration(dividerItemDecoration);

        } else {
            setContentView(R.layout.activity_main_nothing_to_show);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), TourCreationActivity.class);
            startActivity(intent);
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Put username on toolbar
        if(!username.equals("")){
            getSupportActionBar().setTitle(username);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            //return true;
        }

        if (id == R.id.action_logout) {

            SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("username");
            editor.remove(getString(R.string.authentication_cookie_key));
            editor.apply();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position, String tourName) {
        //Toast.makeText(this, "You clicked " + _tourName + " on row number " + position, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra("_tourName", tourName);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // A more recommended way seems to be what is documented here: https://developer.android.com/guide/navigation/navigation-custom-back#java
        // do what you want to do when the "back" button is pressed.
        finishAffinity();

        // android.os.Process.killProcess(android.os.Process.myPid());
        // System.exit(0); // see: https://stackoverflow.com/questions/18292016/difference-between-finish-and-system-exit0
    }
}

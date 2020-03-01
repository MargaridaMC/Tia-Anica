package net.teamtruta.tiaires;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
        Context context = this;//getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        String authCookie = sharedPref.getString(getString(R.string.authentication_cookie_key), "");

        Log.d("TAG", "Cookie: " + authCookie);

        if(authCookie.equals("")){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        File tourFolder = new File(rootPath);
        if(!tourFolder.exists()){
            tourFolder.mkdir();
        }

        Log.d("TAG", "Folder exists: " + tourFolder.exists());

        File allToursFile = new File(tourFolder, getString(R.string.all_tours_filename));
        Log.d("TAG", "File exists: " + allToursFile.exists());


        if(allToursFile.exists()){

            setContentView(R.layout.activity_main);

            ArrayList<GeocachingTourSummary> tourList = TourList.fromFile(allToursFile);

            tourListView = findViewById(R.id.tour_list);
            tourListView.setLayoutManager(new LinearLayoutManager(this));
            tourListAdapter = new TourListAdapter(this, tourList, this);
            tourListView.setAdapter(tourListAdapter);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(tourListView.getContext(), LinearLayout.VERTICAL);
            tourListView.addItemDecoration(dividerItemDecoration);

        } else {
            setContentView(R.layout.activity_main_nothing_to_show);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), TourCreationActivity.class);
                startActivity(intent);

            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Put username on toolbar
        String username = sharedPref.getString("username", "");
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

}

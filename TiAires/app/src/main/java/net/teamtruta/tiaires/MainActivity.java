package net.teamtruta.tiaires;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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
import android.widget.Toast;

import java.util.ArrayList;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class MainActivity extends AppCompatActivity implements TourListAdapter.ItemClickListener{

    private RecyclerView tourListView;
    private RecyclerView.Adapter tourListAdapter;

    // For now our dataset is defined here
    // TODO: dataset is to be read from internal storage

    GeocachingScrapper scrapper = new GeocachingScrapper();

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

        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
/*
        // Login is done
        try{
            boolean loginSuccess = scrapper.login();
        } catch (Exception e){
            e.fillInStackTrace();
            Log.d("TAG", "Couldn't Login");
            return;
        }
*/

        GeocachingTour tour0 = new GeocachingTour("My tour 0");
        Geocache gc = new Geocache();
        gc.code = "GC63TAD";
        tour0._numFound = 1;
        tour0.addToTour(gc);
        tour0.isCurrentTour = true;

        GeocachingTour tour1 = new GeocachingTour("My tour 1");
        tour1.addToTour(gc);
        gc = new Geocache();
        gc.code = "GC269NB";
        tour1.addToTour(gc);
        gc = new Geocache();
        gc.code = "GC2RK1V";
        tour1.addToTour(gc);
        tour1._numDNF = 1;
        tour1._numFound = 1;

        ArrayList<GeocachingTour> tourList = new ArrayList<>();
        tourList.add(tour0);
        tourList.add(tour1);

        tourListView = findViewById(R.id.tour_list);
        tourListView.setLayoutManager(new LinearLayoutManager(this));
        tourListAdapter = new TourListAdapter(this, tourList, this);
        tourListView.setAdapter(tourListAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(tourListView.getContext(), LinearLayout.VERTICAL);
        tourListView.addItemDecoration(dividerItemDecoration);

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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            //return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked on row number " + position, Toast.LENGTH_SHORT).show();
    }
}

package net.teamtruta.tiaires;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.appcenter.analytics.Analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TourActivity extends AppCompatActivity implements CacheListAdapter.EditOnClickListener, CacheListAdapter.GoToOnClickListener {

    String tourName;
    GeocachingTour tour;
    String _rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        tourName = intent.getExtras().getString("_tourName");

        Map<String, String> properties = new HashMap<>();
        properties.put("TourName", tourName);
        Analytics.trackEvent("TourActivity.onCreate", properties);

        _rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);

        tour = GeocachingTour.fromFile(_rootPath, tourName);

        // Set title
        ab.setTitle(tourName);

        // Set progress
        TextView progressText = findViewById(R.id.tour_progress);
        String progress = tour.getNumFound() + " + " + tour.getNumDNF() + " / " + tour.getSize();
        progressText.setText(progress);

        // Set List
        RecyclerView cacheListView = findViewById(R.id.tour_view);
        cacheListView.setLayoutManager(new LinearLayoutManager(this));
        CacheListAdapter cacheListAdapter = new CacheListAdapter(tour, this, this);
        cacheListView.setAdapter(cacheListAdapter);

        // Create diving line between elements
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(cacheListView.getContext(), LinearLayout.VERTICAL);
        cacheListView.addItemDecoration(dividerItemDecoration);

        // Add swipe to delete action
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToVisitCallback(cacheListAdapter));
        itemTouchHelper.attachToRecyclerView(cacheListView);

    }

    public void editTour(View view){
        Intent intent = new Intent(this, TourCreationActivity.class);
        intent.putExtra("_tourName", tourName);
        intent.putExtra("edit", true);
        startActivity(intent);
    }

    public void deleteTour(View view){


        final Context context = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete tour?");
        builder.setMessage("This will delete tour " + tourName);
        builder.setPositiveButton("Confirm", (dialog, which) -> {

            Map<String, String> properties = new HashMap<>();
            properties.put("TourName", tourName);
            properties.put("UserConfirmed", "true");
            Analytics.trackEvent("TourActivity.deleteTour", properties);

            // Delete tour file
            GeocachingTour.deleteTourFile(_rootPath, tourName);

            TourList.removeTour(_rootPath, tourName);

            Intent intent = new Intent(context, MainActivity.class);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog

            Map<String, String> properties = new HashMap<>();
            properties.put("TourName", tourName);
            properties.put("UserConfirmed", "false");
            Analytics.trackEvent("TourActivity.deleteTour", properties);

        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onClick(int position) {
        Intent intent = new Intent(this, CacheDetailActivity.class);
        intent.putExtra("currentTour", tour.toJSON().toString());
        intent.putExtra("currentCacheIndex", position);
        startActivity(intent);
    }

    @Override
    public void onGoToClick(String code){

        String url = "https://coord.info/" + code;
        Intent i = new Intent(Intent.ACTION_VIEW);

        i.setData(Uri.parse(url));

        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        // do what you want to do when the "back" button is pressed.
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void share(View view){

        List<String> tourCacheCodes = new ArrayList<>();
        for(GeocacheInTour gc:tour._tourCaches){
            tourCacheCodes.add(gc.geocache.code);
        }

        String tourCacheCodesString = tourCacheCodes.toString();
        tourCacheCodesString = tourCacheCodesString.substring(1, tourCacheCodesString.length() - 1);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("vnd.android.cursor.dir/email");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, tourName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, tourCacheCodesString);
        this.startActivity(shareIntent);

    }
}

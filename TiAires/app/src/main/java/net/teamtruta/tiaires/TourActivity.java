package net.teamtruta.tiaires;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class TourActivity extends AppCompatActivity implements CacheListAdapter.EditOnClickListener {

    String tourName;
    GeocachingTour tour;
    File root;

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
        String rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);
        root = new File(rootPath);

        tour = GeocachingTour.fromFile(root, tourName);

        // Set title
        ab.setTitle(tourName);

        // Set progress
        TextView progressText = findViewById(R.id.tour_progress);
        String progress = tour._numFound + " + " + tour._numDNF + " / " + tour.size();
        progressText.setText(progress);

        // Set List
        RecyclerView cacheListView = findViewById(R.id.tour_view);
        cacheListView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter cacheListAdapter = new CacheListAdapter(tour, this);
        cacheListView.setAdapter(cacheListAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(cacheListView.getContext(), LinearLayout.VERTICAL);
        cacheListView.addItemDecoration(dividerItemDecoration);

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
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Delete tour file
                GeocachingTour.deleteTourFile(root, tourName);
                Log.d("TAG", tourName + " should not be in " + root.listFiles());

                // Delete tour entry in tour list file
                File allToursFile = new File(root, getString(R.string.all_tours_filename));
                ArrayList<GeocachingTour> allTours = TourList.fromFile(allToursFile);
                Log.d("TAG", "Original size: " + allTours.size());

                for(GeocachingTour tour: allTours){
                    if(tour.getName().equals(tourName)){
                        allTours.remove(tour);
                        break;
                    }
                }

                Log.d("TAG", "New size: " + allTours.size());

                boolean saved = TourList.toFile(allTours, allToursFile);

                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
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
}

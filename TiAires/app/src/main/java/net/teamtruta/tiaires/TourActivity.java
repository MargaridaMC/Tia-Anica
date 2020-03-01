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

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.appcenter.analytics.Analytics;

public class TourActivity extends AppCompatActivity implements CacheListAdapter.EditOnClickListener, CacheListAdapter.GoToOnClickListener {

    String tourName;
    GeocachingTour tour;
    String _rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Analytics.trackEvent("TourActivity.onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        tourName = intent.getExtras().getString("_tourName");
        _rootPath = getFilesDir().toString() + "/" + getString(R.string.tour_folder);

        tour = GeocachingTour.fromFile(_rootPath, tourName);

        // Set title
        ab.setTitle(tourName);

        // Set progress
        TextView progressText = findViewById(R.id.tour_progress);
        String progress = tour._numFound + " + " + tour._numDNF + " / " + tour.getSize();
        progressText.setText(progress);

        // Set List
        RecyclerView cacheListView = findViewById(R.id.tour_view);
        cacheListView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter cacheListAdapter = new CacheListAdapter(tour, this, this);
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
        Analytics.trackEvent("TourActivity.deleteTour");

        final Context context = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete tour?");
        builder.setMessage("This will delete tour " + tourName);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Delete tour file
                GeocachingTour.deleteTourFile(_rootPath, tourName);

                TourList.removeTour(_rootPath, tourName);

                /*
                // Delete tour entry in tour list file
                ArrayList<GeocachingTourSummary> allTours = TourList.read(_rootPath);
                Log.d("TAG", "Original getSize: " + allTours.size());

                for(GeocachingTourSummary tour: allTours){ // TODO - usar um predicado
                    if(tour.getName().equals(tourName)){
                        allTours.remove(tour);
                        break;
                    }
                }

                Log.d("TAG", "New getSize: " + allTours.size());

                boolean saved = TourList.write(_rootPath, allTours);
                 */

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

    @Override
    public void onGoToClick(String code){

        String url = "https://coord.info/" + code;
        Intent i = new Intent(Intent.ACTION_VIEW);

        i.setData(Uri.parse(url));

        startActivity(i);
    }

}

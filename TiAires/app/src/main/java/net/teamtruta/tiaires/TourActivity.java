package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Text;

import java.io.File;

public class TourActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        Intent intent = getIntent();
        //String tourName = intent.getExtras().getString("tourName");
        //File rootPath = this.getFilesDir();
        //GeocachingTour tour = GeocachingTour.fromFile(rootPath, tourName);

        String tourName = "Limpar Schwabing";
        GeocachingTour tour = new GeocachingTour(tourName);
        String tourString = "[{\"difficulty\":\"4\",\"code\":\"GC3AK7Y\",\"recentLogs\":[],\"size\":\"Micro\",\"foundIt\":2,\"favourites\":67,\"latitude\":\"N 48° 08.556\",\"hint\":\"NO MATCH\",\"name\":\"Letzter Halt Sophienplatz?!\",\"type\":\"Mystery\",\"terrain\":\"1.5\",\"longitude\":\"E 011° 33.872\"},{\"difficulty\":\"2\",\"code\":\"GC3443H\",\"recentLogs\":[],\"size\":\"Small\",\"foundIt\":2,\"favourites\":12,\"latitude\":\"N 48° 08.751\",\"hint\":\"NO MATCH\",\"name\":\"U-Bahn2 #12 (U2): Königsplatz\",\"type\":\"Traditional\",\"terrain\":\"1.5\",\"longitude\":\"E 011° 33.810\"}]";

        JSONArray newCacheArray = null;
        try {
            newCacheArray = new JSONArray(tourString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tour.fromJSON(newCacheArray);

        // Set title
        TextView title = findViewById(R.id.tour_name);
        title.setText(tourName);

        // Set progress
        TextView progressText = findViewById(R.id.tour_progress);
        String progress = tour._numFound + " + " + tour._numDNF + " / " + tour.size();
        progressText.setText(progress);

        // Set List
        RecyclerView cacheListView = findViewById(R.id.tour_view);
        cacheListView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter cacheListAdapter = new CacheListAdapter(tour);
        cacheListView.setAdapter(cacheListAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(cacheListView.getContext(), LinearLayout.VERTICAL);
        cacheListView.addItemDecoration(dividerItemDecoration);

    }

    public void goBack(View view){
        Intent intent  = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

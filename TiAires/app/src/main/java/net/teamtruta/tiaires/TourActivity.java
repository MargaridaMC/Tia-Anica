package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

        GeocachingTour tour = new GeocachingTour("Limpar Schwabing");
        String tourString = "[{\"difficulty\":\"2\",\"code\":\"GC3AK7Y\",\"recentLogs\":[],\"size\":\"Micro\",\"foundIt\":2,\"favourites\":67,\"latitude\":\"N 48° 08.556\",\"hint\":\"NO MATCH\",\"name\":\"Letzter Halt Sophienplatz?!\",\"type\":\"Mystery\",\"terrain\":\"1.5\",\"longitude\":\"E 011° 33.872\"},{\"difficulty\":\"2\",\"code\":\"GC3443H\",\"recentLogs\":[],\"size\":\"Small\",\"foundIt\":2,\"favourites\":12,\"latitude\":\"N 48° 08.751\",\"hint\":\"NO MATCH\",\"name\":\"U-Bahn2 #12 (U2): Königsplatz\",\"type\":\"Traditional\",\"terrain\":\"1.5\",\"longitude\":\"E 011° 33.810\"}]";
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        try {
            obj = jsonParser.parse(tourString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray newCacheArray = (JSONArray) obj;
        tour.fromJSON(newCacheArray);

        RecyclerView cacheListView = findViewById(R.id.tour_view);
        cacheListView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter cacheListAdapter = new CacheListAdapter(tour);
        cacheListView.setAdapter(cacheListAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(cacheListView.getContext(), LinearLayout.VERTICAL);
        cacheListView.addItemDecoration(dividerItemDecoration);

    }
}

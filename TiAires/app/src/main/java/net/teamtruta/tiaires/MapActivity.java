package net.teamtruta.tiaires;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.teamtruta.tiaires.db.CacheDbTable;
import net.teamtruta.tiaires.db.CacheDetailDbTable;
import net.teamtruta.tiaires.db.DbConnection;
import net.teamtruta.tiaires.db.TourDbTable;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    GeocachingTour tour;
    Geocache cacheToFocusOn;
    boolean focusOnCache;

    String TAG = MapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        long tourID = intent.getLongExtra(App.TOUR_ID_EXTRA, -1L);
        if(tourID != -1L){
            tour = new TourDbTable(this).getTour(tourID, new DbConnection(this));
        } else {
            // TODO : something went wrong
            return;
        }

        setTitle(tour.getName());

        focusOnCache = intent.getBooleanExtra(App.FOCUS_ON_CACHE_EXTRA, false);
        if(focusOnCache){
            long cacheID = intent.getLongExtra(App.CACHE_ID_EXTRA, -1L);
            cacheToFocusOn = new CacheDetailDbTable(this).getGeocache(cacheID);

            Log.d(TAG, "Focus: " + cacheToFocusOn);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng centre;

        // Find the center or focus on the clicked cache
        if(focusOnCache){
            Coordinate latitude = cacheToFocusOn.getLatitude();
            Coordinate longitude = cacheToFocusOn.getLongitude();
            centre = new LatLng(latitude.getValue(), longitude.getValue());
            map.addMarker(new MarkerOptions().position(centre));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(centre, 13.0f));
            return;
        }

        List<LatLng> allCacheCoordinates = new ArrayList<>();
        for(GeocacheInTour gcit : tour._tourCaches){

            LatLng geocacheCoordinates = gcit.getGeocache().getLatLng();
            allCacheCoordinates.add(geocacheCoordinates);
            map.addMarker(new MarkerOptions().position(geocacheCoordinates).title(gcit.getGeocache().getName()));
        }

        centre = computeCentroid(allCacheCoordinates);
        map.moveCamera(CameraUpdateFactory.newLatLng(centre));

        Log.d(TAG, "Created Map.");
    }

    private LatLng computeCentroid(List<LatLng> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();

        for (LatLng point : points) {
            latitude += point.latitude;
            longitude += point.longitude;
        }

        return new LatLng(latitude/n, longitude/n);
    }
}

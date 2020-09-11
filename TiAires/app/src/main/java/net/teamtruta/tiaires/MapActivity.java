package net.teamtruta.tiaires;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

// classes needed to initialize map
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

// classes needed to add the location component
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;

// classes needed to add a marker
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import net.teamtruta.tiaires.db.DbConnection;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import java.util.ArrayList;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;

    // variables for adding location layer
    private PermissionsManager permissionsManager;

    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";

    String TAG = MapActivity.class.getSimpleName();
    GeocachingTour _tour;
    long tourID;
    DbConnection dbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Get the ID of the tour that was clicked on
        Intent intent = getIntent();
        tourID = intent.getLongExtra(App.TOUR_ID_EXTRA, -1);
        if (tourID == -1) {
            // Something went wrong
            Log.e(TAG, "Could not get clicked tour.");
            Toast.makeText(this, "An error occurred: couldn't get the requested tour", Toast.LENGTH_LONG).show();
            return;
        }

        // Setup connection to database
        dbConnection = new DbConnection(this);
        _tour = GeocachingTour.getGeocachingTourFromID(tourID, dbConnection);

        // Set title
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(_tour.getName());

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng location;
        for (GeocacheInTour gcit : _tour._tourCaches) {
            location = gcit.getGeocache().getLatLng();
            Feature feature = Feature.fromGeometry(
                    Point.fromLngLat(location.getLongitude(), location.getLatitude()));
            if (gcit.getVisit() == FoundEnumType.Found || gcit.getVisit() == FoundEnumType.DNF) {
                feature.addStringProperty("type", gcit.getVisit().getTypeString());
            } else {
                feature.addStringProperty("type", gcit.getGeocache().getType().getTypeString());//getCacheIconResource(gcit.getGeocache().getType()));
            }

            symbolLayerIconFeatureList.add(feature);
            builder.include(location);
        }


        Style.Builder styleBuilder = new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));

        for (CacheTypeEnum type : CacheTypeEnum.values()) {
            String typeString = type.getTypeString();
            if (typeString == null) break;
            styleBuilder.withImage(typeString, BitmapFactory.decodeResource(
                    MapActivity.this.getResources(), GeocacheIcon.getIconDrawable(typeString)));
        }
        // Add finds and dnfs as well
        styleBuilder.withImage(FoundEnumType.Found.getTypeString(), BitmapFactory.decodeResource(
                MapActivity.this.getResources(), R.drawable.cache_icon_found));
        styleBuilder.withImage(FoundEnumType.DNF.getTypeString(), BitmapFactory.decodeResource(
                MapActivity.this.getResources(), R.drawable.cache_icon_dnf));

        styleBuilder.withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(iconImage(match(get("type"), literal(CacheTypeEnum.Traditional.getTypeString()),
                        stop(CacheTypeEnum.Traditional.getTypeString(), CacheTypeEnum.Traditional.getTypeString()),
                        stop(CacheTypeEnum.Mystery.getTypeString(), CacheTypeEnum.Mystery.getTypeString()),
                        stop(CacheTypeEnum.Solved.getTypeString(), CacheTypeEnum.Solved.getTypeString()),
                        stop(CacheTypeEnum.Multi.getTypeString(), CacheTypeEnum.Multi.getTypeString()),
                        stop(CacheTypeEnum.Earth.getTypeString(), CacheTypeEnum.Earth.getTypeString()),
                        stop(CacheTypeEnum.Letterbox.getTypeString(), CacheTypeEnum.Letterbox.getTypeString()),
                        stop(CacheTypeEnum.Event.getTypeString(), CacheTypeEnum.Event.getTypeString()),
                        stop(CacheTypeEnum.CITO.getTypeString(), CacheTypeEnum.CITO.getTypeString()),
                        stop(CacheTypeEnum.Mega.getTypeString(), CacheTypeEnum.Mega.getTypeString()),
                        stop(CacheTypeEnum.Giga.getTypeString(), CacheTypeEnum.Giga.getTypeString()),
                        stop(CacheTypeEnum.HQ.getTypeString(), CacheTypeEnum.HQ.getTypeString()),
                        stop(CacheTypeEnum.GPSAdventures.getTypeString(), CacheTypeEnum.GPSAdventures.getTypeString()),
                        stop(CacheTypeEnum.Lab.getTypeString(), CacheTypeEnum.Lab.getTypeString()),
                        stop(CacheTypeEnum.HQCelebration.getTypeString(), CacheTypeEnum.HQCelebration.getTypeString()),
                        stop(CacheTypeEnum.HQBlockParty.getTypeString(), CacheTypeEnum.HQBlockParty.getTypeString()),
                        stop(CacheTypeEnum.CommunityCelebration.getTypeString(), CacheTypeEnum.CommunityCelebration.getTypeString()),
                        stop(CacheTypeEnum.Virtual.getTypeString(), CacheTypeEnum.Virtual.getTypeString()),
                        stop(CacheTypeEnum.Webcam.getTypeString(), CacheTypeEnum.Webcam.getTypeString()),
                        stop(CacheTypeEnum.ProjectAPE.getTypeString(), CacheTypeEnum.ProjectAPE.getTypeString()),
                        stop(CacheTypeEnum.Locationless.getTypeString(), CacheTypeEnum.Locationless.getTypeString()),
                        stop(FoundEnumType.Found.getTypeString(), FoundEnumType.Found.getTypeString()),
                        stop(FoundEnumType.DNF.getTypeString(), FoundEnumType.DNF.getTypeString()))),
                        iconAllowOverlap(true),
                        iconAnchor(Property.ICON_ANCHOR_CENTER),
                        iconSize(0.4f))
        );

        mapboxMap.setStyle(styleBuilder, this::enableLocationComponent);
        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50), 2000);

        // Set my location FAB onClickListener
        Activity context = this;
        findViewById(R.id.my_location_fab).setOnClickListener(view -> {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            try {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(new LatLng(
                                locationComponent.getLastKnownLocation().getLatitude(),
                                locationComponent.getLastKnownLocation().getLongitude()))
                        , 2500);
            } catch (Exception e) {

                if (PermissionsManager.areLocationPermissionsGranted(context)) {
                    LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!gps_enabled)
                        Toast.makeText(context, "Please turn on your GPS", Toast.LENGTH_SHORT).show();
                    else
                        enableLocationComponent(mapboxMap.getStyle());

                } else {
                    permissionsManager = new PermissionsManager((PermissionsListener) context);
                    permissionsManager.requestLocationPermissions(context);
                }


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            //locationComponent.setCameraMode(CameraMode.TRACKING);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
/*
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
*/
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id  = item.getItemId();

        // click on icon to go back
        //triangle icon on the main android toolbar.

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, TourActivity.class);
        intent.putExtra(App.TOUR_ID_EXTRA, tourID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
package net.teamtruta.tiaires;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// classes needed to initialize map
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
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
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import net.teamtruta.tiaires.db.DbConnection;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;

    // variables for adding location layer
    private PermissionsManager permissionsManager;

    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_DIFFICULTY = "difficulty";
    private static final String PROPERTY_TERRAIN = "terrain";
    private static final String PROPERTY_SELECTED = "selected";
    private static final int CIRCLE_COLOR = Color.RED;
    private static final int LINE_COLOR = CIRCLE_COLOR;
    private static final float CIRCLE_RADIUS = 6f;
    private static final float LINE_WIDTH = 4f;

    private static final String CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID";
    private static final String LINE_LAYER_ID = "LINE_LAYER_ID";

    String TAG = MapActivity.class.getSimpleName();
    GeocachingTour _tour;
    long tourID;
    DbConnection dbConnection;

    private GeoJsonSource source;
    private FeatureCollection featureCollection;

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

        featureCollection = getData();
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            setUpData(featureCollection);
            enableLocationComponent(style);
            mapboxMap.addOnMapClickListener(MapActivity.this);
        });

        new GenerateViewIconTask(MapActivity.this).execute(featureCollection);

        // Set my location FAB onClickListener
        setupMyLocationFAB();
    }

    private void setupMyLocationFAB() {

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
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
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

    private boolean handleClickIcon(PointF screenPoint) {

        // Display text box with info
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        if (featureSelectStatus(i)) {
                            setFeatureSelectState(featureList.get(i), false);
                        } else {
                            setSelected(i);
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }


    }

    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }

    private void setSelected(int index) {
        if (featureCollection.features() != null) {
            Feature feature = featureCollection.features().get(index);
            setFeatureSelectState(feature, true);
            refreshSource();
        }
    }

    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        if (feature.properties() != null) {
            feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
            refreshSource();
        }
    }

    private void refreshSource() {
        if (source != null && featureCollection != null) {
            source.setGeoJson(featureCollection);
        }
    }

    public FeatureCollection getData(){

        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng location;
        for (GeocacheInTour gcit : _tour._tourCaches) {
            location = gcit.getGeocache().getLatLng();
            Feature feature = Feature.fromGeometry(
                    Point.fromLngLat(location.getLongitude(), location.getLatitude()));

            feature.addStringProperty(PROPERTY_NAME, gcit.getGeocache().getName());
            feature.addStringProperty(PROPERTY_DIFFICULTY, gcit.getGeocache().getDifficulty());
            feature.addStringProperty(PROPERTY_TERRAIN, gcit.getGeocache().getTerrain());
            feature.addBooleanProperty(PROPERTY_SELECTED, false);

            if (gcit.getVisit() == FoundEnumType.Found || gcit.getVisit() == FoundEnumType.DNF) {
                feature.addStringProperty(PROPERTY_TYPE, gcit.getVisit().getTypeString());
            } else {
                feature.addStringProperty(PROPERTY_TYPE, gcit.getGeocache().getType().getTypeString());//getCacheIconResource(gcit.getGeocache().getType()));
            }

            symbolLayerIconFeatureList.add(feature);
            builder.include(location);
        }
        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50), 2000);
        return FeatureCollection.fromFeatures(symbolLayerIconFeatureList);
    }


    /**
     * Sets up all of the sources and layers needed for this example
     *
     * @param collection the FeatureCollection to set equal to the globally-declared FeatureCollection
     */
    public void setUpData(final FeatureCollection collection) {
        featureCollection = collection;
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                setupSource(style);
                setUpImage(style);
                setUpMarkerLayer(style);
                setUpInfoWindowLayer(style);
                //setupLineCircleLayer(style);
            });
        }
    }

    /**
     * Adds the GeoJSON source to the map
     */
    private void setupSource(@NonNull Style loadedStyle) {

        source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
        loadedStyle.addSource(source);
    }

    /**
     * Adds the marker image to the map for use as a SymbolLayer icon
     */
    private void setUpImage(@NonNull Style loadedStyle) {


        for (CacheTypeEnum type : CacheTypeEnum.values()) {
            String typeString = type.getTypeString();
            if (typeString == null) break;
            loadedStyle.addImage(typeString, BitmapFactory.decodeResource(
                    MapActivity.this.getResources(), GeocacheIcon.getIconDrawable(typeString)));
        }
        // Add finds and dnfs as well
        loadedStyle.addImage(FoundEnumType.Found.getTypeString(), BitmapFactory.decodeResource(
                MapActivity.this.getResources(), R.drawable.cache_icon_found));
        loadedStyle.addImage(FoundEnumType.DNF.getTypeString(), BitmapFactory.decodeResource(
                MapActivity.this.getResources(), R.drawable.cache_icon_dnf));


    }

    /**
     * Setup a layer with maki icons, eg. west coast city.
     */
    private void setUpMarkerLayer(@NonNull Style loadedStyle) {

        loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(iconImage(match(get(PROPERTY_TYPE), literal(CacheTypeEnum.Traditional.getTypeString()),
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
    }

    /**
     * Setup a layer with Android SDK call-outs
     * <p>
     * name of the feature is used as key for the iconImage
     * </p>
     */
    private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
        loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        /* show image with id title based on the value of the name feature property */
                        iconImage("{name}"),

                        /* set anchor of icon to bottom-left */
                        iconAnchor(ICON_ANCHOR_BOTTOM),

                        /* all info window and marker image to appear at the same time*/
                        iconAllowOverlap(true),

                        /* offset the info window to be above the marker */
                        iconOffset(new Float[] {-2f, -28f})
                )
/* add a filter to show only when selected feature property is true */
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
    }

    private void setupLineCircleLayer(@NonNull Style loadedStyle){
        // Style and add the CircleLayer to the map
        loadedStyle.addLayer(new CircleLayer(CIRCLE_LAYER_ID, GEOJSON_SOURCE_ID).withProperties(
                circleColor(CIRCLE_COLOR),
                circleRadius(CIRCLE_RADIUS)
        ));

        // Style and add the LineLayer to the map. The LineLayer is placed below the CircleLayer.
        loadedStyle.addLayerBelow(new LineLayer(LINE_LAYER_ID, GEOJSON_SOURCE_ID).withProperties(
                        lineColor(LINE_COLOR),
                        lineWidth(LINE_WIDTH),
                        lineJoin(LINE_JOIN_ROUND)
                ), CIRCLE_LAYER_ID);
    }

    /**
     * AsyncTask to generate Bitmap from Views to be used as iconImage in a SymbolLayer.
     * <p>
     * Call be optionally be called to update the underlying data source after execution.
     * </p>
     * <p>
     * Generating Views on background thread since we are not going to be adding them to the view hierarchy.
     * </p>
     */
    private static class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

        private final HashMap<String, View> viewMap = new HashMap<>();
        private final WeakReference<MapActivity> activityRef;
        private final boolean refreshSource;

        GenerateViewIconTask(MapActivity activity, boolean refreshSource) {
            this.activityRef = new WeakReference<>(activity);
            this.refreshSource = refreshSource;
        }

        GenerateViewIconTask(MapActivity activity) {
            this(activity, false);
        }

        @SuppressWarnings("WrongThread")
        @Override
        protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
            MapActivity activity = activityRef.get();
            if (activity != null) {
                HashMap<String, Bitmap> imagesMap = new HashMap<>();
                LayoutInflater inflater = LayoutInflater.from(activity);

                FeatureCollection featureCollection = params[0];

                for (Feature feature : featureCollection.features()) {

                    BubbleLayout bubbleLayout = (BubbleLayout)
                            inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

                    String name = feature.getStringProperty(PROPERTY_NAME);
                    TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                    titleTextView.setText(name);

                    String difficulty = feature.getStringProperty(PROPERTY_DIFFICULTY);
                    String terrain = feature.getStringProperty(PROPERTY_TERRAIN);
                    TextView descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description);
                    descriptionTextView.setText("D: " + difficulty + ", T: " + terrain);

                    int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    bubbleLayout.measure(measureSpec, measureSpec);

                    float measuredWidth = bubbleLayout.getMeasuredWidth();

                    bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                    Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                    imagesMap.put(name, bitmap);
                    viewMap.put(name, bubbleLayout);
                }

                return imagesMap;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
            super.onPostExecute(bitmapHashMap);
            MapActivity activity = activityRef.get();
            if (activity != null && bitmapHashMap != null) {
                activity.setImageGenResults(bitmapHashMap);
                if (refreshSource) {
                    activity.refreshSource();
                }
            }
        }
    }

    /**
     * Invoked when the bitmaps have been generated from a view.
     */
    public void setImageGenResults(HashMap<String, Bitmap> imageMap) {
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
// calling addImages is faster as separate addImage calls for each bitmap.
                style.addImages(imageMap);
            });
        }
    }

    /**
     * Utility class to generate Bitmaps for Symbol.
     */
    private static class SymbolGenerator {

        /**
         * Generate a Bitmap from an Android SDK View.
         *
         * @param view the View to be drawn to a Bitmap
         * @return the generated bitmap
         */
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }

}
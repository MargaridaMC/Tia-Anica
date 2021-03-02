package net.teamtruta.tiaires;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

// classes needed to initialize map
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
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import net.teamtruta.tiaires.db.DbConnection;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import java.util.ArrayList;
import java.util.Objects;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;

    // variables for adding location layer
    private PermissionsManager permissionsManager;

    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    private static final String FULL_TOUR_LINE_GEOJSON_SOURCE_ID = "FULL_TOUR_LINE_GEOJSON_SOURCE_ID";
    private static final String REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID = "REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID";
    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String FULL_TOUR_LINE_LAYER_ID = "FULL_TOUR_LINE_LAYER_ID";
    private static final String REMAINING_TOUR_LINE_LAYER_ID = "REMAINING_TOUR_LINE_LAYER_ID";

    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_DIFFICULTY = "difficulty";
    private static final String PROPERTY_TERRAIN = "terrain";
    private static final String PROPERTY_FAVOURITES = "favourites";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String PROPERTY_CODE = "code";
    private static final int LINE_COLOR = Color.RED;
    private static final float LINE_WIDTH = 4f;

    //private static final String DISTANCE_UNITS = UNIT_KILOMETERS;


    String TAG = MapActivity.class.getSimpleName();
    GeocachingTour _tour;
    long tourID;
    DbConnection dbConnection;

    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    List<Point> routeCoordinates;
    private static final HashMap<String, View> viewMap = new HashMap<>();
    private double fullTourDistance;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(_tour.getName());

        // Set click listener for the tour distance checkbox
        setTourDistanceCheckBox();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        getData();
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            setUpData(featureCollection);
            enableLocationComponent(style);
            mapboxMap.addOnMapClickListener(MapActivity.this);
        });

        new GenerateViewIconTask(MapActivity.this).execute(featureCollection);

        // Set my location FAB onClickListener
        setupMyLocationFAB();

        // Compute the full distance of the tour
        fullTourDistance = computeTourDistance(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    private void setupMyLocationFAB() {

        Activity context = this;
        findViewById(R.id.my_location_fab).setOnClickListener(view -> {

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            try {
                assert locationComponent.getLastKnownLocation() != null;
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
                        enableLocationComponent(Objects.requireNonNull(mapboxMap.getStyle()));

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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
            enableLocationComponent(Objects.requireNonNull(mapboxMap.getStyle()));
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

    private LatLng convertToLatLng(Feature feature) {
        Point symbolPoint = (Point) feature.geometry();
        assert symbolPoint != null;
        return new LatLng(symbolPoint.latitude(), symbolPoint.longitude());
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
                        return true;
                    }
                }
            }

        } else {

            List<Feature> infoWindows = mapboxMap.queryRenderedFeatures(
                    screenPoint, CALLOUT_LAYER_ID);

            if(!infoWindows.isEmpty()){

                Feature window = infoWindows.get(0);

                String name = window.getStringProperty(PROPERTY_NAME);
                PointF symbolScreenPoint =
                        mapboxMap.getProjection().toScreenLocation(convertToLatLng(window));

                View view = viewMap.get(name);
                assert view != null;
                View button = view.findViewById(R.id.go_to_button);
                // create hitbox for button
                Rect hitRectText = new Rect();
                button.getHitRect(hitRectText);
                // move hitbox to location of symbol
                hitRectText.offset((int) symbolScreenPoint.x, (int) symbolScreenPoint.y);
                // offset to consider box's size
                hitRectText.offset(- view.getWidth() / 2, - view.getHeight() - 50);

                if (hitRectText.contains((int) screenPoint.x, (int) screenPoint.y)) {
                    // user clicked on marker
                    goToGeoCache( window.getStringProperty(PROPERTY_CODE));
                    return true;
                }
            }
        }

        return false;

    }

    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        assert featureCollection.features() != null;
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

    public void getData(){

        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        routeCoordinates = new ArrayList<>();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng location;
        for (GeoCacheInTour gcit : _tour._tourGeoCaches) {
            location = gcit.getGeoCache().getLatLng();

            Point p = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            routeCoordinates.add(p);

            Feature feature = Feature.fromGeometry(p);

            feature.addStringProperty(PROPERTY_NAME, gcit.getGeoCache().getName());
            feature.addStringProperty(PROPERTY_DIFFICULTY, Double.toString(gcit.getGeoCache().getDifficulty()));
            feature.addStringProperty(PROPERTY_TERRAIN, Double.toString(gcit.getGeoCache().getTerrain()));
            feature.addStringProperty(PROPERTY_FAVOURITES, Integer.toString(gcit.getGeoCache().getFavourites()));
            feature.addStringProperty(PROPERTY_CODE, gcit.getGeoCache().getCode());
            feature.addBooleanProperty(PROPERTY_SELECTED, false);

            if (gcit.getCurrentVisitOutcome() == VisitOutcomeEnum.Found || gcit.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF) {
                feature.addStringProperty(PROPERTY_TYPE, gcit.getCurrentVisitOutcome().getVisitOutcomeString());
            } else {
                feature.addStringProperty(PROPERTY_TYPE, gcit.getGeoCache().getType().getTypeString());
            }

            symbolLayerIconFeatureList.add(feature);
            builder.include(location);
        }

        // If there is only one geocache in the list the LatLngBoundsBuilder will fail to build-
        // If that is the case just focus on the geocache
        if( _tour._tourGeoCaches.size() == 1){
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLng(_tour._tourGeoCaches.get(0).getGeoCache().getLatLng()), 2000);
        } else {
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50), 2000);
        }

        featureCollection = FeatureCollection.fromFeatures(symbolLayerIconFeatureList);
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
                setupLineLayer(style);
            });
        }
    }

    /**
     * Adds the GeoJSON source to the map
     */
    private void setupSource(@NonNull Style loadedStyle) {

        // Set source for geocaches
        source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
        loadedStyle.addSource(source);

        // Set source for route lines for whole tour
        FeatureCollection lineFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                LineString.fromLngLats(routeCoordinates))});
        loadedStyle.addSource(new GeoJsonSource(FULL_TOUR_LINE_GEOJSON_SOURCE_ID, lineFeatureCollection));

        // Set source for route lines for remaining tour
        int lastVisitedGeoCache = _tour.getLastVisitedGeoCache();
        List<Point> remainingRouteCoordinates;
        if(lastVisitedGeoCache == 0){
            remainingRouteCoordinates = routeCoordinates.subList(0, routeCoordinates.size());
        } else {
            remainingRouteCoordinates = routeCoordinates.subList(lastVisitedGeoCache - 1, routeCoordinates.size());
        }
        lineFeatureCollection = FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                LineString.fromLngLats(remainingRouteCoordinates))});
        loadedStyle.addSource(new GeoJsonSource(REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID, lineFeatureCollection));
    }

    /**
     * Adds the marker image to the map for use as a SymbolLayer icon
     */
    private void setUpImage(@NonNull Style loadedStyle) {


        for (GeoCacheTypeEnum type : GeoCacheTypeEnum.values()) {
            String typeString = type.getTypeString();
            loadedStyle.addImage(typeString, BitmapFactory.decodeResource(
                    MapActivity.this.getResources(), GeoCacheIcon.Companion.getIconDrawable(typeString)));
        }
        // Add finds and dnfs as well
        loadedStyle.addImage(VisitOutcomeEnum.Found.getVisitOutcomeString(), BitmapFactory.decodeResource(
                MapActivity.this.getResources(), R.drawable.geo_cache_icon_found));
        loadedStyle.addImage(VisitOutcomeEnum.DNF.getVisitOutcomeString(), BitmapFactory.decodeResource(
                MapActivity.this.getResources(), R.drawable.geo_cache_icon_dnf));


    }

    /**
     * Setup a layer with maki icons, eg. west coast city.
     */
    private void setUpMarkerLayer(@NonNull Style loadedStyle) {

        loadedStyle.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(iconImage(match(get(PROPERTY_TYPE), literal(GeoCacheTypeEnum.Traditional.getTypeString()),
                        stop(GeoCacheTypeEnum.Traditional.getTypeString(), GeoCacheTypeEnum.Traditional.getTypeString()),
                        stop(GeoCacheTypeEnum.Mystery.getTypeString(), GeoCacheTypeEnum.Mystery.getTypeString()),
                        stop(GeoCacheTypeEnum.Solved.getTypeString(), GeoCacheTypeEnum.Solved.getTypeString()),
                        stop(GeoCacheTypeEnum.Multi.getTypeString(), GeoCacheTypeEnum.Multi.getTypeString()),
                        stop(GeoCacheTypeEnum.Earth.getTypeString(), GeoCacheTypeEnum.Earth.getTypeString()),
                        stop(GeoCacheTypeEnum.Letterbox.getTypeString(), GeoCacheTypeEnum.Letterbox.getTypeString()),
                        stop(GeoCacheTypeEnum.Event.getTypeString(), GeoCacheTypeEnum.Event.getTypeString()),
                        stop(GeoCacheTypeEnum.CITO.getTypeString(), GeoCacheTypeEnum.CITO.getTypeString()),
                        stop(GeoCacheTypeEnum.Mega.getTypeString(), GeoCacheTypeEnum.Mega.getTypeString()),
                        stop(GeoCacheTypeEnum.Giga.getTypeString(), GeoCacheTypeEnum.Giga.getTypeString()),
                        stop(GeoCacheTypeEnum.HQ.getTypeString(), GeoCacheTypeEnum.HQ.getTypeString()),
                        stop(GeoCacheTypeEnum.GPSAdventures.getTypeString(), GeoCacheTypeEnum.GPSAdventures.getTypeString()),
                        stop(GeoCacheTypeEnum.Lab.getTypeString(), GeoCacheTypeEnum.Lab.getTypeString()),
                        stop(GeoCacheTypeEnum.HQCelebration.getTypeString(), GeoCacheTypeEnum.HQCelebration.getTypeString()),
                        stop(GeoCacheTypeEnum.HQBlockParty.getTypeString(), GeoCacheTypeEnum.HQBlockParty.getTypeString()),
                        stop(GeoCacheTypeEnum.CommunityCelebration.getTypeString(), GeoCacheTypeEnum.CommunityCelebration.getTypeString()),
                        stop(GeoCacheTypeEnum.Virtual.getTypeString(), GeoCacheTypeEnum.Virtual.getTypeString()),
                        stop(GeoCacheTypeEnum.Webcam.getTypeString(), GeoCacheTypeEnum.Webcam.getTypeString()),
                        stop(GeoCacheTypeEnum.ProjectAPE.getTypeString(), GeoCacheTypeEnum.ProjectAPE.getTypeString()),
                        stop(GeoCacheTypeEnum.Locationless.getTypeString(), GeoCacheTypeEnum.Locationless.getTypeString()),
                        stop(VisitOutcomeEnum.Found.getVisitOutcomeString(), VisitOutcomeEnum.Found.getVisitOutcomeString()),
                        stop(VisitOutcomeEnum.DNF.getVisitOutcomeString(), VisitOutcomeEnum.DNF.getVisitOutcomeString()))),
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
                        iconOffset(new Float[] {-2f, -20f})
                )
/* add a filter to show only when selected feature property is true */
                .withFilter(eq((get(PROPERTY_SELECTED)), literal(true))));
    }

    private void setupLineLayer(@NonNull Style loadedStyle){

        // Style and add the LineLayer to the map. The LineLayer is placed below the CircleLayer.
        loadedStyle.addLayerBelow(new LineLayer(FULL_TOUR_LINE_LAYER_ID, FULL_TOUR_LINE_GEOJSON_SOURCE_ID).withProperties(
                    lineColor(LINE_COLOR),
                    lineWidth(LINE_WIDTH),
                    lineJoin(LINE_JOIN_ROUND),
                    visibility(NONE)
                ), MARKER_LAYER_ID);

        loadedStyle.addLayerBelow(new LineLayer(REMAINING_TOUR_LINE_LAYER_ID, REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID).withProperties(
                lineColor(LINE_COLOR),
                lineWidth(LINE_WIDTH),
                lineJoin(LINE_JOIN_ROUND),
                visibility(NONE)
        ), MARKER_LAYER_ID);
    }

    private void setTourDistanceCheckBox() {
        TriStatesCheckBox checkBox = findViewById(R.id.distance_checkbox);
        checkBox.setOnClickListener(v -> {
            switch (checkBox.getState()){
                case(TriStatesCheckBox.INDETERMINATE):
                    showRemainingTourLineLayer();
                    break;
                case(TriStatesCheckBox.CHECKED):
                    hideRemainingTourLineLayer();
                    showFullTourLineLayer();
                    break;
                case(TriStatesCheckBox.UNCHECKED):
                    hideFullTourLineLayer();
                    break;
                default:
                    hideRemainingTourLineLayer();
                    hideFullTourLineLayer();
                    break;
            }
        });
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
                    TextView titleTextView = bubbleLayout.findViewById(R.id.geo_cache_title);
                    titleTextView.setText(name);

                    TextView descriptionTextView = bubbleLayout.findViewById(R.id.geo_cache_description);
                    descriptionTextView.setText(String.format(activity.getString(R.string.geo_cache_description_box),
                            feature.getStringProperty(PROPERTY_DIFFICULTY),
                            feature.getStringProperty(PROPERTY_TERRAIN),
                            feature.getStringProperty(PROPERTY_FAVOURITES)));

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

    public void goToGeoCache(String code) {

        String url = "https://coord.info/" + code;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
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

    private void showFullTourLineLayer(){
        mapboxMap.getStyle(style -> {
            Layer layer = style.getLayer(FULL_TOUR_LINE_LAYER_ID);
            assert layer != null;
            layer.setProperties(visibility(VISIBLE));
        });

        showFullTourDistance();
    }

    private void hideFullTourLineLayer(){
        mapboxMap.getStyle(style -> {
            Layer layer = style.getLayer(FULL_TOUR_LINE_LAYER_ID);
            assert layer != null;
            layer.setProperties(visibility(NONE));
        });

        findViewById(R.id.line_distance).setVisibility(View.INVISIBLE);
    }

    private void showRemainingTourLineLayer(){

        mapboxMap.getStyle(style -> {
            Layer layer = style.getLayer(REMAINING_TOUR_LINE_LAYER_ID);
            assert layer != null;
            layer.setProperties(visibility(VISIBLE));
        });

        showRemainingTourDistance();
    }

    private void hideRemainingTourLineLayer(){

        mapboxMap.getStyle(style -> {
            Layer layer = style.getLayer(REMAINING_TOUR_LINE_LAYER_ID);
            assert layer != null;
            layer.setProperties(visibility(NONE));
        });

        findViewById(R.id.line_distance).setVisibility(View.INVISIBLE);
    }

    private void showFullTourDistance(){
        TextView lineLengthTextView = findViewById(R.id.line_distance);
        lineLengthTextView.setVisibility(View.VISIBLE);

        lineLengthTextView.setText(String.format(getString(R.string.full_tour_distance),
                fullTourDistance, "kms"));
    }

    private void showRemainingTourDistance(){
        TextView lineLengthTextView = findViewById(R.id.line_distance);
        lineLengthTextView.setVisibility(View.VISIBLE);

        lineLengthTextView.setText(String.format(getString(R.string.remaining_tour_distance),
                computeTourDistance(_tour.getLastVisitedGeoCache()), "kms"));
    }


    private double computeTourDistance(int startGeoCacheIDX ){
        double distance = 0;

        if(startGeoCacheIDX == 0)
            startGeoCacheIDX = 1;

        for(int i = startGeoCacheIDX; i < routeCoordinates.size(); i++){
            distance += TurfMeasurement.distance(routeCoordinates.get(i), routeCoordinates.get(i - 1));
        }

        return distance;
    }

    public void set_current_location_as_start_point(View view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Set the current location as the start and finish point of the tour?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Set current point as starting point of the tour
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {});

        dialogBuilder.create().show();

    }

}
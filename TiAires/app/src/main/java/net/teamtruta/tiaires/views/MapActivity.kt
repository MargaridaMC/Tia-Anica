package net.teamtruta.tiaires.views

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.BubbleLayout
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import net.teamtruta.tiaires.databinding.ActivityMapBinding
import kotlinx.coroutines.*
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.BuildConfig
import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheTypeEnum
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum
import net.teamtruta.tiaires.extensions.GeoCacheIcon.Companion.getIconDrawable
import net.teamtruta.tiaires.extensions.TriStatesCheckBox
import net.teamtruta.tiaires.viewModels.MapActivityViewModel
import net.teamtruta.tiaires.viewModels.MapActivityViewModelFactory
import java.util.*


// classes needed to initialize map
// classes needed to add the location component
// classes needed to add a marker
class MapActivity : AppCompatActivity(), OnMapReadyCallback, OnMapClickListener, PermissionsListener {
    // variables for adding location layer
    //private val mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    // variables for adding location layer
    private var permissionsManager: PermissionsManager? = null
    private var TAG = MapActivity::class.java.simpleName
    private var _tour: GeocachingTourWithCaches? = null
    private var geoCacheSource: GeoJsonSource? = null
    private var waypointSource: GeoJsonSource? = null
    private var featureCollection: FeatureCollection? = null
    private var waypointFeatureCollection: FeatureCollection? = null
    private var routeCoordinates: MutableList<Point?> = ArrayList()
    private var _startingPoint: Point? = null
    private val viewModel: MapActivityViewModel by viewModels {
        MapActivityViewModelFactory((application as App).repository)
    }


    private val GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID"
    private val FULL_TOUR_LINE_GEOJSON_SOURCE_ID = "FULL_TOUR_LINE_GEOJSON_SOURCE_ID"
    private val REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID = "REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID"
    private val MARKER_LAYER_ID = "MARKER_LAYER_ID"
    private val CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID"
    private val FULL_TOUR_LINE_LAYER_ID = "FULL_TOUR_LINE_LAYER_ID"
    private val REMAINING_TOUR_LINE_LAYER_ID = "REMAINING_TOUR_LINE_LAYER_ID"
    private val PROPERTY_GEOCACHE_NAME = "name"
    private val PROPERTY_TYPE = "type"
    private val PROPERTY_DIFFICULTY = "difficulty"
    private val PROPERTY_TERRAIN = "terrain"
    private val PROPERTY_FAVOURITES = "favourites"
    private val PROPERTY_SELECTED = "selected"
    private val PROPERTY_CODE = "code"
    private val LINE_COLOR = Color.RED
    private val LINE_WIDTH = 2f
    private val STARTING_POINT_ICON_ID = "STARTING_POINT_ICON_ID"
    private val STARTING_POINT_SOURCE_ID = "STARTING_POINT_SOURCE_ID"
    private val STARTING_POINT_SYMBOL_LAYER_ID = "STARTING_POINT_SYMBOL_LAYER_ID"

    // Properties for Waypoints
    private val PROPERTY_WAYPOINT_NAME = "WAYPOINT_NAME"
    private val WAYPOINT_GEOJSON_SOURCE_ID = "WAYPOINT_GEOJSON_SOURCE_ID"
    private val WAYPOINT_MARKER_LAYER = "WAYPOINT_MARKER_LAYER"
    private val WAYPOINT_CALLOUT_LAYER_ID = "WAYPOINT_CALLOUT_LAYER_ID"
    private val PROPERTY_WAYPOINT_TYPE = "PROPERTY_WAYPOINT_TYPE"
    private val WAYPOINT_PARKING_TYPE = "parkingWaypoint"
    private val WAYPOINT_DONE_TYPE = "doneWaypoint"
    private val WAYPOINT_NORMAL_TYPE = "normalWaypoint"

    private val viewMap = HashMap<String, View>()
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        // Setup bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayHomeAsUpEnabled(true)

        // Set click listener for the tour distance checkbox
        setTourDistanceCheckBox()
    }

    // Method called when map is done loading
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        viewModel.currentTour.observe(this,
                { tour: GeocachingTourWithCaches? -> setTourRelatedData(tour) })

        // Set my location FAB onClickListener
        setupMyLocationFAB()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.map_menu, menu);
        return true
    }

    /***
     * MapBox specific methods
     */
    override fun onStart() {
        super.onStart()
        binding.mapView!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView!!.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView!!.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView!!.onLowMemory()
    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap!!.projection.toScreenLocation(point))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(Objects.requireNonNull(mapboxMap!!.style)!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // click on icon to go back
        //triangle icon on the main android toolbar.
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed Called")
        val intent = Intent(this, TourActivity::class.java)
        //        intent.putExtra(App.TOUR_ID_EXTRA, tourID);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun setTourRelatedData(tour: GeocachingTourWithCaches?) {
        _tour = tour

        // Set title
        val ab = supportActionBar
        ab!!.title = _tour!!.tour.name
        setupMapData()

        generateViewIcon()

        mapboxMap!!.setStyle(Style.MAPBOX_STREETS) { style: Style ->
            setUpData(featureCollection)
            enableLocationComponent(style)
            mapboxMap!!.addOnMapClickListener(this@MapActivity)
        }

        // Show remaining tour line and distance
        showRemainingTourDistance()
        showRemainingTourLineLayer()
    }

    private fun setupMyLocationFAB() {
        findViewById<View>(R.id.my_location_fab).setOnClickListener {
            val locationComponent = mapboxMap!!.locationComponent
            try {
                if (BuildConfig.DEBUG && locationComponent.lastKnownLocation == null) {
                    error("Assertion failed")
                }
                mapboxMap!!.easeCamera(CameraUpdateFactory.newLatLng(LatLng(
                        locationComponent.lastKnownLocation!!.latitude,
                        locationComponent.lastKnownLocation!!.longitude)), 2500)
            } catch (e: Exception) {
                checkLocationPermissionIsGrantedAndGPSIsOn()
            }
        }
    }


    /**
     * Defines the coordinates of all elements to be shown on the map
     */
    private fun setupMapData(){

        // Get cache coordinates and their properties
        val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
        val builder = LatLngBounds.Builder()
        var location: LatLng
        for (gcit in _tour!!.tourGeoCaches) {
            val geoCacheInTour = gcit.geoCacheInTour
            val geoCacheDetails = gcit.geoCache.geoCache
            location = geoCacheDetails.latLng
            val p = Point.fromLngLat(location.longitude, location.latitude)
            routeCoordinates.add(p)
            val feature = Feature.fromGeometry(p)
            feature.addStringProperty(PROPERTY_GEOCACHE_NAME, geoCacheDetails.name)
            feature.addStringProperty(PROPERTY_DIFFICULTY, geoCacheDetails.difficulty.toString())
            feature.addStringProperty(PROPERTY_TERRAIN, geoCacheDetails.terrain.toString())
            feature.addStringProperty(PROPERTY_FAVOURITES, geoCacheDetails.favourites.toString())
            feature.addStringProperty(PROPERTY_CODE, geoCacheDetails.code)
            feature.addBooleanProperty(PROPERTY_SELECTED, false)
            if (geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.Found || geoCacheInTour.currentVisitOutcome === VisitOutcomeEnum.DNF) {
                feature.addStringProperty(PROPERTY_TYPE, geoCacheInTour.currentVisitOutcome.visitOutcomeString)
            } else {
                feature.addStringProperty(PROPERTY_TYPE, geoCacheDetails.type.typeString)
            }
            symbolLayerIconFeatureList.add(feature)
            builder.include(location)
        }
        featureCollection = FeatureCollection.fromFeatures(symbolLayerIconFeatureList)

        // Get starting point if there is one
        if (_tour!!.tour.startingPointLongitude != null) {
            _startingPoint = Point.fromLngLat(_tour!!.tour.startingPointLongitude!!.value,
                    _tour!!.tour.startingPointLatitude!!.value)

            // If we have a starting point add it in the beginning and the end of the route
            routeCoordinates.add(_startingPoint)
            routeCoordinates.add(0, _startingPoint)
        }

        // Get waypoint information if there are any waypoints
        var numberOfPointsInMap = _tour!!.tourGeoCaches.size
        val waypointFeatureList: MutableList<Feature> = ArrayList()
        for (gcit in _tour!!.tourGeoCaches) {

            val geoCacheInTourLatLng = gcit.geoCache.geoCache.latLng
            val geoCacheInTourCoordinatePoint = Point.fromLngLat(geoCacheInTourLatLng.longitude, geoCacheInTourLatLng.latitude)

            for (waypoint in gcit.geoCache.waypoints){

                if(waypoint.latitude == null || waypoint.longitude ==  null){
                    continue
                }
                if(waypoint.isParking) continue

                val p = Point.fromLngLat(waypoint.longitude!!.value, waypoint.latitude!!.value)
                // If the waypoint coincides with the GZ don't show it on the map
                if (geoCacheInTourCoordinatePoint == p) continue

                val feature = Feature.fromGeometry(p)
                feature.addStringProperty(PROPERTY_GEOCACHE_NAME, gcit.geoCache.geoCache.name)
                feature.addStringProperty(PROPERTY_CODE, gcit.geoCache.geoCache.code)
                feature.addStringProperty(PROPERTY_WAYPOINT_NAME, waypoint.name)
                feature.addBooleanProperty(PROPERTY_SELECTED, false)

                when {
                    waypoint.isParking -> {
                        feature.addStringProperty(PROPERTY_WAYPOINT_TYPE, WAYPOINT_PARKING_TYPE)
                    }
                    waypoint.isDone() -> {
                        feature.addStringProperty(PROPERTY_WAYPOINT_TYPE, WAYPOINT_DONE_TYPE)
                    }
                    else -> {
                        feature.addStringProperty(PROPERTY_WAYPOINT_TYPE, WAYPOINT_NORMAL_TYPE)
                    }
                }

                waypointFeatureList.add(feature)

                location = LatLng(waypoint.latitude!!.value, waypoint.longitude!!.value)
                builder.include(location)
            }

            numberOfPointsInMap += gcit.geoCache.waypoints.size
        }
        waypointFeatureCollection = FeatureCollection.fromFeatures(waypointFeatureList)

        // Center camera in such a way that we can see all points in the map
        // If there is only one geocache in the list the LatLngBoundsBuilder will fail to build-
        // If that is the case just focus on the geocache
        if (numberOfPointsInMap == 1) {
            mapboxMap!!.easeCamera(CameraUpdateFactory.newLatLng(_tour!!.tourGeoCaches[0].geoCache.geoCache.latLng), 2000)
        } else {
            mapboxMap!!.easeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50), 2000)
        }
    }

    /**
     * Sets up all of the sources and layers needed for this example
     *
     * @param collection the FeatureCollection to set equal to the globally-declared FeatureCollection
     */
    private fun setUpData(collection: FeatureCollection?) {
        featureCollection = collection
        if (mapboxMap != null) {
            mapboxMap!!.getStyle { style: Style ->
                setupSource(style)
                setUpImage(style)
                setUpMarkerLayer(style)
                setUpStartingPointLayer(style)
                setUpInfoWindowLayer(style)
                setupLineLayer(style)
                setUpWaypointLayer(style)
            }
        }
    }

    /**
     * Adds the GeoJSON source to the map
     */
    private fun setupSource(loadedStyle: Style) {

        // Set source for geocaches
        geoCacheSource = GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection)
        loadedStyle.addSource(geoCacheSource!!)

        // Set source for starting point
        if (_startingPoint != null) {
            loadedStyle.addSource(GeoJsonSource(STARTING_POINT_SOURCE_ID,
                    FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(_startingPoint)))))
        }

        // Set source for route lines for whole tour
        val route = arrayOf(Feature.fromGeometry(
                LineString.fromLngLats(routeCoordinates)))
        var lineFeatureCollection = FeatureCollection.fromFeatures(route)
        loadedStyle.addSource(GeoJsonSource(FULL_TOUR_LINE_GEOJSON_SOURCE_ID, lineFeatureCollection))

        // Set source for route lines for remaining tour
        val lastVisitedGeoCache = _tour!!.getLastVisitedGeoCache()
        val remainingRouteCoordinates: List<Point?> = if (lastVisitedGeoCache == -1) {
            routeCoordinates.subList(0, routeCoordinates.size)
        } else {
            if (_tour!!.tour.startingPointLongitude != null) {
                routeCoordinates.subList(lastVisitedGeoCache + 1, routeCoordinates.size)
            } else routeCoordinates.subList(lastVisitedGeoCache, routeCoordinates.size)
        }
        lineFeatureCollection = FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(
                LineString.fromLngLats(remainingRouteCoordinates))))
        loadedStyle.addSource(GeoJsonSource(REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID, lineFeatureCollection))

        // Set source for waypoint layer
        waypointSource = GeoJsonSource(WAYPOINT_GEOJSON_SOURCE_ID, waypointFeatureCollection)
        loadedStyle.addSource(waypointSource!!)

    }

    /**
     * Adds the cache icons
     */
    private fun setUpImage(loadedStyle: Style) {
        for (type in GeoCacheTypeEnum.values()) {
            val typeString = type.typeString
            loadedStyle.addImage(typeString, BitmapFactory.decodeResource(
                    this@MapActivity.resources, getIconDrawable(typeString)))
        }
        // Add finds and dnfs as well
        loadedStyle.addImage(VisitOutcomeEnum.Found.visitOutcomeString, BitmapFactory.decodeResource(
                this@MapActivity.resources, R.drawable.geo_cache_icon_found))
        loadedStyle.addImage(VisitOutcomeEnum.DNF.visitOutcomeString, BitmapFactory.decodeResource(
                this@MapActivity.resources, R.drawable.geo_cache_icon_dnf))

    }

    /**
     * setup marker layer
     */
    private fun setUpMarkerLayer(loadedStyle: Style) {
        loadedStyle.addLayer(SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                        PropertyFactory.iconImage(
                                Expression.match(
                                        Expression.get(PROPERTY_TYPE), // Property to get
                                        Expression.literal(GeoCacheTypeEnum.Traditional.typeString), // Default value
                                        Expression.stop(GeoCacheTypeEnum.Traditional.typeString, GeoCacheTypeEnum.Traditional.typeString), // Options
                                        Expression.stop(GeoCacheTypeEnum.Mystery.typeString, GeoCacheTypeEnum.Mystery.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Solved.typeString, GeoCacheTypeEnum.Solved.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Multi.typeString, GeoCacheTypeEnum.Multi.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Earth.typeString, GeoCacheTypeEnum.Earth.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Letterbox.typeString, GeoCacheTypeEnum.Letterbox.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Wherigo.typeString, GeoCacheTypeEnum.Wherigo.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Event.typeString, GeoCacheTypeEnum.Event.typeString),
                                        Expression.stop(GeoCacheTypeEnum.CITO.typeString, GeoCacheTypeEnum.CITO.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Mega.typeString, GeoCacheTypeEnum.Mega.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Giga.typeString, GeoCacheTypeEnum.Giga.typeString),
                                        Expression.stop(GeoCacheTypeEnum.HQ.typeString, GeoCacheTypeEnum.HQ.typeString),
                                        Expression.stop(GeoCacheTypeEnum.GPSAdventures.typeString, GeoCacheTypeEnum.GPSAdventures.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Lab.typeString, GeoCacheTypeEnum.Lab.typeString),
                                        Expression.stop(GeoCacheTypeEnum.HQCelebration.typeString, GeoCacheTypeEnum.HQCelebration.typeString),
                                        Expression.stop(GeoCacheTypeEnum.HQBlockParty.typeString, GeoCacheTypeEnum.HQBlockParty.typeString),
                                        Expression.stop(GeoCacheTypeEnum.CommunityCelebration.typeString, GeoCacheTypeEnum.CommunityCelebration.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Virtual.typeString, GeoCacheTypeEnum.Virtual.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Webcam.typeString, GeoCacheTypeEnum.Webcam.typeString),
                                        Expression.stop(GeoCacheTypeEnum.ProjectAPE.typeString, GeoCacheTypeEnum.ProjectAPE.typeString),
                                        Expression.stop(GeoCacheTypeEnum.Locationless.typeString, GeoCacheTypeEnum.Locationless.typeString),
                                        Expression.stop(VisitOutcomeEnum.Found.visitOutcomeString, VisitOutcomeEnum.Found.visitOutcomeString),
                                        Expression.stop(VisitOutcomeEnum.DNF.visitOutcomeString, VisitOutcomeEnum.DNF.visitOutcomeString)
                                )
                        ),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER),
                        PropertyFactory.iconSize(0.4f))
        )

    }

    /**
     * Add layer with starting point symbol
     */
    private fun setUpStartingPointLayer(loadedStyle: Style){
        if (_startingPoint != null) {

            loadedStyle.addImage(STARTING_POINT_ICON_ID,
                        BitmapFactory.decodeResource(this@MapActivity.resources, R.drawable.home_filled_green))

            loadedStyle.addLayer(SymbolLayer(STARTING_POINT_SYMBOL_LAYER_ID, STARTING_POINT_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.iconImage(STARTING_POINT_ICON_ID),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                            PropertyFactory.iconSize(0.5f)
                    ))
        }
    }

    /**
     * Add layer with cache info
     */
    private fun setUpInfoWindowLayer(loadedStyle: Style) {
        loadedStyle.addLayer(SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties( /* show image with id title based on the value of the name feature property */
                        PropertyFactory.iconImage("{name}"),  /* set anchor of icon to bottom-left */
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),  /* all info window and marker image to appear at the same time*/
                        PropertyFactory.iconAllowOverlap(true),  /* offset the info window to be above the marker */
                        PropertyFactory.iconOffset(arrayOf(-2f, -20f))
                ) /* add a filter to show only when selected feature property is true */
                .withFilter(Expression.eq(Expression.get(PROPERTY_SELECTED), Expression.literal(true))))
    }

    /**
     * Add layer with route lines
     */
    private fun setupLineLayer(loadedStyle: Style) {

        // Style and add the LineLayer to the map. The LineLayer is placed below the CircleLayer.
        loadedStyle.addLayerBelow(LineLayer(FULL_TOUR_LINE_LAYER_ID, FULL_TOUR_LINE_GEOJSON_SOURCE_ID).withProperties(
                PropertyFactory.lineColor(LINE_COLOR),
                PropertyFactory.lineWidth(LINE_WIDTH),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.visibility(Property.NONE)
        ), MARKER_LAYER_ID)
        loadedStyle.addLayerBelow(LineLayer(REMAINING_TOUR_LINE_LAYER_ID, REMAINING_TOUR_LINE_GEOJSON_SOURCE_ID).withProperties(
                PropertyFactory.lineColor(LINE_COLOR),
                PropertyFactory.lineWidth(LINE_WIDTH),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.visibility(Property.NONE)
        ), MARKER_LAYER_ID)
    }

    private fun setUpWaypointLayer(loadedStyle: Style){

        val WAYPOINT_ICON_ID = "WAYPOINT_ICON_ID"
        val WAYPOINT_DONE_ICON_ID = "WAYPOINT_DONE_ICON_ID"
        val WAYPOINT_PARKING_ICON_ID = "WAYPOINT_PARKING_ICON_ID"

        loadedStyle.addImage(WAYPOINT_ICON_ID,
                BitmapFactory.decodeResource(this@MapActivity.resources, R.drawable.waypoint_icon))
        loadedStyle.addImage(WAYPOINT_DONE_ICON_ID, BitmapFactory.decodeResource(
                this@MapActivity.resources, R.drawable.waypoint_done_checkmark))
        loadedStyle.addImage(WAYPOINT_PARKING_ICON_ID, BitmapFactory.decodeResource(
                this@MapActivity.resources, R.drawable.waypoint_parking_icon))

        loadedStyle.addLayer(SymbolLayer(WAYPOINT_MARKER_LAYER, WAYPOINT_GEOJSON_SOURCE_ID)
                .withProperties(
                        PropertyFactory.iconImage(
                                Expression.match(
                                        Expression.get(PROPERTY_WAYPOINT_TYPE), // Property to get
                                        Expression.literal(WAYPOINT_NORMAL_TYPE), // Default value
                                        Expression.stop(WAYPOINT_NORMAL_TYPE, WAYPOINT_ICON_ID),
                                        Expression.stop(WAYPOINT_PARKING_TYPE, WAYPOINT_PARKING_ICON_ID),
                                        Expression.stop(WAYPOINT_DONE_TYPE, WAYPOINT_DONE_ICON_ID),

                                )
                        ),

                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                        PropertyFactory.iconSize(0.5f)
                ))


        loadedStyle.addLayer(SymbolLayer(WAYPOINT_CALLOUT_LAYER_ID, WAYPOINT_GEOJSON_SOURCE_ID)
                .withProperties( /* show image with id title based on the value of the name feature property */
                        PropertyFactory.iconImage("{$PROPERTY_GEOCACHE_NAME}{$PROPERTY_WAYPOINT_NAME}"),  /* set anchor of icon to bottom-left */
                        PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),  /* all info window and marker image to appear at the same time*/
                        PropertyFactory.iconAllowOverlap(true),  /* offset the info window to be above the marker */
                        PropertyFactory.iconOffset(arrayOf(-2f, -20f))
                ) /* add a filter to show only when selected feature property is true */
                .withFilter(Expression.eq(Expression.get(PROPERTY_SELECTED), Expression.literal(true))))
    }

    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            val locationComponent = mapboxMap!!.locationComponent
            locationComponent.activateLocationComponent(this, loadedMapStyle)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationComponent.isLocationComponentEnabled = true
            // Set the component's camera mode
            //locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    private fun convertToLatLng(feature: Feature): LatLng {
        val symbolPoint = (feature.geometry() as Point?)!!
        return LatLng(symbolPoint.latitude(), symbolPoint.longitude())
    }

    private fun handleClickIcon(screenPoint: PointF): Boolean {

        // Display text box with info
        val clickedCacheFeature = mapboxMap!!.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID)
        if (clickedCacheFeature.isNotEmpty()) {
            val name = clickedCacheFeature[0].getStringProperty(PROPERTY_GEOCACHE_NAME)
            val featureList = featureCollection!!.features()
            if (featureList != null) {
                for (i in featureList.indices) {
                    if (featureList[i].getStringProperty(PROPERTY_GEOCACHE_NAME) == name) { // If the feature that was clicked on matches this one
                        if (featureSelectStatus(i, featureCollection)) {
                            setFeatureSelectState(featureList[i], false)
                        } else {
                            setFeatureSelectState(featureList[i], true)
                        }
                        return true
                    }
                }
            }
        } else {
                // If the user didn't click on a cache check if they clicked on a waypoint
                val clickedWaypointFeature = mapboxMap!!.queryRenderedFeatures(screenPoint, WAYPOINT_MARKER_LAYER)
                if(clickedWaypointFeature.isNotEmpty()){
                    val geoCacheName = clickedWaypointFeature[0].getStringProperty(PROPERTY_GEOCACHE_NAME)
                    val waypointName = clickedWaypointFeature[0].getStringProperty(PROPERTY_WAYPOINT_NAME)
                    val featureList = waypointFeatureCollection!!.features()
                    if (featureList != null) {
                        for (i in featureList.indices) {
                            if (featureList[i].getStringProperty(PROPERTY_GEOCACHE_NAME) == geoCacheName &&
                                    featureList[i].getStringProperty(PROPERTY_WAYPOINT_NAME) == waypointName) { // If the feature that was clicked on matches this one
                                if (featureSelectStatus(i, waypointFeatureCollection)) {
                                    setFeatureSelectState(featureList[i], false)
                                } else {
                                    setFeatureSelectState(featureList[i], true)
                                }
                                return true
                            }
                        }
                    }
                } else { // User clicked somewhere that's not on top of the symbol
                    val infoWindows = mapboxMap!!.queryRenderedFeatures(
                            screenPoint, CALLOUT_LAYER_ID, WAYPOINT_CALLOUT_LAYER_ID)
                    if (infoWindows.isNotEmpty()) {
                        val window = infoWindows[0]
                        val name = window.getStringProperty(PROPERTY_GEOCACHE_NAME)
                        val symbolScreenPoint = mapboxMap!!.projection.toScreenLocation(convertToLatLng(window))
                        val view = viewMap[name]!!
                        val button = view.findViewById<View>(R.id.go_to_button)
                        // create hitbox for button
                        val hitRectText = Rect()
                        button.getHitRect(hitRectText)
                        // move hitbox to location of symbol
                        hitRectText.offset(symbolScreenPoint.x.toInt(), symbolScreenPoint.y.toInt())
                        // offset to consider box's size
                        hitRectText.offset(-view.width / 2, -view.height - 50)
                        if (hitRectText.contains(screenPoint.x.toInt(), screenPoint.y.toInt())) {
                            // user clicked on marker
                            goToGeoCache(window.getStringProperty(PROPERTY_CODE))
                            return true
                        }
                    }
                }
            }
        return false
    }

    private fun featureSelectStatus(index: Int, selectedFeatureCollection: FeatureCollection?): Boolean {
        if (selectedFeatureCollection == null) {
            return false
        }
        if (BuildConfig.DEBUG && selectedFeatureCollection.features() == null) {
            error("Assertion failed")
        }
        return selectedFeatureCollection.features()!![index].getBooleanProperty(PROPERTY_SELECTED)
    }

    private fun setSelected(index: Int) {
        if (featureCollection!!.features() != null) {
            val feature = featureCollection!!.features()!![index]
            setFeatureSelectState(feature, true)
            refreshSource()
        }
    }

    private fun setFeatureSelectState(feature: Feature, selectedState: Boolean) {
        for(otherFeature: Feature in featureCollection?.features()!!){
            otherFeature.properties()!!.addProperty(PROPERTY_SELECTED, false)
        }

        for(otherFeature: Feature in waypointFeatureCollection?.features()!!){
            otherFeature.properties()!!.addProperty(PROPERTY_SELECTED, false)
        }

        if (feature.properties() != null) {
            feature.properties()!!.addProperty(PROPERTY_SELECTED, selectedState)
        }

        refreshSource()
    }

    private fun refreshSource() {
        if (geoCacheSource != null && featureCollection != null) {
            geoCacheSource!!.setGeoJson(featureCollection)
        }
        if(waypointSource != null && waypointFeatureCollection != null){
            waypointSource!!.setGeoJson(waypointFeatureCollection)
        }
    }// If we have a starting point add it in the beginning and the end of the route
    // If there is only one geocache in the list the LatLngBoundsBuilder will fail to build-
    // If that is the case just focus on the geocache

    // Get starting point if there is one



    private fun setTourDistanceCheckBox() {
        val checkBox = findViewById<TriStatesCheckBox>(R.id.distance_checkbox)
        checkBox.setOnClickListener {
            when (checkBox.getState()) {
                TriStatesCheckBox.INDETERMINATE -> showRemainingTourLineLayer()
                TriStatesCheckBox.CHECKED -> {
                    hideRemainingTourLineLayer()
                    showFullTourLineLayer()
                }
                TriStatesCheckBox.UNCHECKED -> hideFullTourLineLayer()
                else -> {
                    hideRemainingTourLineLayer()
                    hideFullTourLineLayer()
                }
            }
        }
    }

    private fun goToGeoCache(code: String) {
        val url = "https://coord.info/$code"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }


    private fun showFullTourLineLayer() {
        mapboxMap!!.getStyle { style: Style ->
            val layer = style.getLayer(FULL_TOUR_LINE_LAYER_ID)!!
            layer.setProperties(PropertyFactory.visibility(Property.VISIBLE))
        }
        showFullTourDistance()
    }

    private fun hideFullTourLineLayer() {
        mapboxMap!!.getStyle { style: Style ->
            val layer = style.getLayer(FULL_TOUR_LINE_LAYER_ID)!!
            layer.setProperties(PropertyFactory.visibility(Property.NONE))
        }
        findViewById<View>(R.id.line_distance).visibility = View.INVISIBLE
    }

    private fun showRemainingTourLineLayer() {
        mapboxMap!!.getStyle { style: Style ->
            val layer = style.getLayer(REMAINING_TOUR_LINE_LAYER_ID)!!
            layer.setProperties(PropertyFactory.visibility(Property.VISIBLE))
        }
        showRemainingTourDistance()
    }

    private fun hideRemainingTourLineLayer() {
        mapboxMap!!.getStyle { style: Style ->
            val layer = style.getLayer(REMAINING_TOUR_LINE_LAYER_ID)!!
            layer.setProperties(PropertyFactory.visibility(Property.NONE))
        }
        findViewById<View>(R.id.line_distance).visibility = View.INVISIBLE
    }

    private fun showFullTourDistance() {
        val lineLengthTextView = findViewById<TextView>(R.id.line_distance)
        lineLengthTextView.visibility = View.VISIBLE
        lineLengthTextView.text = String.format(getString(R.string.full_tour_distance),
                viewModel.computeTourFullDistance(), "kms")
    }

    private fun showRemainingTourDistance() {
        val lineLengthTextView = findViewById<TextView>(R.id.line_distance)
        lineLengthTextView.visibility = View.VISIBLE
        lineLengthTextView.text = String.format(getString(R.string.remaining_tour_distance),
                viewModel.computeTourRemainingDistance(), "kms")
    }


    fun setCurrentLocationAsStartPoint(view: View?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Set the current location as the start and finish point of the tour?")
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    // Set current point as starting point of the tour
                    val locationComponent = mapboxMap!!.locationComponent
                    try {
                        if (BuildConfig.DEBUG && locationComponent.lastKnownLocation == null) {
                            error("Assertion failed")
                        }
                        val currentLatitude = locationComponent.lastKnownLocation!!.latitude
                        val currentLongitude = locationComponent.lastKnownLocation!!.longitude
                        _tour!!.tour.setStartingPoint(currentLatitude, currentLongitude)
                        viewModel.updateTour(_tour!!)
                        finish()
                        startActivity(intent)
                    } catch (e: Exception) {
                        checkLocationPermissionIsGrantedAndGPSIsOn()
                        e.printStackTrace()
                    }
                }
                .setNegativeButton("No") { _: DialogInterface?, _: Int -> }
        dialogBuilder.create().show()
    }

    private fun checkLocationPermissionIsGrantedAndGPSIsOn() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val lm = this.getSystemService(LOCATION_SERVICE) as LocationManager
            val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!gpsEnabled) Toast.makeText(this, "Please turn on your GPS", Toast.LENGTH_SHORT).show() else enableLocationComponent(Objects.requireNonNull(mapboxMap!!.style)!!)
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this)
        }
    }

    private fun generateSymbol(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        val measuredWidth = view.measuredWidth
        val measuredHeight = view.measuredHeight
        view.layout(0, 0, measuredWidth, measuredHeight)
        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun generateViewIcon(){

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch{

            val imagesMap = HashMap<String?, Bitmap?>()
            val inflater = LayoutInflater.from(this@MapActivity)

            for (feature in featureCollection?.features()!!) {

                val bubbleLayout = inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null) as BubbleLayout
                val name = feature.getStringProperty(PROPERTY_GEOCACHE_NAME)
                val titleTextView = bubbleLayout.findViewById<TextView>(R.id.geo_cache_title)
                titleTextView.text = name
                val descriptionTextView = bubbleLayout.findViewById<TextView>(R.id.geo_cache_description)
                descriptionTextView.text = String.format(this@MapActivity.getString(R.string.geo_cache_description_box),
                        feature.getStringProperty(PROPERTY_DIFFICULTY),
                        feature.getStringProperty(PROPERTY_TERRAIN),
                        feature.getStringProperty(PROPERTY_FAVOURITES))

                val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                bubbleLayout.measure(measureSpec, measureSpec)
                val measuredWidth = bubbleLayout.measuredWidth.toFloat()
                bubbleLayout.arrowPosition = measuredWidth / 2 - 5
                val bitmap = generateSymbol(bubbleLayout)
                imagesMap[name] = bitmap
                viewMap[name] = bubbleLayout
            }

            val waypointsMap = HashMap<String?, Bitmap?>()
            for (feature in waypointFeatureCollection?.features()!!) {

                val bubbleLayout = inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null) as BubbleLayout
                val geoCacheName = feature.getStringProperty(PROPERTY_GEOCACHE_NAME)
                val titleTextView = bubbleLayout.findViewById<TextView>(R.id.geo_cache_title)
                titleTextView.text = geoCacheName
                val descriptionTextView = bubbleLayout.findViewById<TextView>(R.id.geo_cache_description)
                val waypointName = feature.getStringProperty(PROPERTY_WAYPOINT_NAME)
                descriptionTextView.text = waypointName

                val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                bubbleLayout.measure(measureSpec, measureSpec)
                val measuredWidth = bubbleLayout.measuredWidth.toFloat()
                bubbleLayout.arrowPosition = measuredWidth / 2 - 5
                val bitmap = generateSymbol(bubbleLayout)

                val name = geoCacheName + waypointName
                waypointsMap[name] = bitmap
                viewMap[name] = bubbleLayout
            }
            withContext(Dispatchers.Main){
                setImageGenResults(imagesMap)
                setImageGenResults(waypointsMap)
                refreshSource()
            }
        }
    }


    /**
     * Invoked when the bitmaps have been generated from a view.
     */
    private fun setImageGenResults(imageMap: HashMap<String?, Bitmap?>?) {
        if (mapboxMap != null) {
            mapboxMap!!.getStyle { style: Style ->
// calling addImages is faster as separate addImage calls for each bitmap.
                style.addImages(imageMap!!)
            }
        }
    }
}
package net.teamtruta.tiaires;

import net.teamtruta.tiaires.db.DbConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * GeocachingTour class, representing a tour to go out and find them gaches. Includes a list of Geocaches in tour, among others.
 */

public class GeocachingTour
{
    public long _id;
    private String _name;
    boolean _isCurrentTour;
    public List<GeoCacheInTour> _tourGeoCaches = new ArrayList<>();
    private Coordinate _startingPointLatitude;
    private Coordinate _startingPointLongitude;
    DbConnection _dbConnection;

    TourCreationActivity tourCreationActivityDelegate;
    TourActivity tourActivityDelegate;

    // Constructor for when you are creating a new tour
    public GeocachingTour(String name, boolean isCurrentTour, DbConnection dbConnection){
        if(name == null) {
            _name = new Date().toString();
        } else {
            _name = name;
        }
        this._isCurrentTour = isCurrentTour;
        this._dbConnection = dbConnection;
        this._id = _dbConnection.getTourTable().storeNewTour(_name, _isCurrentTour);
    }

    static GeocachingTour getGeocachingTourFromID(long id, DbConnection dbConnection){
        GeocachingTour tour = dbConnection.getTourTable().getTour(id, dbConnection);
        tour._dbConnection = dbConnection;
        tour._tourGeoCaches = dbConnection.getGeoCacheTable().getAllGeoCachesInTour(id, dbConnection);
        return tour;
    }

    public GeocachingTour(String name, long id, boolean isCurrentTour, DbConnection dbConnection){
        this._name = name;
        this._id = id;
        this._isCurrentTour = isCurrentTour;
        this._dbConnection = dbConnection;
    }

    public GeocachingTour(String name, long id, boolean isCurrentTour, double startingPointLatitude,
                          double startingPointLongitude , DbConnection dbConnection){
        this._name = name;
        this._id = id;
        this._isCurrentTour = isCurrentTour;
        this._startingPointLatitude = new Coordinate(startingPointLatitude);
        this._startingPointLongitude = new Coordinate(startingPointLongitude);
        this._dbConnection = dbConnection;
    }

    public String getName(){
       return _name;
        //return _dbConnection.getTourTable().getTourName(_id);
    }

    public long getSize(){
        return _dbConnection.getGeoCacheTable().getSizeOfTour(_id);
    }

    public long getNumFound(){
        return _dbConnection.getGeoCacheTable().getNumberFindInTour(_id);
    }

    public long getNumDNF(){
        return _dbConnection.getGeoCacheTable().getNumberDNFInTour(_id);
    }

    public List<String> getTourGeoCacheCodes() {

        return _tourGeoCaches.stream().map(GeoCacheInTour::getGeoCache)
                .map(GeoCache::getCode).collect(Collectors.toList());

    }

    static List<GeocachingTour> getAllTours(DbConnection dbConnection){
        return dbConnection.getTourTable().getAllTours(dbConnection);
    }

    void removeFromTour(String code)
    {
        final String upperCode = code.toUpperCase();
        _dbConnection.getGeoCacheTable().deleteCache(upperCode, _id);
    }

    public void addToTour(List<String> geoCachesToGet) { // TODO: trocar isto por uma classe básica de todas as collecções?

        // Remove any repeated caches
        geoCachesToGet = geoCachesToGet.stream().distinct().collect(Collectors.toList());

        // Process the deltas from the old list to the new list
        // 1. Remove from the original tour caches that are not in the new one
        List<String> geoCachesAlreadyInTour = getTourGeoCacheCodes();//new CacheDbTable(this).getTourCacheCodes(tourID);
        for (String currentTourCache : geoCachesAlreadyInTour) {
            if (!geoCachesToGet.contains(currentTourCache)) {
                removeFromTour(currentTourCache);
            }
        }

        // 2. Remove from the list of caches to fetch, those we already have loaded
        geoCachesToGet = geoCachesToGet.stream().filter(str -> !geoCachesAlreadyInTour.contains(str))
                .collect(Collectors.toList());

        // Get the IDs of the new caches we obtained and add them to the database
        GeoCache.Companion.getGeoCaches(geoCachesToGet, _dbConnection, this, false);

    }

    public void changeName(String newTourName) {
        this._name = newTourName;
        _dbConnection.getTourTable().changeName(_id, newTourName);
    }

    void onAllGeoCachesObtained(boolean reloading){
        if(reloading){
            tourActivityDelegate.onFinishedReloadingGeoCaches();
        } else {
            tourCreationActivityDelegate.onTourCreated();
        }

    }

    public void reloadTourCaches() {

        GeoCache.Companion.getGeoCaches(getTourGeoCacheCodes(), _dbConnection, this, true);

    }

    public void deleteTour() {

        // Delete all GeocacheInTour in this tour
        _dbConnection.getGeoCacheTable().deleteAllGeoCachesInTour(_id);

        // Delete Tour
        _dbConnection.getTourTable().deleteTour(_id);


    }

    public void updateTourCaches(){

        for(int i = 0; i < _tourGeoCaches.size(); i++){
            _tourGeoCaches.get(i).setOrderIdx(i);
            _dbConnection.getGeoCacheTable().updateEntry(_tourGeoCaches.get(i));
        }

    }

    public int getLastVisitedGeoCache(){

        GeoCacheInTour lastVisitedGeoCache = _tourGeoCaches.stream()
                .filter(x -> x.getCurrentVisitOutcome() == VisitOutcomeEnum.Found ||
                        x.getCurrentVisitOutcome() == VisitOutcomeEnum.DNF)
                .reduce((first, second) -> second)
                .orElse(null);

        if(lastVisitedGeoCache == null){
            return 0;
        }


        ListIterator<GeoCacheInTour> it = _tourGeoCaches.listIterator();
        while (it.hasNext()) {
            if(it.next() == lastVisitedGeoCache) return it.nextIndex();
        }

        return 0;
    }

    public void setStartingPoint(Coordinate latitude, Coordinate longitude){
        this._startingPointLatitude = latitude;
        this._startingPointLongitude = longitude;

        _dbConnection.getTourTable().updateStartingPointInTour(_id, _startingPointLatitude.getValue(),
                _startingPointLongitude.getValue());
    }

    public void setStartingPoint(double latitude, double longitude){
        this._startingPointLatitude = new Coordinate(latitude);
        this._startingPointLongitude = new Coordinate(longitude);

        _dbConnection.getTourTable().updateStartingPointInTour(_id, latitude,
                longitude);
    }


    public Double getStartingPointLatitude(){
        if (_startingPointLatitude == null) return null;
        return _startingPointLatitude.getValue();
    }

    public Double getStartingPointLongitude(){
        if (_startingPointLongitude == null) return null;
        return _startingPointLongitude.getValue();
    }

}

// TODO: I need some unit tests on this
// TODO: rever nomenclatura. Geocache ou GeoCache ou cache ou TourCache ?
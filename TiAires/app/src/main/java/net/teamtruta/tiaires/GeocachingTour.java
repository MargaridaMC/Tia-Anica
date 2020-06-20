package net.teamtruta.tiaires;

import net.teamtruta.tiaires.db.DbConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GeocachingTour class, representing a tour to go out and find them gaches. Includes a list of Geocaches in tour, among others.
 */

public class GeocachingTour
{
    public long _id;
    private String _name;
    boolean _isCurrentTour;
    public List<GeocacheInTour> _tourCaches = new ArrayList<>();
    DbConnection _dbConnection;

    TourCreationActivity tourCreationActivityDelegate;
    TourActivity tourActivityDelegate;
//38 55 23
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
        tour._tourCaches = dbConnection.getCacheTable().getAllCachesInTour(id, dbConnection);
        return tour;
    }

    public GeocachingTour(String name, long id, boolean isCurrentTour, DbConnection dbConnection){
        this._name = name;
        this._id = id;
        this._isCurrentTour = isCurrentTour;
        this._dbConnection = dbConnection;
    }

    public String getName(){
       return _name;
        //return _dbConnection.getTourTable().getTourName(_id);
    }

    public long getSize(){
        return _dbConnection.getCacheTable().getSizeOfTour(_id);
    }

    public long getNumFound(){
        return _dbConnection.getCacheTable().getNumberFindInTour(_id);
    }

    public long getNumDNF(){
        return _dbConnection.getCacheTable().getNumberDNFInTour(_id);
    }

    public List<String> getTourCacheCodes() {

        return _tourCaches.stream().map(GeocacheInTour::getGeocache)
                .map(Geocache::getCode).collect(Collectors.toList());

    }

    static List<GeocachingTour> getAllTours(DbConnection dbConnection){
        return dbConnection.getTourTable().getAllTours(dbConnection);
    }

    void removeFromTour(String code)
    {
        final String upperCode = code.toUpperCase();
        _dbConnection.getCacheTable().deleteCache(upperCode);
    }

    public void addToTour(List<String> cachesToGet) { // TODO: trocar isto por uma classe básica de todas as collecções?

        // Remove any repeated caches
        cachesToGet = cachesToGet.stream().distinct().collect(Collectors.toList());

        // Process the deltas from the old list to the new list
        // 1. Remove from the original tour caches that are not in the new one
        List<String> cachesAlreadyInTour = getTourCacheCodes();//new CacheDbTable(this).getTourCacheCodes(tourID);
        for (String currentTourCache : cachesAlreadyInTour) {
            if (!cachesToGet.contains(currentTourCache)) {
                removeFromTour(currentTourCache);
            }
        }

        // 2. Remove from the list of caches to fetch, those we already have loaded
        cachesToGet = cachesToGet.stream().filter(str -> !cachesAlreadyInTour.contains(str))
                .collect(Collectors.toList());

        // Get the IDs of the new caches we obtained and add them to the database
        Geocache.Companion.getGeocaches(cachesToGet, _dbConnection, this, false);

    }

    public void changeName(String newTourName) {
        this._name = newTourName;
        _dbConnection.getTourTable().changeName(_id, newTourName);
    }

    void onAllGeocachesObtained(boolean reloading){
        if(reloading){
            tourActivityDelegate.onFinishedReloadingCaches();
        } else {
            tourCreationActivityDelegate.onTourCreated();
        }

    }

    public void reloadTourCaches() {

        Geocache.Companion.getGeocaches(getTourCacheCodes(), _dbConnection, this, true);

    }

/*    @Override
    public void onGeocachingScrappingTaskResult(List<Geocache> newlyLoadedCaches, List<Long> cachesAlreadyInDb) {
        List<Long> newlyLoadedCachesIDs = _dbConnection.getCacheDetailTable().store(newlyLoadedCaches);
        cachesAlreadyInDb.addAll(newlyLoadedCachesIDs);
        _dbConnection.getCacheTable().addCachesToTour(_id, cachesAlreadyInDb);
        delegate.onTourCreated();
    }*/

   /* public void reloadTourCaches() {
        List<Long> tourCachesIDs = new ArrayList<>();
        for(GeocacheInTour gc : _tourCaches){
            tourCachesIDs.add(gc.getGeocache().get_id());
        }

        cachesAlreadyInDb = Geocache.Companion.getGeocaches(cachesToGet, _dbConnection, this);

        _dbConnection.getCacheDetailTable().reloadCaches(tourCachesIDs);
    }*/
}

// TODO: I need some unit tests on this
// TODO: rever nomenclatura. Geocache ou GeoCache ou cache ou TourCache ?
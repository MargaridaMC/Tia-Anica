package net.teamtruta.tiaires;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * GeocachingTour class, representing a tour to go out and find them gaches. Includes a list of Geocaches in tour, among others.
 */

public class GeocachingTour implements PostGeocachingScrapping
{
    private String _name;
    List<GeocacheInTour> _tourCaches = new ArrayList<>();
    boolean _isCurrentTour = false;
    long _id = -1L;
    DbConnection _dbConnection;

    List<Long> cachesAlreadyInDb = new ArrayList<>();

    // Constructor for when you are creating a new tour
    GeocachingTour(String name, boolean isCurrentTour, DbConnection dbConnection){
        if(name == null) {
            _name = new Date().toString();
        } else {
            _name = name;
        }
        this._isCurrentTour = isCurrentTour;
        this._dbConnection = dbConnection;
        this._id = _dbConnection.getTourTable().storeNewTour(_name, _isCurrentTour);
    }

    public GeocachingTour(DbConnection dbConnection) {
        this._dbConnection = dbConnection;
    }

    static GeocachingTour getGeocachingTourFromID(long id, DbConnection dbConnection){
        GeocachingTour tour = dbConnection.getTourTable().getTour(id, dbConnection);
        tour._dbConnection = dbConnection;
        tour._tourCaches = dbConnection.getCacheTable().getAllCachesInTour(id, dbConnection);
        return tour;
    }

    GeocachingTour(String name, long id, boolean isCurrentTour, DbConnection dbConnection){
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

        List<String> codes = new ArrayList<>();

        for(GeocacheInTour geocache:_tourCaches){
            codes.add(geocache.getGeocache().getCode());
        }

        return codes;

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

        // Process the deltas from the old list to the new list
        // 1. Remove from the original tour caches that are not in the new one
        List<String> cachesAlreadyInTour = getTourCacheCodes();//new CacheDbTable(this).getTourCacheCodes(tourID);
        for (String currentTourCache : cachesAlreadyInTour) {
            if (!cachesToGet.contains(currentTourCache)) {
                removeFromTour(currentTourCache);
            }
        }

        // 2. Remove from the list of caches to fetch, those we already have loaded
        for (String loadedCache : cachesAlreadyInTour) {
            cachesToGet.remove(loadedCache); // don't get the information again. If the list doesn't containt the cache nothing will happen
        }

        // Get the IDs of the new caches we obtained and add them to the database
        cachesAlreadyInDb = Geocache.getGeocaches(cachesToGet, _dbConnection, this);

    }

    public void changeName(String newTourName) {
        this._name = newTourName;
        boolean success =_dbConnection.getTourTable().changeName(_id, newTourName);
    }

    @Override
    public void onGeocachingScrappingTaskResult(List<Geocache> newlyLoadedCaches) {
        List<Long> newlyLoadedCachesIDs = _dbConnection.getCacheDetailTable().store(newlyLoadedCaches);
        cachesAlreadyInDb.addAll(newlyLoadedCachesIDs);
        _dbConnection.getCacheTable().addCachesToTour(_id, cachesAlreadyInDb);
    }

}

// TODO: I need some unit tests on this
// TODO: add support to say a given trackable was found or left
// TODO: rever nomenclatura. Geocache ou GeoCache ou cache ou TourCache ?
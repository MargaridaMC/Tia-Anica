package net.teamtruta.tiaires;

import com.google.android.gms.maps.model.LatLng;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Geocache {
    private String _code;
    private String _name;
    private Coordinate _latitude;
    private Coordinate _longitude;
    private String _size;
    private String _difficulty;
    private String _terrain;
    //String type; // Normal, etc.
    private CacheTypeEnum _type = CacheTypeEnum.Other;
    private FoundEnumType _foundIt; // 0 - no, 1 - DNF, 2 - yes
    private String _hint;
    private int _favourites;
    private List<GeocacheLog> _recentLogs = new ArrayList<>();
    private String _DNFRisk = "";

    static DbConnection _dbConnection;
    long _id;

    public Geocache(String _code, String _name, Coordinate _latitude, Coordinate _longitude, String _size, String _difficulty, String _terrain, CacheTypeEnum _type, FoundEnumType _foundIt, String _hint, int _favourites) {
        this._code = _code;
        this._name = _name;
        this._latitude = _latitude;
        this._longitude = _longitude;
        this._size = _size;
        this._difficulty = _difficulty;
        this._terrain = _terrain;
        this._type = _type;
        this._foundIt = _foundIt;
        this._hint = _hint;
        this._favourites = _favourites;
    }

    public Geocache(String _code, String _name, Coordinate _latitude, Coordinate _longitude, String _size, String _difficulty, String _terrain, CacheTypeEnum _type, FoundEnumType _foundIt, String _hint, int _favourites, List<GeocacheLog> recentLogs, long id) {
        this._code = _code;
        this._name = _name;
        this._latitude = _latitude;
        this._longitude = _longitude;
        this._size = _size;
        this._difficulty = _difficulty;
        this._terrain = _terrain;
        this._type = _type;
        this._foundIt = _foundIt;
        this._hint = _hint;
        this._favourites = _favourites;
        this._recentLogs = recentLogs;
        this._id = id;
    }

    public Geocache() {

    }

    public Geocache(DbConnection dbConnection) {
        _dbConnection = dbConnection;
    }

    // These set and get methods needs to be public otherwise the serialization will not work.
    public String getCode(){ return _code; }
    public void setCode(String code){ this._code = code;}

    public String getName(){ return _name; }
    public void setName(String name){ this._name = name;}

    public Coordinate getLatitude(){ return _latitude; }
    public void setLatitude(Coordinate latitude){ this._latitude = latitude;}

    public Coordinate getLongitude(){ return _longitude; }
    public void setLongitude(Coordinate longitude){ this._longitude = longitude;}

    public String getSize(){ return _size; }
    public void setSize(String size){ this._size = size;}

    public String getDifficulty(){ return _difficulty; }
    public void setDifficulty(String difficulty){ this._difficulty = difficulty;}

    public String getTerrain(){ return _terrain; }
    public void setTerrain(String terrain){ this._terrain = terrain;}

    public CacheTypeEnum getType(){ return _type;}
    public void setType(CacheTypeEnum type){ this._type = type; }

    public FoundEnumType getFoundIt(){ return _foundIt; }
    public void setFoundIt(FoundEnumType foundIt){ this._foundIt = foundIt;}

    public String getHint(){ return _hint; }
    public void setHint(String hint){ this._hint = hint;}

    public int getFavourites(){ return _favourites; }
    public void setFavourites(int favourites){ this._favourites = favourites;}

    public List<GeocacheLog> getRecentLogs(){ return _recentLogs;}
    public void setRecentLogs(ArrayList<GeocacheLog> recentLogs){this._recentLogs = recentLogs;}

    public String getDNFRisk(){return _DNFRisk; }

    LatLng getLatLng(){

        return new LatLng(_latitude.getValue(), _longitude.getValue());
    }

    public long CountDaysSinceLastFind() {
        if (_recentLogs == null || _recentLogs.size() == 0)
            return 0;

        // I'm not going to comment what I think about this line of _code, esp if compared with the C# version.
        // #language-of-the-flintstones

        GeocacheLog lastLog = _recentLogs.stream().filter(log -> log.getLogType() == FoundEnumType.Found).findFirst().get();

        return ChronoUnit.DAYS.between(lastLog.getLogDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public double AverageDaysBetweenFinds()
    {
        if (_recentLogs == null || _recentLogs.size() == 0)
            return 0;

        long daysDifference = ChronoUnit.DAYS.between(_recentLogs.get(_recentLogs.size()-1).getLogDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        return daysDifference / (double) _recentLogs.size();
    }

    int countDNFsInLastLogs(int numberOfLogsToCheck){

        List<GeocacheLog> logsToCheck = _recentLogs.subList(0, numberOfLogsToCheck);
        return (int) logsToCheck.stream().filter(log -> log.getLogType() == FoundEnumType.DNF).count();

    }

    boolean isDNFRisk(){
        return !_DNFRisk.equals("");
    }

    public String setDNFRisk(){

        GeocacheLog lastLog = _recentLogs.get(0);
        if(lastLog.getLogType() == FoundEnumType.Disabled) {
            _DNFRisk = "Disabled";
            return _DNFRisk;
        }

        if(lastLog.getLogType() == FoundEnumType.NeedsMaintenance) {
            _DNFRisk = "Needs Maintenance";
            return _DNFRisk;
        }

        int maxLogs = 10;
        int nDNFs = countDNFsInLastLogs(maxLogs);
        if(nDNFs >= 2){

            _DNFRisk = "DNFs in last " + maxLogs + ": " + nDNFs;

            // Count DNFs since last log
            if(_recentLogs.get(0).getLogType() == FoundEnumType.DNF){
                int i = 0;
                while (_recentLogs.get(i).getLogType() == FoundEnumType.DNF) i++;
                _DNFRisk += " including last " + i;
            }

        }

        return  _DNFRisk;

    }

    boolean hasHint(){

        return !_hint.equals("NO MATCH");

    }

    Date getLastFindDate(){

        Object[] allFinds = _recentLogs.stream().filter(log -> log.getLogType().equals(FoundEnumType.Found)).toArray();
        GeocacheLog lastFind = (GeocacheLog) allFinds[allFinds.length - 1];
        return lastFind.getLogDate();

    }

    Date getLastLogDate(){

        return _recentLogs.get(0).getLogDate();

    }

    List<GeocacheLog> getLastNLogs(int n){
        return _recentLogs.subList(0, n + 1);
    }

    public String getDNFRiskShort() {

        GeocacheLog lastLog = _recentLogs.get(0);
        if(lastLog.getLogType() == FoundEnumType.Disabled)
            return "Disabled";
        if(lastLog.getLogType() == FoundEnumType.NeedsMaintenance)
            return  "Needs Maintenance";
        else return "DNF Risk";

    }

    static long existsInDb(String code, DbConnection dbConnection){
        return dbConnection.getCacheDetailTable().contains(code);
    }

    static List<Long> getGeocaches(List<String> requestedCaches, DbConnection dbConnection, GeocachingTour geocachingTour){

        List<Long> cacheListIds = new ArrayList<>();
        List<String> cachesToGet = new ArrayList<>();

        // First check if any of the caches already exist in the database
        for(String c : requestedCaches){
            long cacheID = existsInDb(c, dbConnection);
            if(cacheID != -1L){
                cacheListIds.add(cacheID);
            } else {
                cachesToGet.add(c);
            }
        }

        // Scrape the remaining caches
        String authCookie = App.getAuthenticationCookie();
        GeocachingScrapper scrapper = new GeocachingScrapper(authCookie);
        GeocachingScrappingTask geocachingScrappingTask = new GeocachingScrappingTask(scrapper, cachesToGet);
        geocachingScrappingTask.delegate = geocachingTour;
        geocachingScrappingTask.execute();

        return cacheListIds;
    }


}
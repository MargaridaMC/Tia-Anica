package net.teamtruta.tiaires;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * GeocachingTour
 */
public class GeocachingTour {

    private String _name;
    private ArrayList<GeocacheInTour> _tourCaches = new ArrayList<>();
    boolean isCurrentTour = false;
    int _numFound = 0;
    int _numDNF = 0;
    int _size = 0;

    GeocachingTour(String name) {
        if(name == null)
        {
            _name = new Date().toString();
        }
        else
        {
            _name = name;
        }
    }

    int size()
    {
        return _tourCaches.size();
    }

    int addToTour(Geocache gc)
    {
        // TODO: se gc = null ou gc.code == null, estoirar

        // only add if it's not there yet
        if(getCacheInTour(gc.code) == null)
        {
            System.out.println(gc.code + " não está na lista, adding");
            _tourCaches.add(new GeocacheInTour(gc));
        }

        _size = _tourCaches.size();
        return _tourCaches.size();
    }

    public int addToTour(Geocache[] gc) // TODO: trocar isto por uma classe básica de todas as collecções?
    {
        for (Geocache geocache : gc) {
            addToTour(geocache);
        }

        _size = _tourCaches.size();
        return _tourCaches.size();
    }

    public int removeFromTour(String code)
    {
        code = code.toUpperCase();

        // TODO: trocar isto por um predicado chamado no remove
        for(int j=0; j<_tourCaches.size(); j++)
        {
            if(_tourCaches.get(j).geocache.code.compareTo(code) == 0)
                _tourCaches.remove(j);
                break;
        }

        _size = _tourCaches.size();
        return _tourCaches.size();
    }
    
    private GeocacheInTour getCacheInTour(String code)
    {
        code = code.toUpperCase();

        for (GeocacheInTour geocacheInTour : _tourCaches) {
                if(geocacheInTour.geocache.code.compareTo(code) == 0)
                    return geocacheInTour;
        }

        return null;
    }

    GeocacheInTour getCacheInTour(int position){
        return _tourCaches.get(position);
    }

    public void setCacheInTour(int position, GeocacheInTour cache){
        _tourCaches.set(position, cache);
    }

    public void makeCurrentTour(){
        isCurrentTour = true;
    }

    public String getName(){
        return _name;
    }

    int getNumFound(){
        return _numFound;
    }

    int getNumDNF(){
        return _numDNF;
    }

    List<String> getTourCacheCodes(){

        List<String> codes = new ArrayList<>();
        for(GeocacheInTour geocache:_tourCaches){
            codes.add(geocache.geocache.code);
        }

        return codes;
    }

    public JSONObject toJSON(){

        JSONObject tourCacheJSON = new JSONObject();
        try {
            tourCacheJSON.put("tourName", this._name);
            tourCacheJSON.put("numDNF", this._numDNF);
            tourCacheJSON.put("numFound",this._numFound);
            tourCacheJSON.put("size", size());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        int i = 0;

        for(GeocacheInTour gc : _tourCaches){
            JSONObject cacheJSON = gc.toJSON();
            try {
                tourCacheJSON.put(Integer.toString(i), cacheJSON); //gc.geocache.name
            } catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }

        return tourCacheJSON;

    }

    static GeocachingTour fromJSON(JSONObject tourCacheJSON){

        GeocachingTour tour = new GeocachingTour("");

        try {
            tour._name = tourCacheJSON.getString("tourName");
            tour._numDNF = tourCacheJSON.getInt("numDNF");
            tour._numFound = tourCacheJSON.getInt("numFound");
            tour._size = tourCacheJSON.getInt("size");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        for(int i = 0; i < tour._size; i++){

            JSONObject cacheJSON = null;
            try {
                cacheJSON = tourCacheJSON.getJSONObject(Integer.toString(i));
                //System.out.println(i);
                //System.out.println(cacheJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            GeocacheInTour gc = GeocacheInTour.fromJSON(cacheJSON);
            tour._tourCaches.add(gc);

        }

        return tour;

    }

    void toFile(File rootPath){
        // write tour to file, and overwrites the file if one exists
        JSONObject tourJSON = this.toJSON();

        // Save tour to file
        String filename = _name + ".json";
        File file = new File(rootPath, filename);

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file, false);
            stream.write(tourJSON.toString().getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static GeocachingTour fromFile(File rootPath, String tourName){

        GeocachingTour newTour = new GeocachingTour(tourName);

        String filename = tourName + ".json";
        File file = new File(rootPath, filename);

        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String contents = new String(bytes);

        try {
            JSONObject newCacheTour = new JSONObject(contents);
            newTour = GeocachingTour.fromJSON(newCacheTour);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newTour;

    }

    public static boolean deleteTourFile(File rootPath, String tourName){

        String filename = tourName + ".json";
        File file = new File(rootPath, filename);

        return file.delete();
    }

    JSONObject getMetaDataJSON(){

        JSONObject metaData = new JSONObject();

        try {
            metaData.put("tourName", this._name);
            metaData.put("numDNF", this._numDNF);
            metaData.put("numFound", this._numFound);
            metaData.put("size", this._size);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return metaData;
    }

    void fromMetaDataJSON(JSONObject metaData){

        try {
            this._name = metaData.getString("tourName");
            this._numDNF = metaData.getInt("numDNF");
            this._numFound = metaData.getInt("numFound");
            this._size = metaData.getInt("size");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}

// TODO: I need some unit tests on this
// TODO: add support to say a given trackable was found or left
// TODO: rever nomenclatura. Geocache ou GeoCache ou cache ou TourCache ?
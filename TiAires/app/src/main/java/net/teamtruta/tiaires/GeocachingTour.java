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
 * GeocachingTour class, representing a tour to go out and find them gaches. Includes a list of Geocaches in tour, among others.
 */
public class GeocachingTour extends GeocachingTourSummary
{
    ArrayList<GeocacheInTour> _tourCaches = new ArrayList<>();

    GeocachingTour(String name)
    {
        super(name);
    }

    int getSize()
    {
        return _tourCaches.size();
    }

    /**
     * Rename a tour
     * @param newName New name of tour
     * @return Old name of tour
     */
    String setName(String newName)
    {
        String oldName = _name;
        _name = newName;
        return oldName;
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
        return _size;
    }

    public int addToTour(Geocache[] gc) // TODO: trocar isto por uma classe básica de todas as collecções?
    {
        for (Geocache geocache : gc) {
            addToTour(geocache);
        }

        _size = _tourCaches.size();
        return _size;
    }

    public int removeFromTour(String code)
    {
        code = code.toUpperCase();

        // TODO: trocar isto por um predicado chamado no remove
        for(int j=0; j<_tourCaches.size(); j++)
        {
            if(_tourCaches.get(j).geocache.code.equals(code)) {
                _tourCaches.remove(j);
                break;
            }
        }

        _size = _tourCaches.size();
        return _size;
    }

    /**
     * Check if a given cache is in the tour, returning its detailed information, or null if not exists
     * @param code of geocache, eg. GCxxxx
     * @return the information about the cache in tour, or null if not exists
     */
    GeocacheInTour getCacheInTour(String code)
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

    /**
     * Return a list of geocache codes.
     * @return  List of geocache codes. If there are no codes, it returns a list with getSize = 0. Doesn't return null.
     */
    List<String> getTourCacheCodes(){

        List<String> codes = new ArrayList<String>();
        for(GeocacheInTour geocache:_tourCaches){
            codes.add(geocache.geocache.code);
        }

        return codes;
    }

    /**
     * Serialize the content of the tour into a JSON object.
     * TODO: this method and the next should be in a separate class, e.g., GeocachingTourSerializer or something.
     * @return JSON object with the contents of the Tour, including the cache information
     */
    public JSONObject toJSON(){

        JSONObject tourCacheJSON = new JSONObject();
        try {
            tourCacheJSON.put("_tourName", this._name);
            tourCacheJSON.put("numDNF", this._numDNF);
            tourCacheJSON.put("numFound",this._numFound);
            tourCacheJSON.put("getSize", getSize());
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

    /**
     * Deserialize a JSON tour into a GeocachingTour instance.
     * TODO: this method and the previous should be in a separate class, e.g., GeocachingTourSerializer or something.
     * @param tourCacheJSON Tour to de-serialize
     * @return De-serialized GeocachingTour
     */
    static GeocachingTour fromJSON(JSONObject tourCacheJSON){

        GeocachingTour tour = new GeocachingTour("");

        try {
            tour._name = tourCacheJSON.getString("_tourName");
            tour._numDNF = tourCacheJSON.getInt("numDNF");
            tour._numFound = tourCacheJSON.getInt("numFound");
            tour._size = tourCacheJSON.getInt("getSize");
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

    /**
     * Serialize this tour and write it to a file
     * TODO: put in a separate class, eg GeocachingTourStorage
     * @param rootPath Path to the tour, which will be saved with the name of the tour and .json extention
     */
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

    /**
     * Read a tour from file and deserialize it
     * TODO: put in a separate class, eg GeocachingTourStorage
     * @param rootPath Path to the tour and it's name
     */
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



}

// TODO: I need some unit tests on this
// TODO: add support to say a given trackable was found or left
// TODO: rever nomenclatura. Geocache ou GeoCache ou cache ou TourCache ?
package net.teamtruta.tiaires;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

    private JSONObject toJSON(){

        JSONObject tourCacheJSON = new JSONObject();
        try {
            tourCacheJSON.put("tourName", _name);
            tourCacheJSON.put("numDNF", _numDNF);
            tourCacheJSON.put("numFound", _numFound);
            tourCacheJSON.put("size", size());

            int i = 0;

            for(GeocacheInTour gc : _tourCaches){
                JSONObject cacheJSON = gc.geocache.toJSON();
                tourCacheJSON.put(Integer.toString(i), cacheJSON); //gc.geocache.name
                i++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



        return tourCacheJSON;

    }

    private void fromJSON(JSONObject tourCacheJSON){

        int size = tourCacheJSON.length() - 4;

        try {
            _name = tourCacheJSON.getString("tourName");
            _numDNF = tourCacheJSON.getInt("numDNF");
            _numFound = tourCacheJSON.getInt("numFound");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        for(int i = 0; i < size; i++){

            JSONObject cacheJSON = null;
            try {
                cacheJSON = tourCacheJSON.getJSONObject(Integer.toString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Geocache gc = new Geocache();
            gc.fromJSON(cacheJSON);

            _tourCaches.add(new GeocacheInTour(gc));

        }
        _size = _tourCaches.size();
    }

    void toFile(File rootPath){

        JSONObject tourJSON = this.toJSON();

        // Save tour to file
        String filename = _name + ".json";
        File file = new File(rootPath, filename);

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
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
            newTour.fromJSON(newCacheTour);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newTour;

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
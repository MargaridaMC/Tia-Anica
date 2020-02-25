package net.teamtruta.tiaires;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * GeocachingTour
 */
public class GeocachingTour {

    private String _name;
    private ArrayList<GeocacheInTour> _tourCaches = new ArrayList<>();
    boolean isCurrentTour = false;
    int _numFound = 0;
    int _numDNF = 0;

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

        return _tourCaches.size();
    }

    public int addToTour(Geocache[] gc) // TODO: trocar isto por uma classe básica de todas as collecções?
    {
        for (Geocache geocache : gc) {
            addToTour(geocache);
        }

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

    private JSONArray toJSON(){

        JSONArray tourCacheJSON = new JSONArray();

        for(GeocacheInTour gc : _tourCaches){

            JSONObject cacheJSON = gc.geocache.toJSON();
            tourCacheJSON.put(cacheJSON);

        }

        return tourCacheJSON;

    }

    void fromJSON(JSONArray tourCacheJSON){

        int size = tourCacheJSON.length();
        for(int i = 0; i < size; i++){

            JSONObject cacheJSON = null;
            try {
                cacheJSON =  tourCacheJSON.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Geocache gc = new Geocache();
            gc.fromJSON(cacheJSON);

            _tourCaches.add(new GeocacheInTour(gc));

        }

    }

    void toFile(File rootPath){

        JSONArray tourJSON = this.toJSON();

        // Save tour to file
        String filename = _name + "_tour.json";
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

        JSONParser jsonParser = new JSONParser();
        GeocachingTour newTour = new GeocachingTour(tourName);

        String filename = tourName + "_tour.json";
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
            //Read JSON file
            Object obj = jsonParser.parse(contents);

            JSONArray newCacheArray = (JSONArray) obj;
            newTour.fromJSON(newCacheArray);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newTour;

    }
}

// TODO: I need some unit tests on this
// TODO: add support to say a given trackable was found or left
// TODO: rever nomenclatura. Geocache ou GeoCache ou cache ou TourCache ?
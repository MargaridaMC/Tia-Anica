package net.teamtruta.tiaires;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * GeocachingTour class, representing a tour to go out and find them gaches. Includes a list of Geocaches in tour, among others.
 */
@JsonIgnoreProperties({"numDNF", "numFound", "summary"})
public class GeocachingTour
{
    private String _name;
    private int _size;
    private ArrayList<GeocacheInTour> _tourCaches = new ArrayList<>();

    @JsonCreator
    GeocachingTour(@JsonProperty("name") String name)
    {
        if(name == null)
        {
            _name = new Date().toString();
        }
        else
        {
            _name = name;
        }
    }

    public String getName(){ return _name; }
    public void setName(String name) { this._name = name; }

    public int getSize(){ return _tourCaches.size(); }
    public void setSize(int size){ this._size = size; }

    public ArrayList<GeocacheInTour> getTourGeocaches(){ return _tourCaches; }
    public void setTourGeocaches(ArrayList<GeocacheInTour> tourCaches){ this._tourCaches = tourCaches; }

    static boolean write(String folder, GeocachingTour tour)
    {
        try
        {
            new ObjectMapper().writeValue(new File(folder, tour.getName()), tour);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    static GeocachingTour read(String folder, String tourName)
    {

        // Read the full content of the file
        try {
            // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return new ObjectMapper().readValue(new File(folder, tourName), new TypeReference<GeocachingTour>(){});
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return new GeocachingTour(null); // return an empty list
        }
    }

    public String toString(){

        try
        {
            return new ObjectMapper().writeValueAsString(this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }

    }

    public static GeocachingTour fromString(String tourString){

        try {
            // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return new ObjectMapper().readValue(tourString, new TypeReference<GeocachingTour>(){});
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return new GeocachingTour(null); // return an empty list
        }
    }

    /**
     * Get number of found caches in tour
     * @return number of finds in tour
     */
    public int getNumFound(){

        return (int) _tourCaches.stream().filter(gc -> gc.getVisit() == FoundEnumType.Found).count();

    }

    /**
     * Get number of DNFs in tour
     * @return number of DNFs in tour
     */
    public int getNumDNF(){

        return (int) _tourCaches.stream().filter(gc -> gc.getVisit() == FoundEnumType.DNF).count();

    }


    int addToTour(Geocache gc)
    {
        // TODO: se gc = null ou gc.code == null, estoirar

        // only add if it's not there yet
        if(getCacheInTour(gc.getCode()) == null)
        {
            System.out.println(gc.getCode() + " não está na lista, adding");
            _tourCaches.add(new GeocacheInTour(gc));
        }

        _size = _tourCaches.size();
        return _size;
    }

    public int addToTour(List<Geocache> gc) // TODO: trocar isto por uma classe básica de todas as collecções?
    {
        for (Geocache geocache : gc) {
            addToTour(geocache);
        }

        _size = _tourCaches.size();
        return _size;
    }

    int addToTour(Geocache gc, int position)
    {

        if(getCacheInTour(gc.getCode()) == null)
        {
            _tourCaches.add(position, new GeocacheInTour(gc));
        }

        _size = _tourCaches.size();
        return _size;
    }

    int removeFromTour(String code)
    {
        final String upperCode = code.toUpperCase();

        _tourCaches.removeIf(cit -> cit.getGeocache().getCode().equals(upperCode));

        _size = _tourCaches.size();
        return _size;
    }

    /**
     * Check if a given cache is in the tour, returning its detailed information, or null if not exists
     * @param code of geocache, eg. GCxxxx
     * @return the information about the cache in tour, or null if not exists
     */
    public GeocacheInTour getCacheInTour(String code)
    {
        code = code.toUpperCase();

        for (GeocacheInTour geocacheInTour : _tourCaches) {
            if(geocacheInTour.getGeocache().getCode().compareTo(code) == 0)
                return geocacheInTour;
        }

        return null;
    }

    GeocacheInTour getCacheInTour(int position){
        return _tourCaches.get(position);
    }

    void setCacheInTour(int position, GeocacheInTour cache){
        _tourCaches.set(position, cache);
    }

    /**
     * Return a list of geocache codes.
     * @return  List of geocache codes. If there are no codes, it returns a list with getSize = 0. Doesn't return null.
     */
    List<String> getTourCacheCodes(){

        List<String> codes = new ArrayList<>();

        for(GeocacheInTour geocache:_tourCaches){
            codes.add(geocache.getGeocache().getCode());
        }

        return codes;
    }

    /**
     * Delete the file with the information about the tour
     * @param folder Folder where the tours are stored
     * @param tourName Name of the tour to delete
     * @return nuthin'
     */
    static boolean deleteTourFile(String folder, String tourName)
    {
        return new File(folder, tourName).delete();
    }

    public GeocachingTourSummary getSummary(){

        GeocachingTourSummary summary = new GeocachingTourSummary(_name);
        summary.setSize(getSize());
        summary.setNumDNF(getNumDNF());
        summary.setNumFound(getNumFound());

        return summary;
    }

    void swapCachePositions(int fromPosition, int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(_tourCaches, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(_tourCaches, i, i - 1);
            }
        }
    }
}

// TODO: I need some unit tests on this
// TODO: add support to say a given trackable was found or left
// TODO: rever nomenclatura. Geocache ou GeoCache ou cache ou TourCache ?
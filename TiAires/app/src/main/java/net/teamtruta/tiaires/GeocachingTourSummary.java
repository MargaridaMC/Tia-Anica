package net.teamtruta.tiaires;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

/**
 * GeocachingTourSummary class represents the base information of a GeocachingTour, with the minimum metadata information
 */
public class GeocachingTourSummary
{
    // TODO: move ths to Private and fix where it's used from the activity!
    String _name;
    int _numFound = 0;
    int _numDNF = 0;
    int _size = 0;
    private boolean _isCurrentTour = false;


    /**
     * Class constructor
     * @param name Name of Tour. If null, it'll be named with the current date
     */
    GeocachingTourSummary(String name)
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


    public String getName(){
        return _name;
    }

    public int getNumFound(){
        return _numFound;
    }

    public int getNumDNF() { return _numDNF; }

    public boolean getIsCurrentTour() { return _isCurrentTour; }

    public void setIsCurrentTourOn() { _isCurrentTour = true; }

    public void setIsCurrentTourOff() { _isCurrentTour = false; }

    /**
     * Get the metadata about the tour -- (tour name, number of DNFs, Founds, and # of caches)
     * @return JSON object with the metadata (tour name, number of DNFs, Founds, and # of caches)
     */
    String serialize()
    {
        JSONObject summaryJsonObject = new JSONObject();

        try {
            summaryJsonObject.put("tourName", _name); // TODO: refactor to remove the underscore
            summaryJsonObject.put("numDNF", _numDNF);
            summaryJsonObject.put("numFound", _numFound);
            summaryJsonObject.put("size", _size);
            summaryJsonObject.put("isCurrent", _isCurrentTour);

            return summaryJsonObject.toString();
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null; // TODO - is this the best thing to return? how to best deal with breaking changes?
    }

    /**
     * De-serialize metadata from Json
     * @param summaryJsonString Summary information stored as a string
     */
    static GeocachingTourSummary deserialize(String summaryJsonString)
    {
        JSONObject summaryJsonObject = null;

        try {
            summaryJsonObject = new JSONObject(summaryJsonString);

            GeocachingTourSummary gts = new GeocachingTourSummary(summaryJsonObject.getString("tourName"));
            gts._numFound = summaryJsonObject.getInt("numFound");
            gts._numDNF = summaryJsonObject.getInt("numDNF");
            gts._size = summaryJsonObject.getInt("size");
            gts._isCurrentTour = summaryJsonObject.getBoolean("isCurrent");

            return gts;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
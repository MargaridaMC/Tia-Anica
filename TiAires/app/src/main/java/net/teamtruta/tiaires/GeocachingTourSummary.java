package net.teamtruta.tiaires;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GeocachingTourSummary class represents the base information of a GeocachingTour, with the minimum metadata information
 */
public class GeocachingTourSummary
{
    protected String _name;
    protected int _numFound = 0;
    protected int _numDNF = 0;
    protected int _size = 0;
    protected boolean _isCurrentTour = false;

    /**
     * Class constructor.
     * Attributes: https://stackoverflow.com/questions/55529028/jackson-deserialization-error-mismatchedinputexception
     * @param name Name of Tour. If null, it'll be named with the current date
     */
    @JsonCreator
    GeocachingTourSummary(@JsonProperty("name") String name)
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
    public void setName(String name) { _name = name; }

    public boolean getIsCurrentTour() { return _isCurrentTour; }
    public void setIsCurrentTour(boolean isCurrent) { _isCurrentTour = isCurrent; }

    // These properties are odd. They are here because they are needed in the listing, but in reality they should be calculated properties based
    // on the collection stored in a subclass. An approach to this is to override in the subclass and reimplement.

    public int getNumFound(){
        return _numFound;
    }
    public void setNumFound(int found) { _numFound = found; }

    public int getNumDNF() { return _numDNF; }
    public void setNumDNF(int dnf) { _numDNF = dnf; }

    public int getSize() { return _size; }
    public void setSize(int size) { _size = size; }


    public String serializeYourself() throws JsonProcessingException
    {
        String result = new ObjectMapper().writeValueAsString(this);
        return result;
    }

    /**
     * Get the metadata about the tour -- (tour name, number of DNFs, Founds, and # of caches)
     * @return JSON object with the metadata (tour name, number of DNFs, Founds, and # of caches)
     */
    String serialize()
    {
        JSONObject summaryJsonObject = new JSONObject();

        try {
            summaryJsonObject.put("tourName", _name);
            summaryJsonObject.put("numDNF", _numDNF);
            summaryJsonObject.put("numFound", _numFound);
            summaryJsonObject.put("size", _size);
            summaryJsonObject.put("isCurrent", _isCurrentTour);

            return summaryJsonObject.toString();
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
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
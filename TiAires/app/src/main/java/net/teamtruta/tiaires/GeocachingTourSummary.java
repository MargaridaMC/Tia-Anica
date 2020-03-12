package net.teamtruta.tiaires;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeocachingTourSummary class represents the base information of a GeocachingTour, with the minimum metadata information
 */
public class GeocachingTourSummary
{
    private String _name;
    private int _numFound = 0;
    private int _numDNF = 0;
    private int _size = 0;
    private boolean _isCurrentTour = false;

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

    // These set and get methods needs to be public otherwise the serialization will not work.
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


}
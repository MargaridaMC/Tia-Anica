package net.teamtruta.tiaires;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeocacheInTour
 */
public class GeocacheInTour
{
    private Geocache _geocache;

    // the following fields have the "In Tour" semantics. So if a cache was found but not in the tour, _found will be = NotAttempted; same logic applies to other fields.
    private String _userNotes = "";
    private FoundEnumType _found = FoundEnumType.NotAttempted;
    private Boolean _needsMaintenance = false;
    private Date _foundDate;

    @JsonCreator
    public GeocacheInTour(@JsonProperty("geocache") Geocache gc) {
        _geocache = gc;
    }

    // These set and get methods needs to be public otherwise the serialization will not work.
    public Geocache getGeocache(){ return _geocache; }
    public void setGeocache(Geocache geocache){ _geocache = geocache; };

    public String getNotes(){ return _userNotes; }
    public void setNotes(String notes){ this._userNotes = notes; }

    // não gosto deste nome. attempt, visit, ...?
    public FoundEnumType getVisit(){ return _found; }
    public void setVisit(FoundEnumType visit){this._found = visit;}

    public boolean getNeedsMaintenance(){ return _needsMaintenance; }
    public void setNeedsMaintenance(boolean needsMaintenance){this._needsMaintenance = needsMaintenance; }

    public Date getFoundDate(){ return _foundDate; }
    public void setFoundDate(Date foundDate){this._foundDate = foundDate; }

    public void setVisit(FoundEnumType found, String notes, Boolean needsMaintenance)
    {
        _found = found;
        _needsMaintenance = needsMaintenance;
        _foundDate = new Date();
        _userNotes = notes;
    }

}
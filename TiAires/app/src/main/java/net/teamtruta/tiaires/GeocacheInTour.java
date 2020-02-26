package net.teamtruta.tiaires;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GeocacheInTour
 */
public class GeocacheInTour
{
    Geocache geocache;

    // the following fields have the "In Tour" semantics. So if a cache was found but not in the tour, _found will be = NotAttempted; same logic applies to other fields.
    private String _userNotes = "";
    private FoundEnumType _found = FoundEnumType.NotAttempted;
    private Boolean _needsMaintenance = false;
    private Date _foundDate;

    GeocacheInTour(Geocache gc) {
        geocache = gc;
    }

    public void setVisit(FoundEnumType found, String notes, Boolean needsMaintenance)
    {
        _found = found;
        _needsMaintenance = needsMaintenance;
        _foundDate = new Date();
        _userNotes = notes;
    }

    String getNotes()
    {
        return _userNotes;
    }

    void setNotes(String notes){
        this._userNotes = notes;
    }

    FoundEnumType getVisit() // não gosto deste nome. attempt, visit, ...?
    {
        return _found;
    }

    void setVisit(FoundEnumType visit){
        _found = visit;
    }

    public Boolean getNeedsMaintenance()
    {
        return _needsMaintenance;
    }

    JSONObject toJSON(){

        JSONObject cacheJSON = new JSONObject();

        try {
            cacheJSON.put("notes", _userNotes);
            cacheJSON.put("foundIt", _found);
            cacheJSON.put("needsMaintenance", _needsMaintenance);

            if(_foundDate != null){
                cacheJSON.put("foundDate", _foundDate.toString());
            } else {
                cacheJSON.put("foundDate", "ND");
            }

            cacheJSON.put("geocache", geocache.toJSON());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cacheJSON;
    }

    static GeocacheInTour fromJSON(JSONObject cacheJSON){

        GeocacheInTour cacheInTour = null;
        String foundString;
        String foundDateString = "";

        try {
            JSONObject geocacheJSON = cacheJSON.getJSONObject("geocache");
            Geocache geocache = Geocache.fromJSON(geocacheJSON);
            cacheInTour = new GeocacheInTour(geocache);
            foundString = cacheJSON.get("foundIt").toString();
            cacheInTour._found = FoundEnumType.valueOf(foundString);
            cacheInTour._userNotes = cacheJSON.getString("notes");
            cacheInTour._needsMaintenance = cacheJSON.getBoolean("needsMaintenance");
            foundDateString = cacheJSON.getString("foundDate");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(cacheInTour._found == FoundEnumType.Found || cacheInTour._found == FoundEnumType.DNF){
            try{
                cacheInTour._foundDate = new SimpleDateFormat("dd/MM/yyyy").parse(foundDateString);
            } catch(ParseException e){
                // There should be date there but it's not parseable
                e.printStackTrace();
            }
        }

        return cacheInTour;

    }
}

// TODO: Java suporta valores default nos parâmetros? se sim qual a sintaxe?
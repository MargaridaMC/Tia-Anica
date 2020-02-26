package app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * GeocacheInTour
 */
public class GeocacheInTour 
{
    public Geocache geocache;

    // the following fields have the "In Tour" semantics. So if a cache was found but not in the tour, _found will be = NotAttempted; same logic applies to other fields.
    private String _userNotes = "";
    private FoundEnumType _found = FoundEnumType.NotAttempted;
    private Boolean _needsMaintenance = false;
    private Date _foundDate;

    public GeocacheInTour(Geocache gc) {
        geocache = gc;
    }

    public void setVisit(FoundEnumType found, String notes, Boolean needsMaintenance)
    {
        _found = found;
        _needsMaintenance = needsMaintenance;
        _foundDate = new Date();
        _userNotes = notes;
    }

    public String getNotes()
    {
        return _userNotes;
    }

    public FoundEnumType getVisit() // não gosto deste nome. attempt, visit, ...?
    {
        return _found;
    }

    public Boolean getNeedsMaintenance()
    {
        return _needsMaintenance;
    }

    public JSONObject toJSON(){

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
    
    public static GeocacheInTour fromJSON(JSONObject cacheJSON){
        
        JSONObject geocacheJSON = cacheJSON.getJSONObject("geocache");
        Geocache geocache = Geocache.fromJSON(geocacheJSON);
     
        GeocacheInTour cacheInTour = new GeocacheInTour(geocache);

        String foundString = cacheJSON.get("foundIt").toString();
        cacheInTour._found = FoundEnumType.valueOf(foundString);
        cacheInTour._userNotes = cacheJSON.getString("notes");
        cacheInTour._needsMaintenance = cacheJSON.getBoolean("needsMaintenance");
        String foundDateString = cacheJSON.getString("foundDate");
        if(cacheInTour._found == FoundEnumType.Found || cacheInTour._found == FoundEnumType.Found.DNF){
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
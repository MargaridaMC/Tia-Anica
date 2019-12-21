package app;

import java.util.Date;

/**
 * GeocacheInTour
 */
public class TourGeocache 
{
    public Geocache geocache;

    // the following fields have the "In Tour" semantics. So if a cache was found but not in the tour, _found will be = NotAttempted; same logic applies to other fields.
    private String _userNotes;
    private FoundEnumType _found = FoundEnumType.NotAttempted;
    private Boolean _needsMaintenance = false;
    private Date _foundDate;

    public TourGeocache(Geocache gc) {
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
}

// TODO: Java suporta valores default nos parâmetros? se sim qual a sintaxe?
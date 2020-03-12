package app;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Geocache {
    String _code;
    public String _name;
    String _latitude;
    String _longitude;
    String _size;
    String _difficulty;
    String _terrain;
    //String type; // Normal, etc.
    CacheTypeEnum _type = CacheTypeEnum.Other;
    FoundEnumType _foundIt; // 0 - no, 1 - DNF, 2 - yes
    String _hint;
    int _favourites;
    ArrayList<GeocacheLog> _recentLogs = new ArrayList<>();

    // These set and get methods needs to be public otherwise the serialization will not work.
    public String getCode(){ return _code; }
    public void setCode(String code){ this._code = code;}

    public String getName(){ return _name; }
    public void setName(String name){ this._name = name;}

    public String getLatitude(){ return _latitude; }
    public void setLatitude(String latitude){ this._latitude = latitude;}

    public String getLongitude(){ return _longitude; }
    public void setLongitude(String longitude){ this._longitude = longitude;}

    public String getSize(){ return _size; }
    public void setSize(String size){ this._size = size;}

    public String getDifficulty(){ return _difficulty; }
    public void setDifficulty(String difficulty){ this._difficulty = difficulty;}

    public String getTerrain(){ return _terrain; }
    public void setTerrain(String terrain){ this._terrain = terrain;}

    public CacheTypeEnum getType(){ return _type;}
    public void setType(CacheTypeEnum type){ this._type = type; }

    public FoundEnumType getFoundIt(){ return _foundIt; }
    public void setFoundIt(FoundEnumType foundIt){ this._foundIt = foundIt;}

    public String getHint(){ return _hint; }
    public void setHint(String hint){ this._hint = hint;}

    public int getFavourites(){ return _favourites; }
    public void setFavourites(int favourites){ this._favourites = favourites;}

    public ArrayList<GeocacheLog> getRecentLogs(){ return _recentLogs;}
    public void setRecentLogs(ArrayList<GeocacheLog> recentLogs){this._recentLogs = recentLogs;}


    public long CountDaysSinceLastFind() {
        if (_recentLogs == null || _recentLogs.size() == 0)
            return 0;

        // I'm not going to comment what I think about this line of _code, esp if compared with the C# version.
        // #language-of-the-flintstones
        return ChronoUnit.DAYS.between(_recentLogs.get(0).logDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public double AverageDaysBetweenFinds()
    {
        if (_recentLogs == null || _recentLogs.size() == 0)
            return 0;

        long daysDifference = ChronoUnit.DAYS.between(_recentLogs.get(_recentLogs.size()-1).logDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        return daysDifference / (double) _recentLogs.size();
    }

   
    boolean hasHint(){

        return !_hint.equals("NO MATCH");

    }
}
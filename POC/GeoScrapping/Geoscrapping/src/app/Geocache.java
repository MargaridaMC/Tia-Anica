package app;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;
import org.json.JSONException;

public class Geocache {
    String code;
    public String name;
    String latitude;
    String longitude;
    String size;
    String difficulty;
    String terrain;
    //String type; // Normal, etc.
    CacheTypeEnum type = CacheTypeEnum.Other;
    int foundIt; // 0 - no, 1 - DNF, 2 - yes
    String hint;
    int favourites;
    ArrayList<GeocacheLog> recentLogs = new ArrayList<>();

    public long CountDaysSinceLastFind() {
        if (recentLogs == null || recentLogs.size() == 0)
            return 0;

        // I'm not going to comment what I think about this line of code, esp if compared with the C# version.
        // #language-of-the-flintstones
        return ChronoUnit.DAYS.between(recentLogs.get(0).logDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public double AverageDaysBetweenFinds()
    {
        if (recentLogs == null || recentLogs.size() == 0)
            return 0;

        long daysDifference = ChronoUnit.DAYS.between(recentLogs.get(recentLogs.size()-1).logDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
        new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        return daysDifference / (double) recentLogs.size();
    }

    JSONObject toJSON(){

        JSONObject cacheJSON = new JSONObject();

        try {
            cacheJSON.put("code", code);
            cacheJSON.put("name", name);
            cacheJSON.put("latitude", latitude);
            cacheJSON.put("longitude", longitude);
            cacheJSON.put("size", size);
            cacheJSON.put("difficulty", difficulty);
            cacheJSON.put("terrain", terrain);
            cacheJSON.put("type", type);
            cacheJSON.put("foundIt", foundIt);
            cacheJSON.put("hint", hint);
            cacheJSON.put("favourites", favourites);
            //cacheJSON.put("recentLogs", recentLogs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cacheJSON;
    }

    static Geocache fromJSON(JSONObject cacheJSON){

        // TODO: check for null
        Geocache cache = new Geocache();

        try {
            cache.code = cacheJSON.getString("code");
            cache.name = cacheJSON.getString("name");
            cache.latitude = cacheJSON.getString("latitude");
            cache.longitude = cacheJSON.getString("longitude");
            cache.size = cacheJSON.getString("size");
            cache.difficulty = cacheJSON.getString("difficulty");
            cache.terrain = cacheJSON.getString("terrain");
            String typeString = cacheJSON.get("type").toString();
            cache.type = CacheTypeEnum.valueOf(typeString);
            cache.foundIt = cacheJSON.getInt("foundIt");
            cache.hint = cacheJSON.getString("hint");
            cache.favourites = cacheJSON.getInt("favourites");
            //recentLogs = (ArrayList<GeocacheLog>) cacheJSON.get("recentLogs"); // Unsure if this will work
        } catch (JSONException e) {
            e.printStackTrace();
        }
    
        return cache;

    }
}
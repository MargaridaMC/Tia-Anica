package net.teamtruta.tiaires;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

public class Geocache {
    String code;
    public String name;
    String latitude;
    String longitude;
    String size;
    String difficulty;
    String terrain;
    String type; // Normal, etc.
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

    public JSONObject toJSON(){

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
            cacheJSON.put("recentLogs", recentLogs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cacheJSON;
    }

    public void fromJSON(JSONObject cacheJSON){

        try {
            code = cacheJSON.get("code").toString();
            name = cacheJSON.get("name").toString();
            latitude = cacheJSON.get("latitude").toString();
            longitude = cacheJSON.get("longitude").toString();
            size = cacheJSON.get("size").toString();
            difficulty = cacheJSON.get("difficulty").toString();
            terrain = cacheJSON.get("terrain").toString();
            type = cacheJSON.get("type").toString();
            foundIt = (int) cacheJSON.get("foundIt");
            hint = cacheJSON.get("hint").toString();
            favourites = (int) cacheJSON.get("favourites");
            recentLogs = (ArrayList<GeocacheLog>) cacheJSON.get("recentLogs");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
package app;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;

public class Geocache {
    public String code;
    public String name;
    public String latitude;
    public String longitude;
    public String size;
    public String difficulty;
    public String terrain;
    public String type; // Normal, etc.
    public int foundIt; // 0 - no, 1 - DNF, 2 - yes
    public String hint;
    public int favourites;
    public ArrayList<GeocacheLog> recentLogs = new ArrayList<GeocacheLog>();

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
}
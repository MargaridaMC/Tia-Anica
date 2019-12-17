package app;

import java.util.Date;

public class Geocache {
    public String name;
    public String latitude;
    public String longitude;
    public String size;
    public String difficulty;
    public String terrain;
    public String type; // Normal, etc.

    public int foundIt; // 0 - no, 1 - DNF, 2 - yes
    public Date lastLogDate; // ou LastFound + LastLogType
}
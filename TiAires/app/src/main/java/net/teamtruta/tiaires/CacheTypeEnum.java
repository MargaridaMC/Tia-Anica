package net.teamtruta.tiaires;

public enum CacheTypeEnum{

    Traditional("Traditional Geocache"),
    Mystery("Mystery Cache"),
    Multi("Multi"),
    Earth("EarthCache"),
    Letterbox("Letterbox Hybrid"),
    Event("Event Cache"),
    CITO("Cache In Trash Out Event"),
    Mega("Mega"),
    Giga("Giga"),
    Wherigo("Wherigo Cache"),
    HQ("Geocaching HQ"),
    GPSAdventures("GPS Adventures Exhibit"),
    Lab("Lab"),
    HQCelebration("Geocaching HQ Celebration"),
    HQBlockParty("Geocaching HQ Block Party"),
    CommunityCelebration("Community Celebration Event"),
    Virtual("Virtual Cache"),
    Webcam("Webcam Cache"),
    ProjectAPE("Project APE Cache"),
    Locationless("Locationless (Reverse) Cache"),
    Other(null); 

    private String typeString;

    public String getTypeString(){
        return typeString;
    }

    private CacheTypeEnum(String type){
        this.typeString = type;
    }

    public static CacheTypeEnum valueOfString(String typeString) {
        for (CacheTypeEnum e : values()) {
            if (e.typeString.equals(typeString)) {
                return e;
            }
        }
        return Other;
    }
}
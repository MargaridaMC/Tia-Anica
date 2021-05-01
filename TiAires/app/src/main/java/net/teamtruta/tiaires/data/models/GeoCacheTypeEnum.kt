package net.teamtruta.tiaires.data.models

enum class GeoCacheTypeEnum(val typeString: String) {
    Traditional("Traditional Geocache"),
    Mystery("Mystery Cache"),
    Solved("Solved Cache"),
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
    Other("");

    companion object {
        fun valueOfString(typeString: String): GeoCacheTypeEnum {
            for (e in values()) {
                if (e.typeString == typeString) {
                    return e
                }
            }
            return Other
        }

        fun geoCacheCanHaveWaypoints(geoCacheType: GeoCacheTypeEnum): Boolean{
            return geoCacheType == Multi
        }
    }

}
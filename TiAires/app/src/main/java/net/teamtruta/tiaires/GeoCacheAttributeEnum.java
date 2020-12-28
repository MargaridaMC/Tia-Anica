package net.teamtruta.tiaires;

public enum GeoCacheAttributeEnum
{

    BoatRequired("Boat required"),
    ClimbingGearRequired("Climbing gear required"),
    DangerousArea("Dangerous area" ),
    FlashlightRequired("Flashlight required"),
    MayRequireSnowShoes("May require snowshoes"),
    MayRequireSkis("May require cross country skis"),
    MayRequireSwimming("May require swimming"),
    MayRequireWading("May require wading"),
    NightCache("Night cache"),
    NotAvailable47("Not available 24/7"),
    ScubaGearRequired("Scuba gear required"),
    SeasonalAccessOnly("Seasonal access only"),
    SpecialToolRequired("Special tool required"),
    TeamworkCache("Teamwork cache"),
    TreeClimbingRequired("Tree climbing required"),
    UVLightRequired("UV light required"),
    //"Field puzzle"
    None("None");

    public String attributeString;

    GeoCacheAttributeEnum(String s) {
        this.attributeString = s;
    }

    public static GeoCacheAttributeEnum valueOfString(String typeString) {
        for (GeoCacheAttributeEnum e : values()) {
            if (e.attributeString.equals(typeString)) {
                return e;
            }
        }
        return None;
    }
}

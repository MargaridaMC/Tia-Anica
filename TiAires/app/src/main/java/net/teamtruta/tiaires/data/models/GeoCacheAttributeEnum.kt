package net.teamtruta.tiaires.data.models

enum class GeoCacheAttributeEnum(var attributeString: String) {
    BoatRequired("Boat required"),
    ClimbingGearRequired("Climbing gear required"),
    DangerousArea("Dangerous area"),
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
    NeedsMaintenance("Needs maintenance"),  //"Field puzzle"
    None("None");

    companion object {
        @JvmStatic
        fun valueOfString(typeString: String): GeoCacheAttributeEnum {
            for (e in values()) {
                if (e.attributeString == typeString) {
                    return e
                }
            }
            return None
        }
    }

}


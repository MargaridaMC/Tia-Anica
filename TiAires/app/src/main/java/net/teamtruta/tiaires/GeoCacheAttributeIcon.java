package net.teamtruta.tiaires;

public class GeoCacheAttributeIcon
{

    public static int getGeoCacheAttributeIcon(GeoCacheAttributeEnum attribute){

        int drawableID = -1;
        switch (attribute){
            case NightCache:
                drawableID = R.drawable.attribute_night_geo_cache;
                break;
            case BoatRequired:
                drawableID = R.drawable.attribute_boat_required;
                break;
            case DangerousArea:
                drawableID = R.drawable.attribute_dangerous_area;
                break;
            case TeamworkCache:
                drawableID = R.drawable.attribute_teamwork_geo_cache;
                break;
            case MayRequireSkis:
                drawableID = R.drawable.attribute_skiis_required;
                break;
            case NotAvailable47:
                drawableID = R.drawable.attribute_not_available_247;
                break;
            case UVLightRequired:
                drawableID = R.drawable.attribute_uv_light_required;
                break;
            case MayRequireWading:
                drawableID = R.drawable.attribute_wading_required;
                break;
            case ScubaGearRequired:
                drawableID = R.drawable.attribute_scuba_gear_required;
                break;
            case FlashlightRequired:
                drawableID = R.drawable.attribute_flashlight_required;
                break;
            case MayRequireSwimming:
                drawableID = R.drawable.attribute_swimming_required;
                break;
            case SeasonalAccessOnly:
                drawableID = R.drawable.attribute_seasonal_access_only;
                break;
            case MayRequireSnowShoes:
                drawableID = R.drawable.attribute_snowshoes_required;
                break;
            case SpecialToolRequired:
                drawableID = R.drawable.attribute_special_tool_required;
                break;
            case ClimbingGearRequired:
                drawableID = R.drawable.attribute_climbing_gear_required;
                break;
            case TreeClimbingRequired:
                drawableID = R.drawable.attribute_treeclimbing_required;
                break;
            case NeedsMaintenance:
                drawableID = R.drawable.attribute_needs_maintenance;
                break;
            default:
                drawableID = R.drawable.attribute_unknown;
                break;
        }

        return drawableID;

    }

    public static int getGeoCacheAttributeIcon(String geoCacheAttributeTypeString){
        GeoCacheAttributeEnum attributeType = GeoCacheAttributeEnum.valueOfString(geoCacheAttributeTypeString);
        return getGeoCacheAttributeIcon(attributeType);
    }
}

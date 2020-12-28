package net.teamtruta.tiaires;

public class GeoCacheIcon
{

    public static int getIconDrawable(GeoCacheTypeEnum geoCacheType){

            int drawableID;
            switch (geoCacheType){
                case Traditional:
                    drawableID = R.drawable.geo_cache_icon_type_traditional50x50;
                    break;
                case Mystery:
                    drawableID = R.drawable.geo_cache_icon_type_mystery50x50;
                    break;
                case Multi:
                    drawableID = R.drawable.geo_cache_icon_type_multi50x50;
                    break;
                case Earth:
                    drawableID = R.drawable.geo_cache_icon_type_earth50x50;
                    break;
                case Letterbox:
                    drawableID = R.drawable.geo_cache_icon_type_letterbox50x50;
                    break;
                case Event:
                    drawableID = R.drawable.geo_cache_icon_type_event50x50;
                    break;
                case CITO:
                    drawableID = R.drawable.geo_cache_icon_type_cito50x50;
                    break;
                case Mega:
                    drawableID = R.drawable.geo_cache_icon_type_mega50x50;
                    break;
                case Giga:
                    drawableID = R.drawable.geo_cache_icon_type_giga50x50;
                    break;
                case Wherigo:
                    drawableID = R.drawable.geo_cache_icon_type_wherigo50x50;
                    break;
                case HQ:
                    drawableID = R.drawable.geo_cache_icon_type_hq50x50;
                    break;
                case GPSAdventures:
                    drawableID = R.drawable.geo_cache_icon_type_gps_adventures50x50;
                    break;
                case HQCelebration:
                    drawableID = R.drawable.geo_cache_icon_type_hq_celebration50x50;
                    break;
                case HQBlockParty:
                    drawableID = R.drawable.geo_cache_icon_type_hq_blockparty50x50;
                    break;
                case CommunityCelebration:
                    drawableID = R.drawable.geo_cache_icon_type_community_event50x50;
                    break;
                case Virtual:
                    drawableID = R.drawable.geo_cache_icon_type_virtual50x50;
                    break;
                case Webcam:
                    drawableID = R.drawable.geo_cache_icon_type_webcam50x50;
                    break;
                case ProjectAPE:
                    drawableID = R.drawable.geo_cache_icon_type_project_ape50x50;
                    break;
                case Locationless:
                    drawableID = R.drawable.geo_cache_icon_type_locationless50x50;
                    break;
                case Solved:
                    drawableID = R.drawable.geo_cache_icon_solved50x50;
                    break;
                default:
                    drawableID = R.drawable.shrug;
                    break;
            }

        return drawableID;
    }

    public static int getIconDrawable(String geoCacheTypeString){
        GeoCacheTypeEnum geoCacheType = GeoCacheTypeEnum.valueOfString(geoCacheTypeString);
        return getIconDrawable(geoCacheType);
    }

}

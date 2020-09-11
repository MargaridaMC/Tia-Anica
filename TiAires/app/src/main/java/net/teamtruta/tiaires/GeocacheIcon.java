package net.teamtruta.tiaires;

public class GeocacheIcon {

    public static int getIconDrawable(CacheTypeEnum cacheType){

            int drawableID;
            switch (cacheType){
                case Traditional:
                    drawableID = R.drawable.cache_icon_type_traditional;
                    break;
                case Mystery:
                    drawableID = R.drawable.cache_icon_type_mystery;
                    break;
                case Multi:
                    drawableID = R.drawable.cache_icon_type_multi;
                    break;
                case Earth:
                    drawableID = R.drawable.cache_icon_type_earth;
                    break;
                case Letterbox:
                    drawableID = R.drawable.cache_icon_type_letterbox;
                    break;
                case Event:
                    drawableID = R.drawable.cache_icon_type_event;
                    break;
                case CITO:
                    drawableID = R.drawable.cache_icon_type_cito;
                    break;
                case Mega:
                    drawableID = R.drawable.cache_icon_type_mega;
                    break;
                case Giga:
                    drawableID = R.drawable.cache_icon_type_giga;
                    break;
                case Wherigo:
                    drawableID = R.drawable.cache_icon_type_wherigo;
                    break;
                case HQ:
                    drawableID = R.drawable.cache_icon_type_hq;
                    break;
                case GPSAdventures:
                    drawableID = R.drawable.cache_icon_type_gps_adventures;
                    break;
                case HQCelebration:
                    drawableID = R.drawable.cache_icon_type_hq_celebration;
                    break;
                case HQBlockParty:
                    drawableID = R.drawable.cache_icon_type_hq_blockparty;
                    break;
                case CommunityCelebration:
                    drawableID = R.drawable.cache_icon_type_community_event;
                    break;
                case Virtual:
                    drawableID = R.drawable.cache_icon_type_virtual;
                    break;
                case Webcam:
                    drawableID = R.drawable.cache_icon_type_webcam;
                    break;
                case ProjectAPE:
                    drawableID = R.drawable.cache_icon_type_project_ape;
                    break;
                case Locationless:
                    drawableID = R.drawable.cache_icon_type_locationless;
                    break;
                case Solved:
                    drawableID = R.drawable.cache_icon_solved;
                    break;
                default:
                    drawableID = R.drawable.shrug;
                    break;
            }

        return drawableID;
    }

    public static int getIconDrawable(String cacheTypeString){
        CacheTypeEnum cacheType = CacheTypeEnum.valueOfString(cacheTypeString);
        return getIconDrawable(cacheType);
    }

}

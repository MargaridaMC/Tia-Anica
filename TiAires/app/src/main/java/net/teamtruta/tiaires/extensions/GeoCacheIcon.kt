package net.teamtruta.tiaires.extensions

import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheTypeEnum

class GeoCacheIcon {

    companion object {
        fun getIconDrawable(geoCacheType: GeoCacheTypeEnum): Int {
            return when (geoCacheType) {
                GeoCacheTypeEnum.Traditional -> R.drawable.geo_cache_icon_type_traditional
                GeoCacheTypeEnum.Mystery -> R.drawable.geo_cache_icon_type_mystery
                GeoCacheTypeEnum.Multi -> R.drawable.geo_cache_icon_type_multi
                GeoCacheTypeEnum.Earth -> R.drawable.geo_cache_icon_type_earth
                GeoCacheTypeEnum.Letterbox -> R.drawable.geo_cache_icon_type_letterbox
                GeoCacheTypeEnum.Event -> R.drawable.geo_cache_icon_type_event
                GeoCacheTypeEnum.CITO -> R.drawable.geo_cache_icon_type_cito
                GeoCacheTypeEnum.Mega -> R.drawable.geo_cache_icon_type_mega
                GeoCacheTypeEnum.Giga -> R.drawable.geo_cache_icon_type_giga
                GeoCacheTypeEnum.Wherigo -> R.drawable.geo_cache_icon_type_wherigo
                GeoCacheTypeEnum.HQ -> R.drawable.geo_cache_icon_type_hq
                GeoCacheTypeEnum.GPSAdventures -> R.drawable.geo_cache_icon_type_gps_adventures
                GeoCacheTypeEnum.HQCelebration -> R.drawable.geo_cache_icon_type_hq_celebration
                GeoCacheTypeEnum.HQBlockParty -> R.drawable.geo_cache_icon_type_hq_blockparty
                GeoCacheTypeEnum.CommunityCelebration -> R.drawable.geo_cache_icon_type_community_event
                GeoCacheTypeEnum.Virtual -> R.drawable.geo_cache_icon_type_virtual
                GeoCacheTypeEnum.Webcam -> R.drawable.geo_cache_icon_type_webcam
                GeoCacheTypeEnum.ProjectAPE -> R.drawable.geo_cache_icon_type_project_ape
                GeoCacheTypeEnum.Locationless -> R.drawable.geo_cache_icon_type_locationless
                GeoCacheTypeEnum.Solved -> R.drawable.geo_cache_icon_solved
                else -> R.drawable.shrug
            }
        }

        fun getIconDrawable(geoCacheTypeString: String): Int {
            val geoCacheType = GeoCacheTypeEnum.valueOfString(geoCacheTypeString)
            return getIconDrawable(geoCacheType)
        }

    }
}
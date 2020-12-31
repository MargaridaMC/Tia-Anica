package net.teamtruta.tiaires

object GeoCacheIcon {
    fun getIconDrawable(geoCacheType: GeoCacheTypeEnum?): Int {
        val drawableID: Int
        drawableID = when (geoCacheType) {
            GeoCacheTypeEnum.Traditional -> R.drawable.geo_cache_icon_type_traditional50x50
            GeoCacheTypeEnum.Mystery -> R.drawable.geo_cache_icon_type_mystery50x50
            GeoCacheTypeEnum.Multi -> R.drawable.geo_cache_icon_type_multi50x50
            GeoCacheTypeEnum.Earth -> R.drawable.geo_cache_icon_type_earth50x50
            GeoCacheTypeEnum.Letterbox -> R.drawable.geo_cache_icon_type_letterbox50x50
            GeoCacheTypeEnum.Event -> R.drawable.geo_cache_icon_type_event50x50
            GeoCacheTypeEnum.CITO -> R.drawable.geo_cache_icon_type_cito50x50
            GeoCacheTypeEnum.Mega -> R.drawable.geo_cache_icon_type_mega50x50
            GeoCacheTypeEnum.Giga -> R.drawable.geo_cache_icon_type_giga50x50
            GeoCacheTypeEnum.Wherigo -> R.drawable.geo_cache_icon_type_wherigo50x50
            GeoCacheTypeEnum.HQ -> R.drawable.geo_cache_icon_type_hq50x50
            GeoCacheTypeEnum.GPSAdventures -> R.drawable.geo_cache_icon_type_gps_adventures50x50
            GeoCacheTypeEnum.HQCelebration -> R.drawable.geo_cache_icon_type_hq_celebration50x50
            GeoCacheTypeEnum.HQBlockParty -> R.drawable.geo_cache_icon_type_hq_blockparty50x50
            GeoCacheTypeEnum.CommunityCelebration -> R.drawable.geo_cache_icon_type_community_event50x50
            GeoCacheTypeEnum.Virtual -> R.drawable.geo_cache_icon_type_virtual50x50
            GeoCacheTypeEnum.Webcam -> R.drawable.geo_cache_icon_type_webcam50x50
            GeoCacheTypeEnum.ProjectAPE -> R.drawable.geo_cache_icon_type_project_ape50x50
            GeoCacheTypeEnum.Locationless -> R.drawable.geo_cache_icon_type_locationless50x50
            GeoCacheTypeEnum.Solved -> R.drawable.geo_cache_icon_solved50x50
            else -> R.drawable.shrug
        }
        return drawableID
    }

    fun getIconDrawable(geoCacheTypeString: String?): Int {
        val geoCacheType = GeoCacheTypeEnum.valueOfString(geoCacheTypeString)
        return getIconDrawable(geoCacheType)
    }
}
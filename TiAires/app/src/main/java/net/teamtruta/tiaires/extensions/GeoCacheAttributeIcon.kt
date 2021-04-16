package net.teamtruta.tiaires.extensions

import net.teamtruta.tiaires.R
import net.teamtruta.tiaires.data.models.GeoCacheAttributeEnum

class GeoCacheAttributeIcon {
    companion object {
        fun getGeoCacheAttributeIcon(attribute: GeoCacheAttributeEnum): Int {
            return when (attribute) {
                GeoCacheAttributeEnum.NightCache -> R.drawable.attribute_night_geo_cache
                GeoCacheAttributeEnum.BoatRequired -> R.drawable.attribute_boat_required
                GeoCacheAttributeEnum.DangerousArea -> R.drawable.attribute_dangerous_area
                GeoCacheAttributeEnum.TeamworkCache -> R.drawable.attribute_teamwork_geo_cache
                GeoCacheAttributeEnum.MayRequireSkis -> R.drawable.attribute_skiis_required
                GeoCacheAttributeEnum.NotAvailable47 -> R.drawable.attribute_not_available_247
                GeoCacheAttributeEnum.UVLightRequired -> R.drawable.attribute_uv_light_required
                GeoCacheAttributeEnum.MayRequireWading -> R.drawable.attribute_wading_required
                GeoCacheAttributeEnum.ScubaGearRequired -> R.drawable.attribute_scuba_gear_required
                GeoCacheAttributeEnum.FlashlightRequired -> R.drawable.attribute_flashlight_required
                GeoCacheAttributeEnum.MayRequireSwimming -> R.drawable.attribute_swimming_required
                GeoCacheAttributeEnum.SeasonalAccessOnly -> R.drawable.attribute_seasonal_access_only
                GeoCacheAttributeEnum.MayRequireSnowShoes -> R.drawable.attribute_snowshoes_required
                GeoCacheAttributeEnum.SpecialToolRequired -> R.drawable.attribute_special_tool_required
                GeoCacheAttributeEnum.ClimbingGearRequired -> R.drawable.attribute_climbing_gear_required
                GeoCacheAttributeEnum.TreeClimbingRequired -> R.drawable.attribute_treeclimbing_required
                GeoCacheAttributeEnum.NeedsMaintenance -> R.drawable.attribute_needs_maintenance
                else -> R.drawable.attribute_unknown
            }
        }
    }
}
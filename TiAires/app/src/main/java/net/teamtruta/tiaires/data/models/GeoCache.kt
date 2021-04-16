package net.teamtruta.tiaires.data.models

import androidx.room.*
import com.mapbox.mapboxsdk.geometry.LatLng

@Entity(tableName = "cacheDetail")
class GeoCache (

        val code: String,
        val name: String,
        val latitude: Coordinate,
        val longitude: Coordinate,
        val size: String,
        val difficulty: Double,
        val terrain: Double,
        val type: GeoCacheTypeEnum = GeoCacheTypeEnum.Other,
        val previousVisitOutcome: VisitOutcomeEnum = VisitOutcomeEnum.NotAttempted,
        val hint: String,
        val favourites: Int = 0,

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0
){
    constructor(code: String, name: String,
                latitude: Coordinate, longitude: Coordinate,
                size: String, difficulty: Double, terrain: Double,
                type: GeoCacheTypeEnum, visit: VisitOutcomeEnum, hint: String,
                favourites: Int) : this(code, name, latitude, longitude, size,
            difficulty, terrain, type, visit,
            hint, favourites, 0)

    val latLng: LatLng
        get() = LatLng(latitude.value, longitude.value)

    fun hasHint(): Boolean {
        return hint != "NO MATCH"
    }

}

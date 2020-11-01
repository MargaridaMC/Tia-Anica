package net.teamtruta.tiaires

import com.mapbox.mapboxsdk.geometry.LatLng
import net.teamtruta.tiaires.db.DbConnection
import kotlin.collections.ArrayList

class Geocache(val code: String, val name: String, val latitude: Coordinate,
               val longitude: Coordinate, val size: String, val difficulty: String,
               val terrain: String, val type: CacheTypeEnum = CacheTypeEnum.Other,
               val foundIt: FoundEnumType = FoundEnumType.NotAttempted,
               val hint: String, val favourites: Int = 0, val recentLogs: List<GeocacheLog>,
               val attributes: List<GeocacheAttributeEnum> = listOf(),
               var _id: Long = 0) {

    constructor(code: String, name: String, latitude: Coordinate, longitude: Coordinate,
                size: String, difficulty: String, terrain: String, type: CacheTypeEnum,
                visit: FoundEnumType, hint: String, favourites: Int,
                recentLogs: ArrayList<GeocacheLog>, attributes: List<GeocacheAttributeEnum>) :
            this(code, name, latitude, longitude, size, difficulty, terrain, type, visit, hint,
                    favourites, recentLogs, attributes, 0)

    val latLng: LatLng
        get() = LatLng(latitude.value, longitude.value)

    val dNFRisk: String
        get() {
            if (foundIt == FoundEnumType.Disabled) return "DISABLED"
            if (recentLogs.any { it.logType == FoundEnumType.NeedsMaintenance }) return "NEEDS MAINTENANCE"
            return ""
        }


    fun isDNFRisk(): Boolean {
        return dNFRisk != ""
    }

    fun hasHint(): Boolean {
        return hint != "NO MATCH"
    }


    fun getLastNLogs(n: Int): List<GeocacheLog> {
        return recentLogs.subList(0, n + 1)
    }


    companion object {

        lateinit var geocachingTourDelegate: GeocachingTour
        lateinit var dbConnection: DbConnection
        lateinit var cachesAlreadyInDb: MutableList<Long>
        private var overwrite : Boolean = false

        private fun existsInDb(code: String?, dbConnection: DbConnection): Long {
            return dbConnection.cacheDetailTable.contains(code!!)
        }

        fun getGeocaches(requestedGeocacheCodes: List<String>, dbConnection: DbConnection,
                         geocachingTourDelegate: GeocachingTour, overwrite : Boolean = false) {

            this.geocachingTourDelegate = geocachingTourDelegate
            this.dbConnection = dbConnection
            cachesAlreadyInDb =  mutableListOf()

            if(overwrite){
                this.overwrite = overwrite
                getGeocaches(requestedGeocacheCodes)
                return
            }

            val cachesToGet: MutableList<String> = ArrayList()

            // First check if any of the caches already exist in the database
            for (c in requestedGeocacheCodes) {
                val cacheID = existsInDb(c, dbConnection)
                if (cacheID != -1L) {
                    cachesAlreadyInDb.add(cacheID)
                } else {
                    cachesToGet.add(c)
                }
            }

            getGeocaches(cachesToGet)
        }

        private fun getGeocaches(requestedGeocacheCodes: List<String>) {
            val authCookie = App.getAuthenticationCookie()
            val scrapper = GeocachingScrapper(authCookie)
            val geocachingScrappingTask = GeocachingScrappingTask(scrapper, requestedGeocacheCodes)
            geocachingScrappingTask.execute()
        }

        fun onGeocachesObtained(obtainedCaches: MutableList<Geocache>) {
            // Store newly obtained caches

            if(overwrite){
                dbConnection.cacheDetailTable.update(obtainedCaches)
            } else {
                val obtainedCacheIDs = dbConnection.cacheDetailTable.store(obtainedCaches, false)
                cachesAlreadyInDb.addAll(obtainedCacheIDs)
                dbConnection.cacheTable.addCachesToTour(geocachingTourDelegate._id, cachesAlreadyInDb)
            }

            geocachingTourDelegate.onAllGeocachesObtained(overwrite)
        }
    }
}
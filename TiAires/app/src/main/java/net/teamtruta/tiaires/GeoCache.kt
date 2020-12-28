package net.teamtruta.tiaires

import com.mapbox.mapboxsdk.geometry.LatLng
import net.teamtruta.tiaires.db.DbConnection
import kotlin.collections.ArrayList

class GeoCache(val code: String, val name: String, val latitude: Coordinate,
               val longitude: Coordinate, val size: String, val difficulty: String,
               val terrain: String, val type: GeoCacheTypeEnum = GeoCacheTypeEnum.Other,
               val previousVisitOutcome: VisitOutcomeEnum = VisitOutcomeEnum.NotAttempted,
               val hint: String, val favourites: Int = 0, val recentLogs: List<GeoCacheLog>,
               val attributes: List<GeoCacheAttributeEnum> = listOf(),
               var _id: Long = 0) {

    constructor(code: String, name: String, latitude: Coordinate, longitude: Coordinate,
                size: String, difficulty: String, terrain: String, type: GeoCacheTypeEnum,
                visit: VisitOutcomeEnum, hint: String, favourites: Int,
                recentLogs: ArrayList<GeoCacheLog>, attributes: List<GeoCacheAttributeEnum>) :
            this(code, name, latitude, longitude, size, difficulty, terrain, type, visit, hint,
                    favourites, recentLogs, attributes, 0)

    val latLng: LatLng
        get() = LatLng(latitude.value, longitude.value)

    val dNFRisk: String
        get() {
            if (previousVisitOutcome == VisitOutcomeEnum.Disabled) return "DISABLED"
            if (recentLogs.any { it.logType == VisitOutcomeEnum.NeedsMaintenance }) return "NEEDS MAINTENANCE"
            return ""
        }


    fun isDNFRisk(): Boolean {
        return dNFRisk != ""
    }

    fun hasHint(): Boolean {
        return hint != "NO MATCH"
    }


    fun getLastNLogs(n: Int): List<GeoCacheLog> {
        return recentLogs.subList(0, n + 1)
    }


    companion object {

        lateinit var geocachingTourDelegate: GeocachingTour
        lateinit var dbConnection: DbConnection
        lateinit var geoCachesAlreadyInDb: MutableList<Long>
        private var overwrite : Boolean = false

        private fun existsInDb(code: String?, dbConnection: DbConnection): Long {
            return dbConnection.geoCacheDetailTable.contains(code!!)
        }

        fun getGeoCaches(requestedGeoCacheCodes: List<String>, dbConnection: DbConnection,
                         geocachingTourDelegate: GeocachingTour, overwrite : Boolean = false) {

            this.geocachingTourDelegate = geocachingTourDelegate
            this.dbConnection = dbConnection
            geoCachesAlreadyInDb =  mutableListOf()

            if(overwrite){
                this.overwrite = overwrite
                getGeoCaches(requestedGeoCacheCodes)
                return
            }

            val geoCachesToGet: MutableList<String> = ArrayList()

            // First check if any of the geoCaches already exist in the database
            for (c in requestedGeoCacheCodes) {
                val geoCacheID = existsInDb(c, dbConnection)
                if (geoCacheID != -1L) {
                    geoCachesAlreadyInDb.add(geoCacheID)
                } else {
                    geoCachesToGet.add(c)
                }
            }

            getGeoCaches(geoCachesToGet)
        }

        private fun getGeoCaches(requestedGeoCacheCodes: List<String>) {
            val authCookie = App.getAuthenticationCookie()
            val scrapper = GeocachingScrapper(authCookie)
            val geocachingScrappingTask = GeocachingScrappingTask(scrapper, requestedGeoCacheCodes)
            geocachingScrappingTask.execute()
        }

        fun onGeoCachesObtained(obtainedGeoCaches: MutableList<GeoCache>) {
            // Store newly obtained geoCaches

            if(overwrite){
                dbConnection.geoCacheDetailTable.update(obtainedGeoCaches)
            } else {
                val obtainedGeoCacheIDs = dbConnection.geoCacheDetailTable.store(obtainedGeoCaches, false)
                geoCachesAlreadyInDb.addAll(obtainedGeoCacheIDs)
                dbConnection.geoCacheTable.addGeoCachesToTour(geocachingTourDelegate._id, geoCachesAlreadyInDb)
            }

            geocachingTourDelegate.onAllGeoCachesObtained(overwrite)
        }
    }
}
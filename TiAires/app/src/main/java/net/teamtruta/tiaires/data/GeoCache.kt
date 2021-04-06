package net.teamtruta.tiaires.data

import androidx.room.*
import com.mapbox.mapboxsdk.geometry.LatLng
import net.teamtruta.tiaires.*

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

    /*
    constructor(code: String, name: String, latitude: Coordinate, longitude: Coordinate,
                size: String, difficulty: Double, terrain: Double, type: GeoCacheTypeEnum,
                visit: VisitOutcomeEnum, hint: String, favourites: Int,
                recentLogs: ArrayList<GeoCacheLog>, attributes: List<GeoCacheAttributeEnum>) :
            this(code, name, latitude, longitude, size, difficulty, terrain, type, visit, hint,
                    favourites, recentLogs, attributes, 0)
*/
    val latLng: LatLng
        get() = LatLng(latitude.value, longitude.value)

    fun hasHint(): Boolean {
        return hint != "NO MATCH"
    }

    /*

    companion object {

        lateinit var geocachingTourDelegate: GeocachingTour
        lateinit var dbConnection: DbConnection
        lateinit var geoCachesAlreadyInDb: MutableList<Long>
        private var overwrite : Boolean = false

        private fun existsInDb(code: String?, dbConnection: DbConnection): Long {
            // This should be in Dao probably
            return dbConnection.geoCacheDetailTable.contains(code!!)
        }

        fun getGeoCaches(requestedGeoCacheCodes: List<String>, dbConnection: DbConnection,
                         geocachingTourDelegate: GeocachingTour, overwrite : Boolean = false) {
            // TODO this will need refactoring
            Companion.geocachingTourDelegate = geocachingTourDelegate
            Companion.dbConnection = dbConnection
            geoCachesAlreadyInDb =  mutableListOf()

            if(overwrite){
                Companion.overwrite = overwrite
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
            // TODO
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

*/
}

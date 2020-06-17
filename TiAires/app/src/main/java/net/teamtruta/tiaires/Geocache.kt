package net.teamtruta.tiaires

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Geocache(val code: String, val name: String, val latitude: Coordinate,
               val longitude: Coordinate, val size: String, val difficulty: String,
               val terrain: String, val type: CacheTypeEnum = CacheTypeEnum.Other,
               val foundIt: FoundEnumType = FoundEnumType.NotAttempted,
               val hint: String, val favourites: Int = 0, val recentLogs: List<GeocacheLog>,
               var _id: Long = 0, var dNFRisk: String = "") {

    constructor(code: String, name: String, latitude: Coordinate, longitude: Coordinate,
                size: String, difficulty: String, terrain: String, type: CacheTypeEnum,
                visit: FoundEnumType, hint: String, favourites: Int,
                recentLogs: ArrayList<GeocacheLog>) : this(code, name, latitude, longitude, size,
            difficulty, terrain, type, visit, hint, favourites, recentLogs, 0, "")

/*
    constructor() {}

    constructor(dbConnection: DbConnection?) {
        _dbConnection = dbConnection
    }

    fun setRecentLogs(recentLogs: ArrayList<GeocacheLog>?) {
        this.recentLogs = recentLogs
    }
*/

    val latLng: LatLng
        get() = LatLng(latitude.value, longitude.value)

 /*   fun countDNFsInLastLogs(numberOfLogsToCheck: Int): Int {
        val logsToCheck = recentLogs.subList(0, numberOfLogsToCheck)
        return logsToCheck.stream().filter { (logType) -> logType == FoundEnumType.DNF }.count().toInt()
    }*/

    fun isDNFRisk(): Boolean {
        return dNFRisk != ""
    }

    /*fun setDNFRisk(): String {
        val (logType) = recentLogs[0]
        if (logType == FoundEnumType.Disabled) {
            dNFRisk = "Disabled"
            return dNFRisk
        }
        if (logType == FoundEnumType.NeedsMaintenance) {
            dNFRisk = "Needs Maintenance"
            return dNFRisk
        }
        val maxLogs = 10
        val nDNFs = countDNFsInLastLogs(maxLogs)
        if (nDNFs >= 2) {
            dNFRisk = "DNFs in last $maxLogs: $nDNFs"

            // Count DNFs since last log
            if (recentLogs[0].logType == FoundEnumType.DNF) {
                var i = 0
                while (recentLogs[i].logType == FoundEnumType.DNF) i++
                dNFRisk += " including last $i"
            }
        }
        return dNFRisk
    }*/

    fun hasHint(): Boolean {
        return hint != "NO MATCH"
    }


    fun getLastNLogs(n: Int): List<GeocacheLog> {
        return recentLogs.subList(0, n + 1)
    }

    val dNFRiskShort: String
        get() {
            val logType = recentLogs[0].logType
            if (logType == FoundEnumType.Disabled) return "Disabled"
            return if (logType == FoundEnumType.NeedsMaintenance) "Needs Maintenance" else "DNF Risk"
        }

    companion object {

        //var _dbConnection: DbConnection? = null

        fun existsInDb(code: String?, dbConnection: DbConnection): Long {
            return dbConnection.cacheDetailTable.contains(code!!)
        }


        fun getGeocaches(requestedCaches: List<String>, dbConnection: DbConnection, geocachingTour: GeocachingTour): List<Long> {
            val cacheListIds: MutableList<Long> = ArrayList()
            val cachesToGet: MutableList<String> = ArrayList()

            // First check if any of the caches already exist in the database
            for (c in requestedCaches) {
                val cacheID = existsInDb(c, dbConnection)
                if (cacheID != -1L) {
                    cacheListIds.add(cacheID)
                } else {
                    cachesToGet.add(c)
                }
            }

            // Scrape the remaining caches
            val authCookie = App.getAuthenticationCookie()
            val scrapper = GeocachingScrapper(authCookie)
            val geocachingScrappingTask = GeocachingScrappingTask(scrapper, cachesToGet)
            geocachingScrappingTask.delegate = geocachingTour
            geocachingScrappingTask.execute()
            return cacheListIds
        }
    }


    /*fun CountDaysSinceLastFind(): Long {
        if (recentLogs == null || recentLogs!!.size == 0) return 0

        // I'm not going to comment what I think about this line of _code, esp if compared with the C# version.
        // #language-of-the-flintstones
        val (_, logDate) = recentLogs!!.stream().filter { (logType) -> logType == FoundEnumType.Found }.findFirst().get()
        return ChronoUnit.DAYS.between(logDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
    }

    fun AverageDaysBetweenFinds(): Double {
        if (recentLogs == null || recentLogs!!.size == 0) return 0
        val daysDifference = ChronoUnit.DAYS.between(recentLogs!![recentLogs!!.size - 1].logDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
        return daysDifference / recentLogs!!.size.toDouble()
    }

        val lastFindDate: Date
        get() {
            val allFinds = recentLogs.stream().filter { (logType) -> logType == FoundEnumType.Found }.toArray()
            val (_, logDate) = allFinds[allFinds.size - 1] as GeocacheLog
            return logDate
        }

    val lastLogDate: Date
        get() = recentLogs[0].logDate

*/
}
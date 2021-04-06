package net.teamtruta.tiaires.data

import androidx.room.Embedded
import androidx.room.Relation
import net.teamtruta.tiaires.GeoCacheAttributeEnum
import net.teamtruta.tiaires.VisitOutcomeEnum
import java.lang.Integer.min

class GeoCacheWithLogsAndAttributes(
        @Embedded val geoCache: GeoCache,
        @Relation(
                parentColumn = "id",
                entityColumn = "cacheDetailIDFK"
        )
        val recentLogs: List<GeoCacheLog>,

        @Relation(
                parentColumn = "id",
                entityColumn = "cacheDetailIDFK"
        )
        val attributes: List<GeoCacheAttribute>
){

        fun getLastNLogs(n: Int): List<GeoCacheLog>{
                return recentLogs.subList(0, min(n, recentLogs.size))
        }

        val dNFRisk: String
                get() {
                        if (geoCache.previousVisitOutcome == VisitOutcomeEnum.Disabled) return "DISABLED"
                        if (getLastNLogs(10).any {it.logType == VisitOutcomeEnum.DNF }) return "DNF RISK"
                        //if (recentLogs.subList(0, 10).any { it.logType == VisitOutcomeEnum.NeedsMaintenance } ||
                        if (attributes.contains(GeoCacheAttributeEnum.NeedsMaintenance)) return "NEEDS MAINTENANCE"
                        return ""
                }


        fun isDNFRisk(): Boolean {
                return dNFRisk != ""
        }

        fun setGeoCacheID(id: Long){
                geoCache.id = id
                recentLogs.forEach{ log -> log.cacheDetailIDFK = id}
                attributes.forEach{ at -> at.cacheDetailIDFK = id}
        }

}

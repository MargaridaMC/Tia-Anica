package net.teamtruta.tiaires.data.models

import androidx.room.Embedded
import androidx.room.Relation
import java.lang.Integer.min

class GeoCacheWithLogsAndAttributesAndWaypoints(
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
        val attributes: List<GeoCacheAttribute>,

        @Relation(
                parentColumn = "id",
                entityColumn = "cacheDetailIDFK"
        )
        val waypoints: List<Waypoint>
){

        fun getLastNLogs(n: Int): List<GeoCacheLog>{
                return recentLogs.subList(0, min(n, recentLogs.size))
        }

        val dNFRisk: String
                get() {
                        if (geoCache.previousVisitOutcome == VisitOutcomeEnum.Disabled) return "DISABLED"
                        if (getLastNLogs(10).any {it.logType == VisitOutcomeEnum.DNF }) return "DNF RISK"
                        if (this.attributes.filter{ it.attributeType == GeoCacheAttributeEnum.NeedsMaintenance }.any()) return "NEEDS MAINTENANCE"
                        return ""
                }


        fun isDNFRisk(): Boolean {
                return dNFRisk != ""
        }

}

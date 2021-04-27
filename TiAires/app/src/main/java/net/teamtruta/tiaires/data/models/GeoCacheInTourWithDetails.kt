package net.teamtruta.tiaires.data.models

import androidx.room.Embedded
import androidx.room.Relation

class GeoCacheInTourWithDetails (
        @Embedded val geoCacheInTour: GeoCacheInTour,
        @Relation(
                parentColumn = "geoCacheDetailIDFK",
                entityColumn = "id",
                entity = GeoCache::class
        )
        val geoCache: GeoCacheWithLogsAndAttributesAndWaypoints
        ){
        constructor(geoCache: GeoCache, tourID: Long):
                this(GeoCacheInTour(geoCacheDetailIDFK = geoCache.id, tourIDFK = tourID),
                        geoCache = GeoCacheWithLogsAndAttributesAndWaypoints(geoCache, listOf(), listOf(), listOf())){
                        this.geoCacheInTour.currentVisitOutcome = geoCache.previousVisitOutcome
                }

}
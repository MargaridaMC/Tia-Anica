package net.teamtruta.tiaires.data

import androidx.room.Embedded
import androidx.room.Relation

class GeoCacheInTourWithDetails (
        @Embedded val geoCacheInTour: GeoCacheInTour,
        @Relation(
                parentColumn = "geoCacheDetailIDFK",
                entityColumn = "id",
                entity = GeoCache::class
        )
        val geoCache: GeoCacheWithLogsAndAttributes
        ){
        constructor(geoCache: GeoCache, tourID: Long):
                this(GeoCacheInTour(geoCacheDetailIDFK = geoCache.id, tourIDFK = tourID),
                        geoCache = GeoCacheWithLogsAndAttributes(geoCache, listOf(), listOf())){
                        this.geoCacheInTour.currentVisitOutcome = geoCache.previousVisitOutcome
                }

}
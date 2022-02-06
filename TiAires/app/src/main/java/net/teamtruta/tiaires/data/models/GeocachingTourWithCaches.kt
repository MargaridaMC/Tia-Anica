package net.teamtruta.tiaires.data.models

import androidx.room.Embedded
import androidx.room.Relation

class GeocachingTourWithCaches(
        @Embedded val tour: GeocachingTour,
        @Relation(
                parentColumn = "id",
                entityColumn = "tourIDFK",
                entity = GeoCacheInTour::class
        )
        var tourGeoCaches: MutableList<GeoCacheInTourWithDetails>){

        init {
                tourGeoCaches.sortBy { it.geoCacheInTour.orderIdx}
        }

        constructor(name: String): this(GeocachingTour(name), mutableListOf<GeoCacheInTourWithDetails>())

        fun getSize(): Int{
                return tourGeoCaches.size
        }

        fun getNumFound(): Int{
                return tourGeoCaches.filter { x -> x.geoCacheInTour.currentVisitOutcome ==  VisitOutcomeEnum.Found}.count()
        }

        fun getNumDNF():Int{
                return tourGeoCaches.filter { x -> x.geoCacheInTour.currentVisitOutcome ==  VisitOutcomeEnum.DNF}.count()
        }


        fun getLastVisitedGeoCache(): Int{

                val lastVisitedGeoCache = tourGeoCaches
                        .indexOfLast { it.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.Found ||
                        it.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.DNF}

                return lastVisitedGeoCache
        }

        fun getTourGeoCacheCodes(): List<String>{
                return tourGeoCaches.map { x -> x.geoCache.geoCache.code }.toList()
        }

}
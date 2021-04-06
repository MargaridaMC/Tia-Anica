package net.teamtruta.tiaires.data

import androidx.room.Embedded
import androidx.room.Relation
import net.teamtruta.tiaires.VisitOutcomeEnum

class GeocachingTourWithCaches(
        @Embedded val tour: GeocachingTour,
        @Relation(
                parentColumn = "id",
                entityColumn = "tourIDFK",
                entity = GeoCacheInTour::class
        )
        val tourGeoCaches: MutableList<GeoCacheInTourWithDetails>){

        constructor(name: String): this(GeocachingTour(name), mutableListOf<GeoCacheInTourWithDetails>())

        fun getSize(): Int{
                return tourGeoCaches.size
        }

        fun getNumFound():Int{
                return tourGeoCaches.filter { x -> x.geoCacheInTour.currentVisitOutcome ==  VisitOutcomeEnum.Found}.count()
        }

        fun getNumDNF():Int{
                return tourGeoCaches.filter { x -> x.geoCacheInTour.currentVisitOutcome ==  VisitOutcomeEnum.DNF}.count()
        }


        fun getLastVisitedGeoCache(): Int{

                val lastVisitedGeoCache = tourGeoCaches
                        .indexOfFirst { x -> x.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.Found ||
                        x.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.DNF}


                if(lastVisitedGeoCache == -1){
                        return 0
                }

                return lastVisitedGeoCache
        }

        fun getTourGeoCacheCodes(): List<String>{
                return tourGeoCaches.map { x -> x.geoCache.geoCache.code }.toList()
        }

}
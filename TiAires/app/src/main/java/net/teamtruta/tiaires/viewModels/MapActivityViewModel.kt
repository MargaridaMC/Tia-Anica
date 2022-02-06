package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.repositories.Repository

class MapActivityViewModel(private val repository: Repository) : ViewModel(){

    val currentTour: LiveData<GeocachingTourWithCaches> = repository.getCurrentTour()

    fun updateTour(tour: GeocachingTourWithCaches) {
        GlobalScope.launch {
            repository.updateGeocachingTour(tour)
        }
    }

    fun computeTourFullDistance(): Double{
        return computeTourDistance(-1)
    }

    fun computeTourRemainingDistance():Double{
        return if(currentTour.value == null){
            0.0
        } else {
            computeTourDistance(currentTour.value!!.getLastVisitedGeoCache())
        }
    }

    private fun computeTourDistance(startGeoCacheIDX: Int): Double {
        if(currentTour.value == null){
            return 0.0
        }

        val tour = currentTour.value!!
        val tourGeoCacheCoordinates = tour.tourGeoCaches.map {
                gcit -> Point.fromLngLat(gcit.geoCache.geoCache.longitude.value,
            gcit.geoCache.geoCache.latitude.value)
        }

        var startIDX = startGeoCacheIDX

        var distance = 0.0
        if (startIDX == -1) {
            startIDX = 1

            // If there is a starting point it will be the first element in route coordinates
            if (tour.tour.startingPointLatitude != null) {
                distance = TurfMeasurement.distance(tourGeoCacheCoordinates[0]!!,
                    Point.fromLngLat(tour.tour.startingPointLongitude!!.value, tour.tour.startingPointLatitude
                        !!.value))
            }
        }
        else {
            startIDX += 1
        }

        for (i in startIDX until tourGeoCacheCoordinates.size) {
            distance += TurfMeasurement.distance(tourGeoCacheCoordinates[i]!!,
                    tourGeoCacheCoordinates[i - 1]!!)
        }
        return distance
    }

}

class MapActivityViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapActivityViewModel::class.java)){
            return MapActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}
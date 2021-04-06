package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.GeocachingTour
import net.teamtruta.tiaires.data.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.Repository

class TourCreationViewModel(private val repository: Repository) : ViewModel(){

    val gettingTour: MutableLiveData<Boolean?> = repository.gettingTour

    val currentTour = repository.getCurrentTour()

    fun setGeoCachesInExistingTour(geoCacheList: List<String>, tour: GeocachingTourWithCaches){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            setTourGeocaches(geoCacheList, tour)
        }
    }

    fun setTourGeocaches(geoCacheList: List<String>, tour: GeocachingTourWithCaches){

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            // 1. Remove any repeated caches
            val geoCachesToGet = geoCacheList.distinct().toList()

            val geoCacheOrder = geoCachesToGet.withIndex().associate { Pair(it.value, (it.index + 1)*1000)  }

            // 2. Process the deltas from the old list to the new list
            // 2.1 Remove from the original tour caches that are not in the new one
            val geoCachesAlreadyInTour = tour.getTourGeoCacheCodes()
            geoCachesAlreadyInTour.forEach{code -> if(!geoCacheList.contains(code))
                GlobalScope.launch { repository.dropGeoCacheFromTour(code, tour) }}

            // 2.2 Remove from the list of caches to fetch, those we already have loaded
            var newGeoCachesInTourCodes = geoCachesToGet
            newGeoCachesInTourCodes = newGeoCachesInTourCodes.filter { code -> !geoCachesAlreadyInTour.contains(code) }.toList()


            repository.getGeoCaches(newGeoCachesInTourCodes, geoCacheOrder, tour.tour.id)
        }

    }

    fun updateGeoCachingTour(geocachingTourWithCaches: GeocachingTourWithCaches){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.updateGeocachingTour(geocachingTourWithCaches)
        }
    }

    fun createNewTourWithCaches(tourName: String, geoCacheCodesList: MutableList<String>) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val tour = GeocachingTour(tourName)
            val tourID = repository.addNewTour(tour)
            tour.id = tourID

            val tourWithGeoCaches = GeocachingTourWithCaches(tour, mutableListOf())
            setTourGeocaches(geoCacheCodesList, tourWithGeoCaches)

        }

    }

}

class TourCreationViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TourCreationViewModel::class.java)){
            return TourCreationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}
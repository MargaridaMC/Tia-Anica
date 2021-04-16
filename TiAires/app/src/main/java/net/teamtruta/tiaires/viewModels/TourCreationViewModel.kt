package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.models.GeocachingTour
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.repositories.Repository

class TourCreationViewModel(private val repository: Repository) : ViewModel(){

    val gettingTour: MutableLiveData<Boolean?> = repository.gettingTour

    val currentTour = repository.getCurrentTour()

    fun setGeoCachesInExistingTour(geoCacheList: List<String>, tour: GeocachingTourWithCaches){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            setTourGeocaches(geoCacheList, tour)
        }
    }

    private fun setTourGeocaches(geoCacheList: List<String>, tour: GeocachingTourWithCaches){

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.setTourCacheList(geoCacheList, tour)
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
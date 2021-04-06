package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.*
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.GeoCacheInTour
import net.teamtruta.tiaires.data.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.Repository

class TourViewModel(private val repository: Repository) : ViewModel(){

    fun updateGeoCacheInTour(gcit: GeoCacheInTour){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            repository.updateGeoCacheInTour(gcit)
        }
    }

    fun getCurrentTour(): LiveData<GeocachingTourWithCaches> = repository.getCurrentTour()
    fun setCurrentTour(tourID: Long) = repository.setCurrentTourID(tourID)

    fun deleteTour(tour: GeocachingTourWithCaches) {
        GlobalScope.launch { repository.deleteTour(tour) } }

    fun getGeoCacheInTourFromID(geoCacheInTourID: Long): LiveData<GeoCacheInTourWithDetails> =
            repository.getGeoCacheInTourFromID(geoCacheInTourID)



}

class TourViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TourViewModel::class.java)){
            return TourViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}
package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.*
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.models.GeoCacheInTour
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum
import net.teamtruta.tiaires.data.repositories.Repository
import net.teamtruta.tiaires.extensions.Event
import net.teamtruta.tiaires.extensions.Resource

class TourViewModel(private val repository: Repository) : ViewModel(){

    val gettingTour: MutableLiveData<Boolean?> = repository.gettingTour

    private val _draftUploadResult = MutableLiveData<Event<Resource<Boolean>>> ()
    val draftUploadResult: LiveData<Event<Resource<Boolean>>>
        get() = _draftUploadResult

    fun updateGeoCacheInTour(gcit: GeoCacheInTour){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            repository.updateGeoCacheInTour(gcit)
        }
    }

    fun getCurrentTour(): LiveData<GeocachingTourWithCaches> = repository.getCurrentTour()

    fun deleteTour(tour: GeocachingTourWithCaches) {
        GlobalScope.launch { repository.deleteTour(tour) } }


    fun refreshTourGeoCacheDetails(tour: GeocachingTourWithCaches){
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.refreshTourGeoCacheDetails(tour)
        }
    }

    fun uploadTourDrafts(_tour: GeocachingTourWithCaches?) {

        // Get list of caches that have already been visited (but that have not been logged before)
        val visitedGeoCaches = _tour?.tourGeoCaches
                ?.filter { x ->  (x.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.DNF &&
                        x.geoCache.geoCache.previousVisitOutcome != VisitOutcomeEnum.DNF)
                        || (x.geoCacheInTour.currentVisitOutcome == VisitOutcomeEnum.Found &&
                        x.geoCache.geoCache.previousVisitOutcome != VisitOutcomeEnum.Found)}
                ?.filter { x -> !x.geoCacheInTour.draftUploaded }
                ?.toList()

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (visitedGeoCaches != null) {
                _draftUploadResult.postValue(Event(repository.uploadDrafts(visitedGeoCaches)))
            }
        }

    }

    fun reorderTourCaches(geoCacheInTourList: List<GeoCacheInTourWithDetails?>) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            geoCacheInTourList.forEachIndexed { index, geoCacheInTourWithDetails ->
                if(geoCacheInTourWithDetails != null &&
                        geoCacheInTourWithDetails.geoCacheInTour.orderIdx != index) {
                    geoCacheInTourWithDetails.geoCacheInTour.orderIdx = index
                    repository.updateGeoCacheInTour(geoCacheInTourWithDetails.geoCacheInTour)
                }
            } }
        }

    }


class TourViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TourViewModel::class.java)){
            return TourViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}
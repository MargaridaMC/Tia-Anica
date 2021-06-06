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

    private val _draftUploadResult = MutableLiveData<Event<Any>> ()
    val draftUploadResult: LiveData<Event<Any>>
        get() = _draftUploadResult

    val geoCachesBeingObtained: MutableMap<String, MutableLiveData<Event<Any>?>> = mutableMapOf()

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
                _draftUploadResult.postValue(repository.uploadDrafts(visitedGeoCaches))
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

    fun addNewGeoCacheToTour(_tour: GeocachingTourWithCaches,  newGeoCacheCode: String) {

        geoCachesBeingObtained[newGeoCacheCode] = MutableLiveData(null)

        // Check if the geocache already exists in the tour
        if(newGeoCacheCode in _tour.getTourGeoCacheCodes()){
            geoCachesBeingObtained[newGeoCacheCode]?.value = Event(false, "The geocache with code $newGeoCacheCode already exists in the tour. Check if you specified the correct code.")
            return
        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            geoCachesBeingObtained[newGeoCacheCode]?.postValue(repository.addNewGeoCacheToTour(_tour, newGeoCacheCode))
        }
    }

    fun removeGeoCacheFromTour(geoCacheInTour: GeoCacheInTourWithDetails) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.removeGeoCacheFromTour(geoCacheInTour.geoCacheInTour)
        }
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
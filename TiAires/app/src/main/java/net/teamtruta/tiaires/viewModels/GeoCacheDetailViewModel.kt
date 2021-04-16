package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.teamtruta.tiaires.data.models.GeoCacheInTour
import net.teamtruta.tiaires.data.models.GeoCacheInTourWithDetails
import net.teamtruta.tiaires.data.models.VisitOutcomeEnum
import net.teamtruta.tiaires.data.repositories.Repository
import java.time.Instant

class GeoCacheDetailViewModel (private val repository: Repository) : ViewModel(){

    fun getGeoCacheInTourFromID(geoCacheInTourID: Long): LiveData<GeoCacheInTourWithDetails> =
            repository.getGeoCacheInTourFromID(geoCacheInTourID)

    fun updateGeoCacheInTour(gcit: GeoCacheInTour){
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            repository.updateGeoCacheInTour(gcit)
        }
    }


    fun setGeoCacheInTourVisit(currentGeoCacheInTour: GeoCacheInTour, visit: VisitOutcomeEnum) {

        if(currentGeoCacheInTour.currentVisitOutcome != visit) currentGeoCacheInTour.draftUploaded = false

        currentGeoCacheInTour.currentVisitOutcome = visit

        when(visit){
            VisitOutcomeEnum.NotAttempted ->  currentGeoCacheInTour.currentVisitDatetime = null
            VisitOutcomeEnum.Found ->  currentGeoCacheInTour.currentVisitDatetime = Instant.now()
            VisitOutcomeEnum.DNF ->  currentGeoCacheInTour.currentVisitDatetime = Instant.now()
            else -> {}
        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.updateGeoCacheInTour(currentGeoCacheInTour)
        }
    }

    fun updateGeocaCheInTourDetails(currentGeoCacheInTour: GeoCacheInTour,
                                    needsMaintenance: Boolean,
                                     favouritePoint: Boolean,
                                     foundTrackable: String,
                                     droppedTrackable: String,
                                     notes: String) {

        currentGeoCacheInTour.needsMaintenance = needsMaintenance
        currentGeoCacheInTour.favouritePoint = favouritePoint

        currentGeoCacheInTour.foundTrackable = if(foundTrackable == "") null else foundTrackable
        currentGeoCacheInTour.droppedTrackable = if(droppedTrackable == "") null else droppedTrackable

        currentGeoCacheInTour.notes = notes

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.updateGeoCacheInTour(currentGeoCacheInTour)
        }

    }
}

class GeoCacheDetailViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeoCacheDetailViewModel::class.java)){
            return GeoCacheDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}
package net.teamtruta.tiaires.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import net.teamtruta.tiaires.data.models.*
import net.teamtruta.tiaires.data.repositories.Repository
import net.teamtruta.tiaires.extensions.Event
import java.time.Instant

class GeoCacheDetailViewModel (private val repository: Repository) : ViewModel(){

    private val _waypointAdditionSuccessful = MutableLiveData<Event<String>>()
    val waypointAdditionSuccessful: LiveData<Event<String>>
        get() = _waypointAdditionSuccessful

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

    fun addNewWaypointToGeoCache(waypointName: String, waypointCoordinates: String,
                                 waypointNotes: String,
                                 geoCacheInTourID: Long) {

        val coordinatePair = Coordinate.fromFullCoordinates(waypointCoordinates)
        if(coordinatePair == null){
            _waypointAdditionSuccessful.postValue(Event(false, "Could not create new waypoint. Please check the coordinate format."))

        } else {
            val (latitude, longitude) = coordinatePair
            val newWaypoint = Waypoint(waypointName, latitude, longitude,
                    waypointState = Waypoint.WAYPOINT_NOT_ATTEMPTED, isParking = false, notes = waypointNotes)

            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                repository.addNewWaypointToGeoCache(newWaypoint, geoCacheInTourID)
            }
            _waypointAdditionSuccessful.postValue(Event(false, "Successfully created new waypoint!"))
        }




    }

    fun onWaypointDone(waypoint: Waypoint, waypointState: Int) {
        waypoint.waypointState = waypointState
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.updateWaypoint(waypoint)
        }
    }

    fun deleteWaypoint(waypoint: Waypoint) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.deleteWaypoint(waypoint)
        }

    }

    fun updateWaypoint(waypoint: Waypoint, name: String, coordinateString: String, notes: String) {
        waypoint.name = name
        val coordinatePair = Coordinate.fromFullCoordinates(coordinateString)
        if (coordinatePair != null) {
            waypoint.latitude = coordinatePair.first
            waypoint.longitude = coordinatePair.second
        } else {
            waypoint.latitude = null
            waypoint.longitude = null
        }
        waypoint.notes = notes
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            repository.updateWaypoint(waypoint)
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
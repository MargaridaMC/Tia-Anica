package net.teamtruta.tiaires.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.teamtruta.tiaires.App
import net.teamtruta.tiaires.GeocachingScrapper
import net.teamtruta.tiaires.GeocachingScrappingTask


class Repository(private val tourDao: GeocachingTourDao,
                 private val geoCacheInTourDao: GeoCacheInTourDao,
                private val geoCacheDao: GeoCacheDao,
                private val geoCacheLogDao: GeoCacheLogDao,
                private val geoCacheAttributeDao: GeoCacheAttributeDao){

    val gettingTour: MutableLiveData<Boolean?> = MutableLiveData(null)
    var allTours: LiveData<List<GeocachingTourWithCaches>> = tourDao.getAllTours()
    private var _currentTourID: Long = 0

    fun setCurrentTourID(tourID: Long){
        _currentTourID = tourID
    }

    fun getCurrentTour(): LiveData<GeocachingTourWithCaches> {
        return  tourDao.getGeocachingTourFromID(_currentTourID)
    }

    suspend fun deleteTour(tour: GeocachingTourWithCaches) {
        tourDao.deleteTour(tour.tour)
    }



    fun addNewGeoCacheInTour(geoCacheInTour: GeoCacheInTour){
        geoCacheInTourDao.insert(geoCacheInTour)
    }
    fun updateGeoCacheInTour(geoCacheInTour: GeoCacheInTour){
        geoCacheInTourDao.updateGeoCacheInTour(geoCacheInTour)
    }

    fun getGeoCacheInTourFromID(geoCacheInTourID: Long): LiveData<GeoCacheInTourWithDetails> {
        return geoCacheInTourDao.getGeoCacheInTourFromID(geoCacheInTourID)
    }

    suspend fun updateGeocachingTour(geocachingTourWithCaches: GeocachingTourWithCaches) {
        tourDao.updateTour(geocachingTourWithCaches.tour)
    }

    fun dropGeoCacheFromTour(code: String, tour: GeocachingTourWithCaches) {
        geoCacheInTourDao.dropGeoCacheFromTour(code, tour.tour.id)
    }

    fun getGeoCaches(geoCachesToGet: List<String>, geoCacheOrder:Map<String, Int>, tourID: Long) {
        gettingTour.postValue(true)
        val requestedGeoCacheCodes = mutableListOf<String>()
        geoCachesToGet.forEach{ code -> //check if the cache exists in the database´
            Log.d("REPOSITORY", "Looking for cache with code $code")
            val geoCacheID = geoCacheDao.getGeoCacheIDFromCode(code)
            if(geoCacheID != 0L){
                val geoCacheInTour = GeoCacheInTour(geoCacheDetailIDFK = geoCacheID, tourIDFK = tourID)
                geoCacheInTour.orderIdx = geoCacheOrder[code]!!
                geoCacheInTourDao.insert(geoCacheInTour)
            } else {
                requestedGeoCacheCodes.add(code)
            }

        }

        // fetch data using geocaching scrapper
        if (requestedGeoCacheCodes.size > 0) {
            val authCookie = App.authenticationCookie
            val scrapper = GeocachingScrapper(authCookie)
            val geocachingScrappingTask = GeocachingScrappingTask(scrapper, requestedGeoCacheCodes,
                    this, tourID, geoCacheOrder, null)
            geocachingScrappingTask.execute()
        } else {
            // We're done with creating the tour
            gettingTour.postValue(false)
            if(tourID != _currentTourID) _currentTourID = tourID
        }

    }

    fun refreshTourGeoCacheDetails(tour: GeocachingTourWithCaches){
        val geocachesToGet = tour.tourGeoCaches.map{
             x -> x.geoCache.geoCache.code
        }

        val geocacheIDs = tour.tourGeoCaches.map{
            x -> x.geoCache.geoCache.id
        }

        val authCookie = App.authenticationCookie
        val scrapper = GeocachingScrapper(authCookie)
        val geocachingScrappingTask = GeocachingScrappingTask(scrapper, geocachesToGet,
                this, null, null, geocacheIDs)
        geocachingScrappingTask.execute()
    }


    fun onGeoCachesObtained(obtainedGeoCachesWithLogsAndAttributes: MutableList<GeoCacheWithLogsAndAttributes>, tourID:Long,
                            geoCacheOrder: Map<String, Int>, geoCacheIDs: List<Long>?  ) {

          val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {

            if(geoCacheIDs == null){
                // These caches were just obtained and just need to be store in the database

                val obtainedGeoCaches = obtainedGeoCachesWithLogsAndAttributes.map { x -> x.geoCache }
                val obtainedGeoCacheIDs = geoCacheDao.insert(*obtainedGeoCaches.toTypedArray())
                obtainedGeoCaches.forEachIndexed { i, geoCache ->
                    val codeID = obtainedGeoCacheIDs[i]

                    geoCache.id = codeID

                    // Save logs
                    val geoCacheLogs = obtainedGeoCachesWithLogsAndAttributes[i].recentLogs
                    geoCacheLogs.forEach{log ->
                        log.cacheDetailIDFK = codeID
                        geoCacheLogDao.insert(log)}

                    // Save attributes
                    val geoCacheAttributes = obtainedGeoCachesWithLogsAndAttributes[i].attributes
                    geoCacheAttributes.forEach{attribute ->
                        attribute.cacheDetailIDFK = codeID
                        geoCacheAttributeDao.insert(attribute)}

                    val geoCacheInTour = GeoCacheInTourWithDetails(geoCache, tourID)
                    geoCacheInTour.geoCacheInTour.orderIdx = geoCacheOrder[geoCache.code]!!
                    addNewGeoCacheInTour(geoCacheInTour.geoCacheInTour)
                }
            } else {
                // We are refreshing the caches so we need to replace both the cache details as well as the logs and attributes
                obtainedGeoCachesWithLogsAndAttributes.forEachIndexed {
                    i, geoCacheWithLogsAndAttributes ->
                        val geoCacheID = geoCacheIDs[i]

                        // Update geocache
                        val geoCache = geoCacheWithLogsAndAttributes.geoCache
                        geoCache.id = geoCacheID
                        geoCacheDao.update(geoCache)

                        // Update logs
                        geoCacheLogDao.deleteLogsInGeoCache(geoCacheID)
                        val logs: List<GeoCacheLog> = geoCacheWithLogsAndAttributes.recentLogs
                        logs.map {it.apply { cacheDetailIDFK =  geoCacheID}}
                        geoCacheLogDao.insert(*logs.toTypedArray())

                        // Update attributes
                        geoCacheAttributeDao.deleteAttributesInGeoCache(geoCacheID)
                        val attributes: List<GeoCacheAttribute> = geoCacheWithLogsAndAttributes.attributes
                        attributes.map {it.apply { cacheDetailIDFK =  geoCacheID}}
                        geoCacheAttributeDao.insert(*attributes.toTypedArray())

                }

            }

            _currentTourID = tourID
            gettingTour.postValue(false)
        }


    }

    fun addNewTour(tour: GeocachingTour):Long {
        return tourDao.addNewTour(tour)
    }

    fun deleteAllGeoCachesNotBeingUsed() {
        geoCacheDao.deleteAllGeoCachesNotBeingUsed()
    }

    fun getNTours() : Int{
        return if(allTours.value == null) 0
        else allTours.value!!.size
    }

}
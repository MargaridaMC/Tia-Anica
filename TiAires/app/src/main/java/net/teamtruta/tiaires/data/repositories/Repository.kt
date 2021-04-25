package net.teamtruta.tiaires.data.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.teamtruta.tiaires.*
import net.teamtruta.tiaires.data.daos.*
import net.teamtruta.tiaires.data.models.*
import net.teamtruta.tiaires.extensions.Resource
import net.teamtruta.tiaires.extensions.Status
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant


class Repository(private val tourDao: GeocachingTourDao,
                 private val geoCacheInTourDao: GeoCacheInTourDao,
                 private val geoCacheDao: GeoCacheDao,
                 private val geoCacheLogDao: GeoCacheLogDao,
                 private val geoCacheAttributeDao: GeoCacheAttributeDao){

    private val TAG = Repository::class.java.simpleName
    private val DRAFT_FILE_PATH = "drafts.txt"
    private val USERNAME = "username"

    val gettingTour: MutableLiveData<Boolean?> = MutableLiveData(null)
    var allTours: LiveData<List<GeocachingTourWithCaches>> = tourDao.getAllTours()
    private var _currentTourID: Long = 0

    private val groundspeakRepository = GroundspeakRepository()

    fun getAuthenticationCookie() : String? {

        val context = App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Application.MODE_PRIVATE)
        return sharedPreferences?.getString(context.getString(R.string.authentication_cookie_key), "")
    }

    fun setAuthenticationCookie(authCookie: String){
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Application.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(context.getString(R.string.authentication_cookie_key), authCookie)
        editor?.apply()
    }

    fun getUsername(): String?{
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Application.MODE_PRIVATE)
        return sharedPreferences.getString(USERNAME, "")
    }

    fun setUsername(username: String){
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                Application.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USERNAME, username)
        editor.apply()
    }

    fun setCurrentTourID(tourID: Long){
        _currentTourID = tourID
    }

    fun getCurrentTour(): LiveData<GeocachingTourWithCaches> {
        return  tourDao.getGeocachingTourFromID(_currentTourID)
    }

    suspend fun deleteTour(tour: GeocachingTourWithCaches) {
        tourDao.deleteTour(tour.tour)
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

    private fun dropGeoCacheFromTour(code: String, tour: GeocachingTourWithCaches) {
        geoCacheInTourDao.dropGeoCacheFromTour(code, tour.tour.id)
    }

    suspend fun setTourCacheList(geoCacheList: List<String>, tour: GeocachingTourWithCaches) {

        gettingTour.postValue(true)

        // 1. Remove any repeated caches
        val geoCachesToGet = geoCacheList.distinct().toList()

        // 2. Process the deltas from the old list to the new list
        // 2.1 Remove from the original tour caches that are not in the new one
        val geoCachesAlreadyInTour = tour.getTourGeoCacheCodes()
        geoCachesAlreadyInTour.forEach{code -> if(!geoCacheList.contains(code))
            GlobalScope.launch { dropGeoCacheFromTour(code, tour) }}

        geoCachesToGet.forEachIndexed {
            index, geoCacheCode ->

            // If this cache is already in tour simply set it to this position
            if(geoCachesAlreadyInTour.contains(geoCacheCode)){
                val geoCacheInTourWithGivenCode: GeoCacheInTour = tour.tourGeoCaches.filter {
                    gcit -> gcit.geoCache.geoCache.code == geoCacheCode
                }[0].geoCacheInTour

                geoCacheInTourWithGivenCode.orderIdx = index
                geoCacheInTourDao.updateGeoCacheInTour(geoCacheInTourWithGivenCode)
            } else {
                // If it's not in the tour we need to fetch the cache details first and then save it
                val newGeoCacheID = getGeoCacheIDFromCode(geoCacheCode)

                if(newGeoCacheID == -1L){
                    //TODO Something went wrong
                }

                // Save GeoCacheInTour
                val newGeoCacheInTour = GeoCacheInTour(geoCacheDetailIDFK = newGeoCacheID,
                        tourIDFK = tour.tour.id)
                newGeoCacheInTour.orderIdx = index
                geoCacheInTourDao.insert(newGeoCacheInTour)
            }

        }

        val tourID = tour.tour.id
        if(tourID != _currentTourID) _currentTourID = tourID

        gettingTour.postValue(false)
    }

    private suspend fun getGeoCacheIDFromCode(geocacheCode: String) : Long{
        val geoCacheID = geoCacheDao.getGeoCacheIDFromCode(geocacheCode)
        if(geoCacheID != 0L){
            return geoCacheID
        } else {
            val obtainedGeoCacheWithLogsAndAttributes = groundspeakRepository.getGeoCacheFromCode(geocacheCode)
            if(obtainedGeoCacheWithLogsAndAttributes!=null){

                // Save GeoCache Details
                val newGeoCacheID = geoCacheDao.insert(obtainedGeoCacheWithLogsAndAttributes.geoCache)

                // Save logs
                val geoCacheLogs = obtainedGeoCacheWithLogsAndAttributes.recentLogs
                geoCacheLogs.forEach{ log ->
                    log.cacheDetailIDFK = newGeoCacheID
                    geoCacheLogDao.insert(log)}

                // Save attributes
                val geoCacheAttributes = obtainedGeoCacheWithLogsAndAttributes.attributes
                geoCacheAttributes.forEach{ attribute ->
                    attribute.cacheDetailIDFK = newGeoCacheID
                    geoCacheAttributeDao.insert(attribute)}

                return newGeoCacheID
            }

            return -1L
        }

    }

    suspend fun refreshTourGeoCacheDetails(tour: GeocachingTourWithCaches){
        gettingTour.postValue(true)
        val geoCacheCodesToGet = tour.tourGeoCaches.map{ x -> x.geoCache.geoCache.code
        }

        val geoCacheIDs = tour.tourGeoCaches.map{ x -> x.geoCache.geoCache.id
        }

        geoCacheCodesToGet.forEachIndexed{
            index, geoCacheCode ->

            val newlyObtainedGeocache = groundspeakRepository.getGeoCacheFromCode(geoCacheCode)
            if(newlyObtainedGeocache != null){

                val id = geoCacheIDs[index]

                // Update GeoCache Details
                newlyObtainedGeocache.geoCache.id = id
                geoCacheDao.update(newlyObtainedGeocache.geoCache)

                // Update logs
                val geoCacheLogs = newlyObtainedGeocache.recentLogs
                geoCacheLogs.forEach{ log ->
                    log.cacheDetailIDFK = id
                    geoCacheLogDao.insert(log)}

                // Save attributes
                val geoCacheAttributes = newlyObtainedGeocache.attributes
                geoCacheAttributes.forEach{ attribute ->
                    attribute.cacheDetailIDFK = id
                    geoCacheAttributeDao.insert(attribute)}

            }

        }
        gettingTour.postValue(false)

    }

    fun addNewTour(tour: GeocachingTour):Long {
        return tourDao.addNewTour(tour)
    }

    fun deleteAllGeoCachesNotBeingUsed() {
        geoCacheDao.deleteAllGeoCachesNotBeingUsed()
    }

    fun uploadDrafts(visitedGeoCaches: List<GeoCacheInTourWithDetails>): Resource<Boolean> {

        val successfullyWroteDraftsToFile = createDraftFile(visitedGeoCaches)
        if(!successfullyWroteDraftsToFile)
            return Resource(Status.ERROR, null, "There was an error in writing your drafts to file.")

        val draftFileAbsolutePath = App.applicationContext().filesDir.toString() + "/" + DRAFT_FILE_PATH
        val authenticationCookie = getAuthenticationCookie()
        if(authenticationCookie == null || authenticationCookie == "")
            return Resource(Status.ERROR, null, "There was an issue with your authentication cookie. Please re-authenticate.")


        val result = groundspeakRepository.uploadDrafts(draftFileAbsolutePath)
        if(result.status == Status.SUCCESS){
            visitedGeoCaches.forEach{gcit ->
                gcit.geoCacheInTour.draftUploaded = true
                updateGeoCacheInTour(gcit.geoCacheInTour)}
        }

        return result
    }

    private fun createDraftFile(geoCachesToUpload: List<GeoCacheInTourWithDetails>): Boolean {
        val stringBuilder = StringBuilder()
        for (gcit in geoCachesToUpload) {
            val geoCacheInTour = gcit.geoCacheInTour
            stringBuilder.append(gcit.geoCache.geoCache.code)
            stringBuilder.append(",")
            if (geoCacheInTour.currentVisitDatetime != null) {
                stringBuilder.append(geoCacheInTour.currentVisitDatetime.toString())
            } else {
                stringBuilder.append(Instant.now().toString())
            }
            stringBuilder.append(",")
            stringBuilder.append(geoCacheInTour.currentVisitOutcome.visitOutcomeString)
            stringBuilder.append(",")
            stringBuilder.append("\"").append(geoCacheInTour.notes).append("\"")
            stringBuilder.append("\n")
        }
        val content = stringBuilder.toString()
        return try {
            val outputStream: FileOutputStream = App.applicationContext().openFileOutput(DRAFT_FILE_PATH, Context.MODE_PRIVATE)
            outputStream.write(content.toByteArray())
            outputStream.close()
            true
        } catch (e: IOException) {
            Log.e(TAG, "File write failed: $e")
            false
        }
    }

    fun login(username: String, password: String): Boolean{
        return groundspeakRepository.login(username, password)
    }

    fun logout(): Boolean {
        val context =  App.applicationContext()
        val sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(USERNAME)
        editor.remove(context.getString(R.string.authentication_cookie_key))
        editor.apply()
        return true
    }



}
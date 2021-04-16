package net.teamtruta.tiaires.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import net.teamtruta.tiaires.data.models.GeocachingTourWithCaches
import net.teamtruta.tiaires.data.models.GeocachingTour

@Dao
interface GeocachingTourDao {

    @Insert
    fun addNewTour(tour: GeocachingTour): Long

    @Transaction
    @Query("SELECT * FROM tour WHERE id = :tourID")
    fun getGeocachingTourFromID(tourID: Long): LiveData<GeocachingTourWithCaches>

    @Query("SELECT name FROM tour WHERE id =:tourID")
    fun getTourName(tourID: Long): String

    // TODO getTourSize, getNumFound, getNumDNF, getTourGeoCacheCodes

    @Transaction
    @Query("SELECT * FROM tour")
    fun getAllTours(): LiveData<List<GeocachingTourWithCaches>>

    // TODO removeFromTour, addToTour

    @Update
    suspend fun updateTour(geocachingTour: GeocachingTour)

    @Delete
    suspend fun deleteTour(geocachingTour: GeocachingTour)

    @Query("SELECT id FROM tour WHERE name = :name LIMIT 1")
    fun getTourIDFromName(name: String): Long


}
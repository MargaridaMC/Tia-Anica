package net.teamtruta.tiaires.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GeoCacheInTourDao {

    @Insert
    fun insert(geoCacheInTour: GeoCacheInTour)

    @Transaction
    @Query("SELECT * FROM cache WHERE id = :geoCacheID")
    fun getGeoCacheInTourFromID(geoCacheID: Long): LiveData<GeoCacheInTourWithDetails>

    @Transaction
    @Query("SELECT * FROM cache WHERE tourIDFK = :tourID")
    fun getAllGeoCachesInTour(tourID: Long): List<GeoCacheInTourWithDetails>

    @Query("SELECT COUNT(*) FROM cache WHERE tourIDFK = :tourID")
    fun getTourSize(tourID: Long): Int

    // TODO I think this should go in the tour dao
    @Query("SELECT COUNT(*) FROM cache WHERE tourIDFK = :tourID AND currentVisitOutcome = :visitType")
    fun getNumberVisitTypeInTour(tourID: Long, visitType: String):Int

    @Update
    fun updateGeoCacheInTour(geoCacheInTour: GeoCacheInTour)

    @Query("DELETE FROM cache WHERE cache.geoCacheDetailIDFK IN " +
            "(SELECT cacheDetail.ID FROM cache LEFT JOIN cacheDetail " +
            "ON cache.geoCacheDetailIDFK = cacheDetail.ID WHERE cacheDetail.code = :code) " +
            "AND cache.tourIDFK IN (SELECT tour.id FROM cache LEFT JOIN tour " +
            "ON cache.tourIDFK = tour.id WHERE tour.id=:tourID)")
    fun dropGeoCacheFromTour(code: String, tourID: Long)

}
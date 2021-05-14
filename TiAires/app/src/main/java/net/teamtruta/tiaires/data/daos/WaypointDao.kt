package net.teamtruta.tiaires.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import net.teamtruta.tiaires.data.models.Waypoint

@Dao
interface WaypointDao {

    @Insert
    fun insert(waypoint: Waypoint)

    @Insert
    fun insert(vararg waypoint: Waypoint)

    @Query("DELETE FROM waypoint WHERE cacheDetailIDFK = :geoCacheID")
    fun deleteAttributesInGeoCache(geoCacheID: Long)

    @Query("SELECT * FROM waypoint WHERE cacheDetailIDFK = :geoCacheID")
    fun getWaypointsInGeoCache(geoCacheID: Long): LiveData<Array<Waypoint>>

    @Update
    fun updateWaypoint(waypoint: Waypoint)

    @Delete
    fun delete(waypoint: Waypoint)
}
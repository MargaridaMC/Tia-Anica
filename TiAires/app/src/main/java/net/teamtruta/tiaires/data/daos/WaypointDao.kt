package net.teamtruta.tiaires.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
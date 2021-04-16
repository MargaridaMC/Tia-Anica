package net.teamtruta.tiaires.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.teamtruta.tiaires.data.models.GeoCacheLog

@Dao
interface GeoCacheLogDao {

    @Insert
    fun insert(log: GeoCacheLog)

    @Insert
    suspend fun insert(vararg logs: GeoCacheLog)

    @Query("DELETE FROM log WHERE cacheDetailIDFK = :geoCacheID")
    fun deleteLogsInGeoCache(geoCacheID: Long)

}
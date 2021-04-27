package net.teamtruta.tiaires.data.daos

import androidx.room.*
import net.teamtruta.tiaires.data.models.GeoCacheWithLogsAndAttributesAndWaypoints
import net.teamtruta.tiaires.data.models.GeoCache

@Dao
interface GeoCacheDao {

    @Insert
    suspend fun insert(geoCache: GeoCache): Long

    @Insert
    suspend fun insert(vararg geoCache: GeoCache): List<Long>

    @Update
    suspend fun update(geoCache: GeoCache)

    @Update
    suspend fun update(vararg geoCache: GeoCache)

    @Transaction
    @Delete
    suspend fun delete(vararg geoCache: GeoCache)

    @Transaction
    @Query("SELECT * FROM cacheDetail WHERE id = :geoCacheID")
    fun getGeocache(geoCacheID: Long): GeoCacheWithLogsAndAttributesAndWaypoints

    // TODO : Delete all entries that are not being used in any geocache in tour

    @Query("SELECT id FROM cacheDetail WHERE code = :code")
    fun getGeoCacheIDFromCode(code: String): Long

    @Query("DELETE FROM cacheDetail WHERE cacheDetail.id NOT IN (SELECT geoCacheDetailIDFK from cache)")
    fun deleteAllGeoCachesNotBeingUsed()
}
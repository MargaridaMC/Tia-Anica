package net.teamtruta.tiaires.data

import androidx.room.*

@Dao
interface GeoCacheDao {

    @Insert
    suspend fun insert(geoCache: GeoCache)

    @Insert
    suspend fun insert(vararg geoCache: GeoCache): List<Long>

    @Transaction
    @Update
    suspend fun update(vararg geoCache: GeoCache)

    @Transaction
    @Delete
    suspend fun delete(vararg geoCache: GeoCache)

    @Transaction
    @Query("SELECT * FROM cacheDetail WHERE id = :geoCacheID")
    fun getGeocache(geoCacheID: Long): GeoCacheWithLogsAndAttributes

    // TODO : Delete all entries that are not being used in any geocache in tour

    @Query("SELECT id FROM cacheDetail WHERE code = :code")
    fun getGeoCacheIDFromCode(code: String): Long

    @Query("DELETE FROM cacheDetail WHERE cacheDetail.id NOT IN (SELECT geoCacheDetailIDFK from cache)")
    fun deleteAllGeoCachesNotBeingUsed()
}
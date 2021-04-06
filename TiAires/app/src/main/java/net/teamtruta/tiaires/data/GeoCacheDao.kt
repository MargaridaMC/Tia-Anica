package net.teamtruta.tiaires.data

import androidx.room.*

@Dao
interface GeoCacheDao {

    @Insert
    suspend fun insert(geoCache: GeoCache)

    @Transaction
    @Insert
    suspend fun insert(vararg geoCache: GeoCache): List<Long>

    @Transaction
    @Update
    suspend fun updateGeocache(vararg geoCache: GeoCache)

    @Transaction
    @Delete
    suspend fun deleteGeoCache(vararg geoCache: GeoCache)

    @Transaction
    @Query("SELECT * FROM cacheDetail WHERE id = :geoCacheID")
    fun getGeocache(geoCacheID: Long): GeoCacheWithLogsAndAttributes

    /* TODO: do this in ViewModel. Check if the getGeocache query returns any values
    @Query("SELECT * FROM  ")
    fun checkGeoCacheExistsInDb(geoCache: GeoCache): Boolean*/


    // TODO : Delete all entries that are not being used in any geocache in tour

    @Query("SELECT id FROM cacheDetail WHERE code = :code")
    fun getGeoCacheIDFromCode(code: String): Long


}
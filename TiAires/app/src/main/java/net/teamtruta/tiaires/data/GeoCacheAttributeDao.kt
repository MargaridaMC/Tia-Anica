package net.teamtruta.tiaires.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GeoCacheAttributeDao {

    @Insert
    fun insert(attribute: GeoCacheAttribute)

    @Insert
    fun insert(vararg attribute: GeoCacheAttribute)

    @Query("DELETE FROM attribute WHERE cacheDetailIDFK = :geoCacheID")
    fun deleteAttributesInGeoCache(geoCacheID: Long)
}
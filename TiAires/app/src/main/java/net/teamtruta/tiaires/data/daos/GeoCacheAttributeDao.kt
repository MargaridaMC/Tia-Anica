package net.teamtruta.tiaires.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.teamtruta.tiaires.data.models.GeoCacheAttribute

@Dao
interface GeoCacheAttributeDao {

    @Insert
    fun insert(attribute: GeoCacheAttribute)

    @Insert
    fun insert(vararg attribute: GeoCacheAttribute)

    @Query("DELETE FROM attribute WHERE cacheDetailIDFK = :geoCacheID")
    fun deleteAttributesInGeoCache(geoCacheID: Long)
}
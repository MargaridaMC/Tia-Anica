package net.teamtruta.tiaires.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface GeoCacheAttributeDao {

    @Insert
    fun addNewAttribute(attribute: GeoCacheAttribute)
}
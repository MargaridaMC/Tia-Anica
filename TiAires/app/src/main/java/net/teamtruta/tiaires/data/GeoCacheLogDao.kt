package net.teamtruta.tiaires.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface GeoCacheLogDao {

    @Insert
    fun addNewLog(log: GeoCacheLog)

}
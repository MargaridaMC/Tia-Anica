package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import net.teamtruta.tiaires.*
import java.util.*

class LogDbTable (context: Context){

    private val TAG = LogDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun deleteLogsInGeoCache(geoCacheID : Long) : Int{
        Log.d(TAG, "Deleted logs with geoCacheID: $geoCacheID")
        val db = dbHelper.writableDatabase
        val nLinesDeleted =  db.delete(LogEntry.TABLE_NAME, "${LogEntry.GEO_CACHE_DETAIL_ID_FK_COL} = ?", arrayOf("$geoCacheID"))
        db.close()
        return nLinesDeleted
    }

    fun storeLogsInGeoCache(gc : GeoCache, overwrite : Boolean = false){

        // If overwrite simply delete all logs pertaining to this cache and then rewrite them
        if(overwrite)
            deleteLogsInGeoCache(gc._id)

        val recentLogs = gc.recentLogs
        for(log in recentLogs){
            store(log, gc._id)
        }

    }

    fun store(log : GeoCacheLog, geoCacheID : Long) : Long{
        val db = dbHelper.writableDatabase
        val values = ContentValues()

        with(values){
            put(LogEntry.GEO_CACHE_DETAIL_ID_FK_COL, geoCacheID)
            put(LogEntry.LOG_DATE_COL, log.logDate.toFormattedString())
            put(LogEntry.LOG_TYPE_COL, log.logType.visitOutcomeString)
        }

        val id = db.insert(LogEntry.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAllLogsInGeoCache(geoCacheID: Long): List<GeoCacheLog> {

        val allLogs = mutableListOf<GeoCacheLog>()
        val db = dbHelper.readableDatabase
        val cursor = db.doQuery(LogEntry.TABLE_NAME, LogEntry.getAllColumns(), "${LogEntry.GEO_CACHE_DETAIL_ID_FK_COL} = ?", arrayOf("$geoCacheID"))
        while(cursor.moveToNext()){
            val logType = VisitOutcomeEnum.valueOfString(cursor.getString(LogEntry.LOG_TYPE_COL))
            val logDate : Date = cursor.getString(LogEntry.LOG_DATE_COL).toDate()
            val log = GeoCacheLog(logType, logDate)
            allLogs.add(log)
        }

        db.close()
        return allLogs
    }

}
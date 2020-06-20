package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import net.teamtruta.tiaires.*
import java.util.*

class LogDbTable (context: Context){

    private val TAG = LogDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

/*
    fun deleteEntry(id: Long) : Int{

        val db = dbHelper.writableDatabase
        val nLinesDeleted = db.delete(LogEntry.TABLE_NAME, "${LogEntry._ID} = ?", arrayOf("$id"))
        db.close()
        return nLinesDeleted
    }


    fun deleteEntries(ids : List<Long>) : Int {
        var totalLinesDeleted = 0
        for(id in ids){
            totalLinesDeleted += deleteEntry(id)
        }
        return totalLinesDeleted
    }
*/

    fun deleteLogsInCache(cacheID : Long) : Int{
        Log.d(TAG, "Deleted logs with cacheID: $cacheID")
        val db = dbHelper.writableDatabase
        val nLinesDeleted =  db.delete(LogEntry.TABLE_NAME, "${LogEntry.CACHE_DETAIL_ID_FK_COL} = ?", arrayOf("$cacheID"))
        db.close()
        return nLinesDeleted
    }

    fun storeLogsInCache(gc : Geocache){

        val recentLogs = gc.recentLogs
        for(log in recentLogs){
            store(log, gc._id)
        }

    }

    fun store(log : GeocacheLog, cacheID : Long) : Long{
        val db = dbHelper.writableDatabase
        val values = ContentValues()

        with(values){
            put(LogEntry.CACHE_DETAIL_ID_FK_COL, cacheID)
            put(LogEntry.LOG_DATE_COL, log.logDate.toFormattedString())
            put(LogEntry.LOG_TYPE_COL, log.logType.typeString)
        }

        val id = db.insert(LogEntry.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAllLogsInCache(geocacheID: Long): List<GeocacheLog> {

        val allLogs = mutableListOf<GeocacheLog>()
        val db = dbHelper.readableDatabase
        val cursor = db.doQuery(LogEntry.TABLE_NAME, LogEntry.getAllColumns(), "${LogEntry.CACHE_DETAIL_ID_FK_COL} = ?", arrayOf("$geocacheID"))
        while(cursor.moveToNext()){
            val logType = FoundEnumType.valueOfString(cursor.getString(LogEntry.LOG_TYPE_COL))
            val logDate : Date = cursor.getString(LogEntry.LOG_DATE_COL).toDate()
            val log = GeocacheLog(logType, logDate)
            allLogs.add(log)
        }

        db.close()
        return allLogs
    }

}
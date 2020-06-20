package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import net.teamtruta.tiaires.*

class CacheDetailDbTable(private val context: Context)  {

    private val TAG = CacheDetailDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun store(gc : Geocache, overwrite: Boolean) : Long{
        val db = dbHelper.writableDatabase
        val values = ContentValues()

        with(values){
            put(CacheDetailEntry.NAME_COL, gc.name)
            put(CacheDetailEntry.CODE_COL, gc.code)
            put(CacheDetailEntry.TYPE_COL, gc.type.typeString)
            put(CacheDetailEntry.SIZE_COL, gc.size)
            put(CacheDetailEntry.TERRAIN_COL, gc.terrain)
            put(CacheDetailEntry.DIF_COL, gc.difficulty)
            put(CacheDetailEntry.FIND_COL, gc.foundIt.typeString)
            put(CacheDetailEntry.HINT_COL, gc.hint)
            put(CacheDetailEntry.LAT_COL, gc.latitude.value)
            put(CacheDetailEntry.LON_COL, gc.longitude.value)
            put(CacheDetailEntry.FAV_COL, gc.favourites)
        }

        var id : Long = -1L
        if (overwrite){
            db.update(CacheDetailEntry.TABLE_NAME, values,
                    "${CacheDetailEntry._ID} = ?", arrayOf("${gc._id}"))
        } else {
            id = db.insert(CacheDetailEntry.TABLE_NAME, null, values)
            gc._id = id
        }

        LogDbTable(context).storeLogsInCache(gc)
        db.close()
        Log.d(TAG, "Stored new Geocache In Tour to the DB $gc")
        return id
    }

    fun store(caches: MutableList<Geocache>, overwrite: Boolean): MutableList<Long> {

        val cacheIds = mutableListOf<Long>()
        for(gc in caches){
            cacheIds.add(store(gc, overwrite))
        }
        return cacheIds
    }

    fun getGeocache(geocacheID: Long) : Geocache {

        val db = dbHelper.readableDatabase
        val columns = CacheDetailEntry.getAllColumns()

        val cursor = db.doQuery(CacheDetailEntry.TABLE_NAME, columns, "${CacheDetailEntry._ID} = ${geocacheID}")

        // Let's get all the logs that are associated with this cache
        val logList = LogDbTable(context).getAllLogsInCache(geocacheID)

        val gc : Geocache =
                with(cursor){
                    moveToFirst()
                    val name = getString(CacheDetailEntry.NAME_COL)
                    val code = getString(CacheDetailEntry.CODE_COL)
                    val type = CacheTypeEnum.valueOfString(getString(CacheDetailEntry.TYPE_COL))
                    val size = getString(CacheDetailEntry.SIZE_COL)
                    val terrain = getString(CacheDetailEntry.TERRAIN_COL)
                    val difficulty = getString(CacheDetailEntry.DIF_COL)
                    val visitType = FoundEnumType.valueOfString(getString(CacheDetailEntry.FIND_COL))
                    val hint = getString(CacheDetailEntry.HINT_COL)
                    val latitude = Coordinate(getDouble(CacheDetailEntry.LAT_COL))
                    val longitude = Coordinate(getDouble(CacheDetailEntry.LON_COL))
                    val nFavs = getInt(CacheDetailEntry.FAV_COL)
                    Geocache(code, name, latitude, longitude, size, difficulty, terrain, type, visitType, hint, nFavs, logList, geocacheID)
                }

        cursor.close()
        db.close()
        return gc
    }

    fun contains(code : String) : Long {

        val db = dbHelper.readableDatabase
        val columns = arrayOf(CacheDetailEntry._ID)
        val cursor = db.doQuery(CacheDetailEntry.TABLE_NAME, columns, "${CacheDetailEntry.CODE_COL} = ?", arrayOf(code))
        val id = if(cursor.moveToNext()){
            cursor.getLong(CacheDetailEntry._ID)
        } else {
            -1L
        }
        cursor.close()
        db.close()
        return id
    }

    fun collectCacheDetailGarbage() {

        Log.d(TAG, "Cache Detail Garbage Collection called")

        val idToDelete = mutableListOf<Long>()
        val SQL_QUERY = "SELECT  ${CacheDetailEntry._ID} FROM ${CacheDetailEntry.TABLE_NAME} " +
                "WHERE ${CacheDetailEntry._ID} NOT IN (" +
                "SELECT DISTINCT (${CacheEntry.CACHE_DETAIL_ID_FK_COL}) FROM ${CacheEntry.TABLE_NAME}" +
                ")"
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(SQL_QUERY, null)
        if(cursor.moveToFirst()){
            idToDelete.add(cursor.getLong(CacheDetailEntry._ID))
        }
        cursor.close()
        db.close()

        // Delete Logs pertaining to this cache and then the cache
        val logTable = LogDbTable(context)
        for(id in idToDelete){
            logTable.deleteLogsInCache(id)
            deleteEntry(id)
        }
/*

        val SQL_GARBAGE_COLLECTION_QUERY = "DELETE FROM ${CacheDetailEntry.TABLE_NAME} " +
                "WHERE ${CacheDetailEntry._ID} NOT IN (" +
                "SELECT DISTINCT (${CacheEntry.CACHE_DETAIL_ID_FK_COL}) FROM ${CacheEntry.TABLE_NAME}" +
                ")"
        val db = dbHelper.writableDatabase
        db.execSQL(SQL_GARBAGE_COLLECTION_QUERY)
*/

    }


    fun deleteEntry(id : Long){

        // First delete all logs related to this cache
        LogDbTable(context).deleteLogsInCache(id)

        // The delete entries to this cache
        val db = dbHelper.writableDatabase
        db.delete(CacheDetailEntry.TABLE_NAME, "${CacheDetailEntry._ID} = ?", arrayOf("$id"))
        db.close()
    }

    fun update(obtainedCaches: MutableList<Geocache>) {

        for(gc in obtainedCaches){
            val db = dbHelper.readableDatabase
            val cursor = db.doQuery(CacheDetailEntry.TABLE_NAME, arrayOf(CacheDetailEntry._ID),
                    "${CacheDetailEntry.CODE_COL} = ?", arrayOf(gc.code))
            cursor.moveToFirst()
            val id = cursor.getLong(CacheDetailEntry._ID)
            db.close()
            cursor.close()
            if(id != -1L){
                gc._id = id
                store(gc, true)
            }


        }


    }

    fun getIDFromCode(code: String): Long {

        val db = dbHelper.readableDatabase

        val cursor = db.doQuery(CacheDetailEntry.TABLE_NAME, arrayOf(CacheDetailEntry._ID),
        "${CacheDetailEntry.CODE_COL} = ?", arrayOf(code))

        val id = if(cursor.moveToFirst()){
            cursor.getLong(CacheDetailEntry._ID)
        } else {
            -1L
        }

        cursor.close()
        db.close()

        return id
    }


}



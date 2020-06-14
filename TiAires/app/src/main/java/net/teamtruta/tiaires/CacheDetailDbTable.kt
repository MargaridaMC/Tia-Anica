package net.teamtruta.tiaires

import android.content.ContentValues
import android.content.Context
import android.util.Log

class CacheDetailDbTable(context: Context)  {

    private val TAG = CacheDetailDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun store(gc : Geocache) : Long{
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

        val id = db.transaction { insert(CacheDetailEntry.TABLE_NAME, null, values) }

        Log.d(TAG, "Stored new Geocache In Tour to the DB $gc")
        return id
    }

    fun getGeocache(geocacheID: Long) : Geocache {

        val db = dbHelper.readableDatabase
        val columns = CacheDetailEntry.getAllColumns()

        val cursor = db.doQuery(CacheDetailEntry.TABLE_NAME, columns, "${CacheDetailEntry._ID} = ${geocacheID}")
        cursor.moveToFirst()
        val name = cursor.getString(CacheDetailEntry.NAME_COL)
        val code = cursor.getString(CacheDetailEntry.CODE_COL)
        val type = CacheTypeEnum.valueOfString(cursor.getString(CacheDetailEntry.TYPE_COL))
        val size = cursor.getString(CacheDetailEntry.SIZE_COL)
        val terrain = cursor.getString(CacheDetailEntry.TERRAIN_COL)
        val difficulty = cursor.getString(CacheDetailEntry.DIF_COL)
        val visitType = FoundEnumType.valueOfString(cursor.getString(CacheDetailEntry.FIND_COL))
        val hint = cursor.getString(CacheDetailEntry.HINT_COL)
        val latitude = Coordinate(cursor.getDouble(CacheDetailEntry.LAT_COL))
        val longitude = Coordinate(cursor.getDouble(CacheDetailEntry.LON_COL))
        val nFavs = cursor.getInt(CacheDetailEntry.CODE_COL)

        val gc = Geocache(code, name, latitude, longitude, size, difficulty, terrain, type, visitType, hint, nFavs)
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

        val SQL_GARBAGE_COLLECTION_QUERY = "DELETE FROM ${CacheDetailEntry.TABLE_NAME} " +
                "WHERE ${CacheDetailEntry._ID} NOT IN (" +
                "SELECT DISTINCT (${CacheEntry.CACHE_DETAIL_ID_FK_COL}) FROM ${CacheEntry.TABLE_NAME}" +
                ")"

        val db = dbHelper.writableDatabase
        db.execSQL(SQL_GARBAGE_COLLECTION_QUERY)

        db.close()
    }

    fun store(caches: MutableList<Geocache>): MutableList<Long> {

        val cacheIds = mutableListOf<Long>()
        for(gc in caches){
            cacheIds.add(store(gc))
        }
        return cacheIds
    }

}



package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import net.teamtruta.tiaires.*

class GeoCacheDetailDbTable(private val context: Context)  {

    private val TAG = GeoCacheDetailDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun store(gc : GeoCache, overwrite: Boolean) : Long{
        val db = dbHelper.writableDatabase
        val values = ContentValues()

        with(values){
            put(GeoCacheDetailEntry.NAME_COL, gc.name)
            put(GeoCacheDetailEntry.CODE_COL, gc.code)
            put(GeoCacheDetailEntry.TYPE_COL, gc.type.typeString)
            put(GeoCacheDetailEntry.SIZE_COL, gc.size)
            put(GeoCacheDetailEntry.TERRAIN_COL, gc.terrain)
            put(GeoCacheDetailEntry.DIF_COL, gc.difficulty)
            put(GeoCacheDetailEntry.FIND_COL, gc.previousVisitOutcome.visitOutcomeString)
            put(GeoCacheDetailEntry.HINT_COL, gc.hint)
            put(GeoCacheDetailEntry.LAT_COL, gc.latitude.value)
            put(GeoCacheDetailEntry.LON_COL, gc.longitude.value)
            put(GeoCacheDetailEntry.FAV_COL, gc.favourites)
        }

        var id : Long = -1L
        if (overwrite){
            db.update(GeoCacheDetailEntry.TABLE_NAME, values,
                    "${GeoCacheDetailEntry._ID} = ?", arrayOf("${gc._id}"))
        } else {
            id = db.insert(GeoCacheDetailEntry.TABLE_NAME, null, values)
            gc._id = id
        }

        db.close()

        // Store also the attributes
        GeoCacheAttributeDbTable(context).store(gc, overwrite)
        LogDbTable(context).storeLogsInGeoCache(gc, overwrite)

        Log.d(TAG, "Stored new GeoCache In Tour to the DB $gc")
        return id
    }

    fun store(geoCaches: MutableList<GeoCache>, overwrite: Boolean): MutableList<Long> {

        val geoCacheIds = mutableListOf<Long>()
        for(gc in geoCaches){
            geoCacheIds.add(store(gc, overwrite))
        }
        return geoCacheIds
    }

    fun getGeoCache(geoCacheID: Long) : GeoCache {

        val db = dbHelper.readableDatabase
        val columns = GeoCacheDetailEntry.getAllColumns()

        val cursor = db.doQuery(GeoCacheDetailEntry.TABLE_NAME, columns, "${GeoCacheDetailEntry._ID} = ${geoCacheID}")

        // Let's get all the logs that are associated with this cache as well as its attributes
        val logList = LogDbTable(context).getAllLogsInGeoCache(geoCacheID)
        val attributes = GeoCacheAttributeDbTable(context).getAttributesFromGeoCacheID(geoCacheID)

        val gc : GeoCache =
                with(cursor){
                    moveToFirst()
                    val name = getString(GeoCacheDetailEntry.NAME_COL)
                    val code = getString(GeoCacheDetailEntry.CODE_COL)
                    val type = GeoCacheTypeEnum.valueOfString(getString(GeoCacheDetailEntry.TYPE_COL))
                    val size = getString(GeoCacheDetailEntry.SIZE_COL)
                    val terrain = getDouble(GeoCacheDetailEntry.TERRAIN_COL)
                    val difficulty = getDouble(GeoCacheDetailEntry.DIF_COL)
                    val visitType = VisitOutcomeEnum.valueOfString(getString(GeoCacheDetailEntry.FIND_COL))
                    val hint = getString(GeoCacheDetailEntry.HINT_COL)
                    val latitude = Coordinate(getDouble(GeoCacheDetailEntry.LAT_COL))
                    val longitude = Coordinate(getDouble(GeoCacheDetailEntry.LON_COL))
                    val nFavs = getInt(GeoCacheDetailEntry.FAV_COL)
                    GeoCache(code, name, latitude, longitude, size, difficulty, terrain, type,
                            visitType, hint, nFavs, logList, attributes, geoCacheID)
                }

        cursor.close()
        db.close()
        return gc
    }

    fun contains(code : String) : Long {

        val db = dbHelper.readableDatabase
        val columns = arrayOf(GeoCacheDetailEntry._ID)
        val cursor = db.doQuery(GeoCacheDetailEntry.TABLE_NAME, columns, "${GeoCacheDetailEntry.CODE_COL} = ?", arrayOf(code))
        val id = if(cursor.moveToNext()){
            cursor.getLong(GeoCacheDetailEntry._ID)
        } else {
            -1L
        }
        cursor.close()
        db.close()
        return id
    }

    fun collectGeoCacheDetailGarbage() {

        Log.d(TAG, "Cache Detail Garbage Collection called")

        val idToDelete = mutableListOf<Long>()
        val SQL_QUERY = "SELECT  ${GeoCacheDetailEntry._ID} FROM ${GeoCacheDetailEntry.TABLE_NAME} " +
                "WHERE ${GeoCacheDetailEntry._ID} NOT IN (" +
                "SELECT DISTINCT (${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL}) FROM ${GeoCacheEntry.TABLE_NAME}" +
                ")"
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(SQL_QUERY, null)
        if(cursor.moveToFirst()){
            idToDelete.add(cursor.getLong(GeoCacheDetailEntry._ID))
        }
        cursor.close()
        db.close()

        // Delete Logs and attributes pertaining to this cache and then the cache
        val logTable = LogDbTable(context)
        val attributeTable = GeoCacheAttributeDbTable(context)
        for(id in idToDelete){
            logTable.deleteLogsInGeoCache(id)
            attributeTable.deleteAttributesInGeoCache(id)
            deleteEntry(id)
        }

    }

    private fun deleteEntry(id : Long){

        // First delete all logs related to this cache
        LogDbTable(context).deleteLogsInGeoCache(id)

        // Then delete all attributes related to this cache
        GeoCacheAttributeDbTable(context).deleteAttributesInGeoCache(id)

        // Then delete entries to this cache
        val db = dbHelper.writableDatabase
        db.delete(GeoCacheDetailEntry.TABLE_NAME, "${GeoCacheDetailEntry._ID} = ?", arrayOf("$id"))
        db.close()
    }

    fun update(obtainedGeoCaches: MutableList<GeoCache>) {

        for(gc in obtainedGeoCaches){
            val db = dbHelper.readableDatabase
            val cursor = db.doQuery(GeoCacheDetailEntry.TABLE_NAME, arrayOf(GeoCacheDetailEntry._ID),
                    "${GeoCacheDetailEntry.CODE_COL} = ?", arrayOf(gc.code))
            cursor.moveToFirst()

            val id = cursor.getLong(GeoCacheDetailEntry._ID)
            db.close()
            cursor.close()

            if(id != -1L){
                // Update Cache Detail Entry
                gc._id = id
                store(gc, true)

            }


        }


    }

    fun getIDFromCode(code: String): Long {

        val db = dbHelper.readableDatabase

        val cursor = db.doQuery(GeoCacheDetailEntry.TABLE_NAME, arrayOf(GeoCacheDetailEntry._ID),
        "${GeoCacheDetailEntry.CODE_COL} = ?", arrayOf(code))

        val id = if(cursor.moveToFirst()){
            cursor.getLong(GeoCacheDetailEntry._ID)
        } else {
            -1L
        }

        cursor.close()
        db.close()

        return id
    }

}



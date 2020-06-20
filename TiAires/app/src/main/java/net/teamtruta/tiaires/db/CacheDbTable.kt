package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log
import net.teamtruta.tiaires.*

class CacheDbTable (private val context: Context) {

    private val TAG = CacheDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun getAllCachesInTour(tourID: Long, dbConnection : DbConnection): List<GeocacheInTour>{

        val allCaches = mutableListOf<GeocacheInTour>()

        // Query database for all caches with this foreign key
        val db = dbHelper.readableDatabase
        val columns = CacheEntry.getAllColumns()
        val cursor = db.doQuery(CacheEntry.TABLE_NAME, columns, "${CacheEntry.TOUR_ID_FK_COL} = ${tourID}")//, orderBy = CacheEntry.ORDER_COL)
        while(cursor.moveToNext()){
            val geocacheInTour = cursor.getGeocacheInTour(dbConnection)
            allCaches.add(geocacheInTour)
        }

        cursor.close()
        db.close()

        return allCaches
    }

    fun getSizeOfTour(tourID : Long) : Long{

        val db = dbHelper.readableDatabase
        val nEntries = DatabaseUtils.queryNumEntries(db, CacheEntry.TABLE_NAME, "${CacheEntry.TOUR_ID_FK_COL} = ?", arrayOf("$tourID"))
        db.close()
        return nEntries
    }

    fun getNumberDNFInTour(tourID: Long):Long{
        val db = dbHelper.readableDatabase
        val nEntries = DatabaseUtils.queryNumEntries(db, CacheEntry.TABLE_NAME, "${CacheEntry.TOUR_ID_FK_COL} = ? AND ${CacheEntry.VISIT_COL} = ?", arrayOf("$tourID", FoundEnumType.DNF.typeString))
        db.close()
        return nEntries
    }

    fun getNumberFindInTour(tourID: Long):Long{
        val db = dbHelper.readableDatabase
        val nEntries = DatabaseUtils.queryNumEntries(db, CacheEntry.TABLE_NAME, "${CacheEntry.TOUR_ID_FK_COL} = ? AND ${CacheEntry.VISIT_COL} = ?", arrayOf("$tourID", FoundEnumType.Found.typeString))
        db.close()
        return nEntries
    }

    fun storeNew(tourIDFK: Long, geocacheIDFK: Long) : Long{

        val geocache = CacheDetailDbTable(context).getGeocache(geocacheIDFK)
        val geocacheInTour = GeocacheInTour(geocache)

        val db = dbHelper.writableDatabase

        // Get index for next element in order
        // val cursor = db.doQuery(CacheEntry.TABLE_NAME, arrayOf("MAX(${CacheEntry.ORDER_COL})"), "${CacheEntry.TOUR_ID_FK_COL} = ?", arrayOf("$tourIDFK"))
        /*val cursor = db.rawQuery("SELECT MAX(${CacheEntry.ORDER_COL}) FROM ${CacheEntry.TABLE_NAME} " +
                "WHERE ${CacheEntry.TOUR_ID_FK_COL} = $tourIDFK", null)
        val orderIdx = try{
            cursor.moveToFirst()
            cursor.getInt(CacheEntry.ORDER_COL) + 1
        } catch (e : Exception){
            -1
        }*/

        val values = ContentValues()
        with(values){

            // If we have already found this cache then we want that information to be included in the Geocache In Tour
            if(geocache.foundIt == FoundEnumType.Found || geocache.foundIt == FoundEnumType.DNF){
                put(CacheEntry.VISIT_COL, geocache.foundIt.typeString)
            } else {
                put(CacheEntry.VISIT_COL, geocacheInTour.visit.typeString)
            }

            put(CacheEntry.FOUND_DATE_COL, geocacheInTour.foundDate?.toString())
            put(CacheEntry.NEEDS_MAINTENANCE_COL, geocacheInTour.needsMaintenance)
            put(CacheEntry.NOTES_COL, geocacheInTour.notes)
            put(CacheEntry.FOUND_TRACKABLE_COL, geocacheInTour.foundTrackable)
            put(CacheEntry.DROPPED_TRACKABLE_COL, geocacheInTour.droppedTrackable)
            put(CacheEntry.FAV_POINT_COL, geocacheInTour.favouritePoint)
            put(CacheEntry.TOUR_ID_FK_COL, tourIDFK)
            put(CacheEntry.CACHE_DETAIL_ID_FK_COL, geocacheIDFK)
            //put(CacheEntry.ORDER_COL, orderIdx)
        }

        val id = db.transaction { insert(CacheEntry.TABLE_NAME, null, values) }

        Log.d(TAG, "Stored new GeocacheInTour to the DB $geocacheInTour")

        //cursor.close()
        db.close()

        return id
    }

    fun getTourCacheCodes(tourID: Long):List<String>{

        val db = dbHelper.readableDatabase
        val cacheCodes = mutableListOf<String>()
        val SQL_QUERY = "SELECT ${CacheDetailEntry.CODE_COL} " +
                "FROM ${CacheDetailEntry.TABLE_NAME} " +
                "WHERE ${CacheDetailEntry._ID} IN (" +
                    "SELECT ${CacheEntry.CACHE_DETAIL_ID_FK_COL} " +
                    "FROM ${CacheEntry.TABLE_NAME} " +
                    "WHERE ${CacheEntry.TOUR_ID_FK_COL} = ?" +
                ")"

        val cursor = db.rawQuery(SQL_QUERY, arrayOf("$tourID"))
        while(cursor.moveToNext()){
            val code = cursor.getString(CacheDetailEntry.CODE_COL)
            cacheCodes.add(code)
        }

        cursor.close()
        db.close()

        return cacheCodes
    }

    fun deleteCache(code: String) {
        val db = dbHelper.writableDatabase
        val QUERY = "DELETE FROM ${CacheEntry.TABLE_NAME} " +
                "WHERE ${CacheEntry.CACHE_DETAIL_ID_FK_COL} IN (" +
                "SELECT ${CacheDetailEntry._ID} " +
                "FROM ${CacheDetailEntry.TABLE_NAME} " +
                "WHERE ${CacheDetailEntry.CODE_COL} = ?)"
        db.execSQL(QUERY, arrayOf(code))
        db.close()
    }

/*    fun deleteEntry(id : Long) : Int{
        val db = dbHelper.writableDatabase
        return db.delete(CacheEntry.TABLE_NAME, "${CacheEntry._ID} = ?", arrayOf("$id"))
    }*/

    fun addCachesToTour(tourID : Long, newGeocaches: MutableList<Long>) {
        for(geocacheID in newGeocaches){
            storeNew(tourID, geocacheID)
        }
    }

    fun updateEntry (geocache : GeocacheInTour) : Boolean{

        val db = dbHelper.writableDatabase
        val values = ContentValues()
        with(values){
            put(CacheEntry.NOTES_COL, geocache.notes)
            put(CacheEntry.VISIT_COL,  geocache.visit.typeString)
            put(CacheEntry.NEEDS_MAINTENANCE_COL, geocache.needsMaintenance)
            put(CacheEntry.FOUND_DATE_COL, geocache.foundDate?.toFormattedString())
            put(CacheEntry.FOUND_TRACKABLE_COL, geocache.foundTrackable)
            put(CacheEntry.DROPPED_TRACKABLE_COL, geocache.droppedTrackable)
            put(CacheEntry.FAV_POINT_COL, geocache.favouritePoint)
        }

        val nLinesChanged = db.update(CacheEntry.TABLE_NAME, values, "${CacheEntry._ID} = ?", arrayOf("${geocache._id}"))

        db.close()

        return nLinesChanged == 1
    }

    fun getGeocacheFromID(cacheID : Long, dbConnection : DbConnection) : GeocacheInTour {

        val db = dbHelper.readableDatabase

        val cursor = db.doQuery(CacheEntry.TABLE_NAME, CacheEntry.getAllColumns(), "${CacheEntry._ID} = ?", arrayOf("$cacheID"))
        cursor.moveToFirst()
        val geocacheInTour = cursor.getGeocacheInTour(dbConnection)

        db.close()
        cursor.close()
        return geocacheInTour
    }

    fun Cursor.getGeocacheInTour(dbConnection : DbConnection) : GeocacheInTour {

        val geocacheID = getLong(CacheEntry.CACHE_DETAIL_ID_FK_COL)
        val gc = CacheDetailDbTable(context).getGeocache(geocacheID)

        val _id = getLong(CacheEntry._ID)
        val notes = getString(CacheEntry.NOTES_COL)
        val visit = FoundEnumType.valueOfString(getString(CacheEntry.VISIT_COL))
        val foundDate = getString(CacheEntry.FOUND_DATE_COL)?.toDate()
        val needsMaintenance = getBoolean(CacheEntry.NEEDS_MAINTENANCE_COL)
        val foundTrackable = getBoolean(CacheEntry.FOUND_TRACKABLE_COL)
        val droppedTrackable = getBoolean(CacheEntry.DROPPED_TRACKABLE_COL)
        val favouritePoint = getBoolean(CacheEntry.FAV_POINT_COL)

        return GeocacheInTour(gc, notes, visit, needsMaintenance,
                foundDate, foundTrackable, droppedTrackable, favouritePoint, _id, dbConnection)
    }
}


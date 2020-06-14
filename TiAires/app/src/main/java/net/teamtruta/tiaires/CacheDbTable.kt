package net.teamtruta.tiaires

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log

class CacheDbTable (private val context: Context) {

    private val TAG = CacheDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)


    fun getAllCachesInTour(tourID: Long, dbConnection : DbConnection): List<GeocacheInTour>{

        val allCaches = mutableListOf<GeocacheInTour>()

        // Query database for all caches with this foreign key
        val db = dbHelper.readableDatabase
        val columns = CacheEntry.getAllColumns()
        val cursor = db.doQuery(CacheEntry.TABLE_NAME, columns, "${CacheEntry.TOUR_ID_FK_COL} = ${tourID}")
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

        val gc = GeocacheInTour()
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        with(values){
            put(CacheEntry.FOUND_DATE_COL, gc.foundDate?.toString())
            put(CacheEntry.NEEDS_MAINTENANCE_COL, gc.needsMaintenance)
            put(CacheEntry.VISIT_COL, gc.visit.typeString)
            put(CacheEntry.NOTES_COL, gc.notes)
            put(CacheEntry.FOUND_TRACKABLE_COL, gc.foundTrackable)
            put(CacheEntry.DROPPED_TRACKABLE_COL, gc.droppedTrackable)
            put(CacheEntry.FAV_POINT_COL, gc.favouritePoint)
            put(CacheEntry.TOUR_ID_FK_COL, tourIDFK)
            put(CacheEntry.CACHE_DETAIL_ID_FK_COL, geocacheIDFK)
        }

        val id = db.transaction { insert(CacheEntry.TABLE_NAME, null, values) }

        Log.d(TAG, "Stored new GeocacheInTour to the DB $gc")

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

    fun removeCache(code: String) {
        val db = dbHelper.writableDatabase
        val QUERY = "DELETE FROM ${CacheEntry.TABLE_NAME} " +
                "WHERE ${CacheEntry.CACHE_DETAIL_ID_FK_COL} IN (" +
                "SELECT ${CacheDetailEntry._ID} " +
                "FROM ${CacheDetailEntry.TABLE_NAME} " +
                "WHERE ${CacheDetailEntry.CODE_COL} = ?)"
        db.execSQL(QUERY, arrayOf(code))
        db.close()
    }

    fun addCachesToTour(tourID : Long, newGeocaches: MutableList<Long>) {
        for(geocacheID in newGeocaches){
            storeNew(tourID, geocacheID)
        }
    }

    fun setGeocacheVisit(geocache : GeocacheInTour) : Boolean{

        val db = dbHelper.writableDatabase
        val values = ContentValues()
        with(values){
            put(CacheEntry.VISIT_COL,  geocache.visit.typeString)
            //if(geocache.foundDate == null) putNull(CacheEntry.FOUND_DATE_COL)
            //else put(CacheEntry.FOUND_DATE_COL, geocache.foundDate.toString())
            put(CacheEntry.FOUND_DATE_COL, geocache.foundDate?.toString())
            put(CacheEntry.NEEDS_MAINTENANCE_COL, geocache.needsMaintenance)
            put(CacheEntry.FAV_POINT_COL, geocache.favouritePoint)
            put(CacheEntry.FOUND_TRACKABLE_COL, geocache.foundTrackable)
            put(CacheEntry.DROPPED_TRACKABLE_COL, geocache.droppedTrackable)
            put(CacheEntry.NOTES_COL, geocache.notes)
        }

        val nLinesChanged = db.update(CacheEntry.TABLE_NAME, values, "${CacheEntry._ID} = ?", arrayOf("${geocache._id}"))

        db.close()

        return nLinesChanged == 1
    }


    fun getGeocacheFromID(cacheID : Long, dbConnection : DbConnection) : GeocacheInTour{

        val db = dbHelper.readableDatabase

        val cursor = db.doQuery(CacheEntry.TABLE_NAME, CacheEntry.getAllColumns(), "${CacheEntry._ID} = ?", arrayOf("$cacheID"))
        cursor.moveToFirst()
        val geocacheInTour = cursor.getGeocacheInTour(dbConnection)

        db.close()
        cursor.close()
        return geocacheInTour
    }

    fun Cursor.getGeocacheInTour(dbConnection : DbConnection) : GeocacheInTour{

        val geocacheID = getLong(CacheEntry.CACHE_DETAIL_ID_FK_COL)
        val gc = CacheDetailDbTable(context).getGeocache(geocacheID)
        val geocacheInTour = GeocacheInTour(gc, dbConnection)

        geocacheInTour._id = getLong(CacheEntry._ID)
        geocacheInTour.notes = getString(CacheEntry.NOTES_COL)
        geocacheInTour.visit = FoundEnumType.valueOfString(getString(CacheEntry.VISIT_COL))
        // TODO: set found date
        //val foundDate = cursor.getString(CacheEntry.FOUND_DATE_COL)
        //geocacheInTour.foundDate = (foundDate)
        geocacheInTour.needsMaintenance = getBoolean(CacheEntry.NEEDS_MAINTENANCE_COL)
        geocacheInTour.foundTrackable = getBoolean(CacheEntry.FOUND_TRACKABLE_COL)
        geocacheInTour.droppedTrackable = getBoolean(CacheEntry.DROPPED_TRACKABLE_COL)
        geocacheInTour.favouritePoint = getBoolean(CacheEntry.FAV_POINT_COL)

        return geocacheInTour
    }
}


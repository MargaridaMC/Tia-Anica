package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log
import net.teamtruta.tiaires.*

class GeoCacheDbTable (private val context: Context) {

    private val TAG = GeoCacheDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun getAllGeoCachesInTour(tourID: Long, dbConnection : DbConnection): List<GeoCacheInTour>{

        val allGeoCaches = mutableListOf<GeoCacheInTour>()

        // Query database for all caches with this foreign key
        val db = dbHelper.readableDatabase
        val columns = GeoCacheEntry.getAllColumns()
        val cursor = db.doQuery(GeoCacheEntry.TABLE_NAME, columns,
                "${GeoCacheEntry.TOUR_ID_FK_COL} = $tourID", orderBy = GeoCacheEntry.ORDER_COL)
        while(cursor.moveToNext()){
            val geoCacheInTour = cursor.getGeoCacheInTour(dbConnection)
            allGeoCaches.add(geoCacheInTour)
        }

        cursor.close()
        db.close()

        return allGeoCaches
    }

    fun getSizeOfTour(tourID : Long) : Long{

        val db = dbHelper.readableDatabase
        val nEntries = DatabaseUtils.queryNumEntries(db, GeoCacheEntry.TABLE_NAME,
                "${GeoCacheEntry.TOUR_ID_FK_COL} = ?", arrayOf("$tourID"))
        db.close()
        return nEntries
    }

    fun getNumberDNFInTour(tourID: Long):Long{
        val db = dbHelper.readableDatabase
        val nEntries = DatabaseUtils.queryNumEntries(db, GeoCacheEntry.TABLE_NAME,
                "${GeoCacheEntry.TOUR_ID_FK_COL} = ? AND ${GeoCacheEntry.VISIT_COL} = ?",
                arrayOf("$tourID", VisitOutcomeEnum.DNF.visitOutcomeString))
        db.close()
        return nEntries
    }

    fun getNumberFindInTour(tourID: Long):Long{
        val db = dbHelper.readableDatabase
        val nEntries = DatabaseUtils.queryNumEntries(db, GeoCacheEntry.TABLE_NAME,
                "${GeoCacheEntry.TOUR_ID_FK_COL} = ? AND ${GeoCacheEntry.VISIT_COL} = ?",
                arrayOf("$tourID", VisitOutcomeEnum.Found.visitOutcomeString))
        db.close()
        return nEntries
    }

    fun storeNew(tourIDFK: Long, geoCacheIDFK: Long) : Long{

        val geoCache = GeoCacheDetailDbTable(context).getGeoCache(geoCacheIDFK)
        val geoCacheInTour = GeoCacheInTour(geoCache)

        val db = dbHelper.writableDatabase

        // Get index for next element in order
        val cursor = db.rawQuery("SELECT MAX(${GeoCacheEntry.ORDER_COL}) AS ${GeoCacheEntry.ORDER_COL} " +
                "FROM ${GeoCacheEntry.TABLE_NAME} " +
                "WHERE ${GeoCacheEntry.TOUR_ID_FK_COL} = ?", arrayOf("$tourIDFK"))
        val orderIdx = try{
            cursor.moveToFirst()
            cursor.getInt(GeoCacheEntry.ORDER_COL) + 1000
        } catch (e : Exception){
            1000
        }

        val values = ContentValues()
        with(values){

            // If we have already found this cache then we want that information to be included in the Geocache In Tour
            if(geoCache.previousVisitOutcome == VisitOutcomeEnum.Found || geoCache.previousVisitOutcome == VisitOutcomeEnum.DNF
                    || geoCache.previousVisitOutcome == VisitOutcomeEnum.Disabled){
                put(GeoCacheEntry.VISIT_COL, geoCache.previousVisitOutcome.visitOutcomeString)
            } else {
                put(GeoCacheEntry.VISIT_COL, geoCacheInTour.currentVisitOutcome.visitOutcomeString)
            }

            put(GeoCacheEntry.FOUND_DATE_COL, geoCacheInTour.foundDate?.toString())
            put(GeoCacheEntry.NEEDS_MAINTENANCE_COL, geoCacheInTour.needsMaintenance)
            put(GeoCacheEntry.NOTES_COL, geoCacheInTour.notes)
            put(GeoCacheEntry.FOUND_TRACKABLE_COL, geoCacheInTour.foundTrackable)
            put(GeoCacheEntry.DROPPED_TRACKABLE_COL, geoCacheInTour.droppedTrackable)
            put(GeoCacheEntry.FAV_POINT_COL, geoCacheInTour.favouritePoint)
            put(GeoCacheEntry.TOUR_ID_FK_COL, tourIDFK)
            put(GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL, geoCacheIDFK)
            put(GeoCacheEntry.ORDER_COL, orderIdx)
            put(GeoCacheEntry.IMAGE_COL, geoCacheInTour.pathToImage)
        }

        val id = db.transaction { insert(GeoCacheEntry.TABLE_NAME, null, values) }

        Log.d(TAG, "Stored new GeoCacheInTour to the DB $geoCacheInTour")

        cursor.close()
        db.close()

        return id
    }


    fun deleteCache(code: String, tourIDFK: Long) {

        // Get the ID corresponding to the geocache with this code
        val id = GeoCacheDetailDbTable(context).getIDFromCode(code)

        val db = dbHelper.writableDatabase
        db.delete(GeoCacheEntry.TABLE_NAME,
                "${GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL} = ? AND ${GeoCacheEntry.TOUR_ID_FK_COL} = ?",
                            arrayOf("$id", "$tourIDFK"))

        db.close()
    }

    fun addGeoCachesToTour(tourID : Long, newGeoCaches: MutableList<Long>) {
        for(geoCacheID in newGeoCaches){
            storeNew(tourID, geoCacheID)
        }
    }

    fun updateEntry (geoCache : GeoCacheInTour) : Boolean{

        val db = dbHelper.writableDatabase
        val values = ContentValues()
        with(values){
            put(GeoCacheEntry.NOTES_COL, geoCache.notes)
            put(GeoCacheEntry.VISIT_COL,  geoCache.currentVisitOutcome.visitOutcomeString)
            put(GeoCacheEntry.NEEDS_MAINTENANCE_COL, geoCache.needsMaintenance)
            put(GeoCacheEntry.FOUND_DATE_COL, geoCache.foundDate?.toFormattedString())
            put(GeoCacheEntry.FOUND_TRACKABLE_COL, geoCache.foundTrackable)
            put(GeoCacheEntry.DROPPED_TRACKABLE_COL, geoCache.droppedTrackable)
            put(GeoCacheEntry.FAV_POINT_COL, geoCache.favouritePoint)
            put(GeoCacheEntry.ORDER_COL, geoCache.orderIdx)
            put(GeoCacheEntry.IMAGE_COL, geoCache.pathToImage)
        }

        val nLinesChanged = db.update(GeoCacheEntry.TABLE_NAME, values, "${GeoCacheEntry._ID} = ?", arrayOf("${geoCache._id}"))

        db.close()

        return nLinesChanged == 1
    }

    fun getGeoCacheFromID(geoCacheID : Long, dbConnection : DbConnection) : GeoCacheInTour {

        val db = dbHelper.readableDatabase

        val cursor = db.doQuery(GeoCacheEntry.TABLE_NAME, GeoCacheEntry.getAllColumns(), "${GeoCacheEntry._ID} = ?", arrayOf("$geoCacheID"))
        cursor.moveToFirst()
        val geoCacheInTour = cursor.getGeoCacheInTour(dbConnection)

        db.close()
        cursor.close()
        return geoCacheInTour
    }

    fun Cursor.getGeoCacheInTour(dbConnection : DbConnection) : GeoCacheInTour {

        val geoCacheID = getLong(GeoCacheEntry.GEO_CACHE_DETAIL_ID_FK_COL)
        val gc = GeoCacheDetailDbTable(context).getGeoCache(geoCacheID)

        val _id = getLong(GeoCacheEntry._ID)
        val notes = getString(GeoCacheEntry.NOTES_COL)
        val visit = VisitOutcomeEnum.valueOfString(getString(GeoCacheEntry.VISIT_COL))
        val foundDate = getString(GeoCacheEntry.FOUND_DATE_COL)?.toDate()
        val needsMaintenance = getBoolean(GeoCacheEntry.NEEDS_MAINTENANCE_COL)
        val foundTrackable = getStringOrNull(GeoCacheEntry.FOUND_TRACKABLE_COL)
        val droppedTrackable = getStringOrNull(GeoCacheEntry.DROPPED_TRACKABLE_COL)
        val favouritePoint = getBoolean(GeoCacheEntry.FAV_POINT_COL)
        val orderIdx = getInt(GeoCacheEntry.ORDER_COL)
        val imagePath = getStringOrNull(GeoCacheEntry.IMAGE_COL)
        return GeoCacheInTour(gc, notes, visit, needsMaintenance,
                foundDate, foundTrackable, droppedTrackable, favouritePoint,
                orderIdx, imagePath, _id, dbConnection)
    }

    fun deleteAllGeoCachesInTour(_id: Long) {
        val db = dbHelper.readableDatabase
        db.delete(GeoCacheEntry.TABLE_NAME, "${GeoCacheEntry.TOUR_ID_FK_COL} = ?",
                arrayOf("$_id"))
        db.close()
    }
}


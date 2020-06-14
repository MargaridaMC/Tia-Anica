package net.teamtruta.tiaires

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import java.util.ArrayList

class TourDbTable(context: Context) {

    private val TAG = TourDbTable::class.simpleName
    private var dbHelper : TiAiresDb = TiAiresDb(context)


    fun storeNewTour(name : String, isCurrentTour : Boolean) : Long{

        val db = dbHelper.writableDatabase
        val values = ContentValues()
        with(values){
            put(TourEntry.NAME_COL, name)
            put(TourEntry.CURRENT_TOUR_COL, isCurrentTour)
        }

        val id = db.transaction { insert(TourEntry.TABLE_NAME, null, values) }

        Log.d(TAG, "Stored new tour to the DB $name")

        return id
    }

    fun getAllTours(dbConnection: DbConnection) : List<GeocachingTour> {

        val db = dbHelper.readableDatabase
        val columns = TourEntry.getAllColumns()//arrayOf(TourEntry._ID, TourEntry.NAME_COL, TourEntry.CURRENT_TOUR_COL)
        val cursor = db.doQuery(TourEntry.TABLE_NAME, columns)

        val allTourNames = mutableListOf<GeocachingTour>()
        while(cursor.moveToNext()){
            val tour = cursor.getGeocachingTour(dbConnection)
            tour._tourCaches = dbConnection.cacheTable.getAllCachesInTour(tour._id, dbConnection)
            allTourNames.add(tour)
        }

        cursor.close()
        db.close()
        return allTourNames

    }

    fun getTour(tourID : Long, dbConnection: DbConnection) : GeocachingTour {

        val db = dbHelper.readableDatabase

        val cursor = db.doQuery(TourEntry.TABLE_NAME, TourEntry.getAllColumns(),
                "${TourEntry._ID} = ?", arrayOf("$tourID"))
        cursor.moveToFirst()

        val tour = cursor.getGeocachingTour(dbConnection)

        cursor.close()
        db.close()

        return tour
    }

    fun deleteTour(tourID : Long) : Int{

        val db = dbHelper.writableDatabase
        val nLinesDeleted = db.delete(TourEntry.TABLE_NAME, "${TourEntry._ID} = ?", arrayOf("$tourID"))
        db.close()
        return nLinesDeleted
    }

    fun changeName(tourID : Long, newTourName: String) : Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(TourEntry.NAME_COL, newTourName)
        val nLinesChanged = db.update(TourEntry.TABLE_NAME, values, "${TourEntry._ID} = ?", arrayOf("$tourID"))
        return nLinesChanged == 1
    }

}

private fun Cursor.getGeocachingTour(dbConnection: DbConnection) : GeocachingTour {

    val name = getString(TourEntry.NAME_COL)
    val id = getLong(TourEntry._ID)
    val isCurrentTour = getBoolean(TourEntry.CURRENT_TOUR_COL)
    return GeocachingTour(name, id, isCurrentTour, dbConnection)
}
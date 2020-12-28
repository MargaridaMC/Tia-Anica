package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import net.teamtruta.tiaires.GeoCache
import net.teamtruta.tiaires.GeoCacheAttributeEnum
import net.teamtruta.tiaires.doQuery
import net.teamtruta.tiaires.getString

class GeoCacheAttributeDbTable (context: Context) {

    private val TAG = GeoCacheAttributeDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun store(gc: GeoCache, overwrite: Boolean = false) : Long{

        // If overwriting simply delete all entries with this geocache ID and readd them
        if(overwrite){
            deleteAttributesInGeoCache(gc._id)
        }

        val db = dbHelper.writableDatabase

        for(attribute: GeoCacheAttributeEnum in gc.attributes){

            val values = ContentValues()
            with(values){
                put(AttributeEntry.ATTRIBUTE_TYPE, attribute.attributeString)
                put(AttributeEntry.GEO_CACHE_DETAIL_ID_FK_COL, gc._id)
            }

            val id: Long = db.insert(AttributeEntry.TABLE_NAME, null, values)
            if(id == -1L) return id
        }

        db.close()
        return 1L
    }

    fun deleteAttributesInGeoCache(geoCacheID: Long): Int {

        Log.d(TAG, "Deleted attributes with geoCacheID: $geoCacheID")

        val db = dbHelper.writableDatabase
        val nLinesDeleted =  db.delete(AttributeEntry.TABLE_NAME,
                "${AttributeEntry.GEO_CACHE_DETAIL_ID_FK_COL} = ?", arrayOf("$geoCacheID"))
        db.close()
        return nLinesDeleted

    }

    fun getAttributesFromGeoCacheID(geoCacheId: Long): List<GeoCacheAttributeEnum>{

        val db = dbHelper.readableDatabase
        val attributes = mutableListOf<GeoCacheAttributeEnum>()
        val cursor = db.doQuery(AttributeEntry.TABLE_NAME, arrayOf(AttributeEntry.ATTRIBUTE_TYPE),
                "${AttributeEntry.GEO_CACHE_DETAIL_ID_FK_COL} = ?", arrayOf(geoCacheId.toString()))

        while(cursor.moveToNext()){
            attributes.add(GeoCacheAttributeEnum.valueOfString(
                    cursor.getString(AttributeEntry.ATTRIBUTE_TYPE)))
        }

        return attributes
    }

}
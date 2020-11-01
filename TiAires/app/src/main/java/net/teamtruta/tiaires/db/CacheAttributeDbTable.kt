package net.teamtruta.tiaires.db

import android.content.ContentValues
import android.content.Context
import android.util.Log
import net.teamtruta.tiaires.Geocache
import net.teamtruta.tiaires.GeocacheAttributeEnum
import net.teamtruta.tiaires.doQuery
import net.teamtruta.tiaires.getString

class CacheAttributeDbTable (context: Context) {

    private val TAG = CacheAttributeDbTable::class.simpleName
    private val dbHelper = TiAiresDb(context)

    fun store(gc: Geocache, overwrite: Boolean = false) : Long{

        // If overwriting simply delete all entries with this cache ID and readd them
        if(overwrite){
            deleteAttributesInCache(gc._id)
        }

        val db = dbHelper.writableDatabase

        for(attribute: GeocacheAttributeEnum in gc.attributes){

            val values = ContentValues()
            with(values){
                put(AttributeEntry.ATTRIBUTE_TYPE, attribute.attributeString)
                put(AttributeEntry.CACHE_DETAIL_ID_FK_COL, gc._id)
            }

            val id: Long = db.insert(AttributeEntry.TABLE_NAME, null, values)
            if(id == -1L) return id
        }

        db.close()
        return 1L
    }

    fun deleteAttributesInCache(cacheID: Long): Int {

        Log.d(TAG, "Deleted attributes with cacheID: $cacheID")

        val db = dbHelper.writableDatabase
        val nLinesDeleted =  db.delete(AttributeEntry.TABLE_NAME,
                "${AttributeEntry.CACHE_DETAIL_ID_FK_COL} = ?", arrayOf("$cacheID"))
        db.close()
        return nLinesDeleted

    }

    fun getAttributesFromCacheID(cacheId: Long): List<GeocacheAttributeEnum>{

        val db = dbHelper.readableDatabase
        val attributes = mutableListOf<GeocacheAttributeEnum>()
        val cursor = db.doQuery(AttributeEntry.TABLE_NAME, arrayOf(AttributeEntry.ATTRIBUTE_TYPE),
                "${AttributeEntry.CACHE_DETAIL_ID_FK_COL} = ?", arrayOf(cacheId.toString()))

        while(cursor.moveToNext()){
            attributes.add(GeocacheAttributeEnum.valueOfString(
                    cursor.getString(AttributeEntry.ATTRIBUTE_TYPE)))
        }

        return attributes
    }

}
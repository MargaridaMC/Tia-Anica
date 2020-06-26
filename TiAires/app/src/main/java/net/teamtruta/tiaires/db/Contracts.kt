package net.teamtruta.tiaires.db

import android.provider.BaseColumns

val DATABASE_NAME = "tiaires.db"
val DATABASE_VERSION = 11

object TourEntry : BaseColumns{
    val TABLE_NAME = "tour"
    val _ID = "id"
    val NAME_COL = "name"
    val CURRENT_TOUR_COL = "isCurrentTour"
    fun getAllColumns(): Array<String> {
        return arrayOf(_ID, NAME_COL, CURRENT_TOUR_COL)
    }
}

object CacheEntry : BaseColumns{

    val TABLE_NAME = "cache"
    val _ID = "id"
    val FOUND_DATE_COL = "foundDate"
    val NEEDS_MAINTENANCE_COL = "needsMaintenance"
    val VISIT_COL = "visit"
    val NOTES_COL = "notes"
    val FOUND_TRACKABLE_COL = "foundTrackable"
    val DROPPED_TRACKABLE_COL = "droppedTrackable"
    val FAV_POINT_COL = "favouritePoint"
    val TOUR_ID_FK_COL = "tourID_FK"
    val CACHE_DETAIL_ID_FK_COL = "cacheDetailID_FK"
    val ORDER_COL = "orderBy"
    fun getAllColumns(): Array<String> {
        return arrayOf(_ID, FOUND_DATE_COL, NEEDS_MAINTENANCE_COL, VISIT_COL,
                NOTES_COL, FOUND_TRACKABLE_COL, DROPPED_TRACKABLE_COL, FAV_POINT_COL,
                TOUR_ID_FK_COL, CACHE_DETAIL_ID_FK_COL, ORDER_COL)
    }
}


object CacheDetailEntry : BaseColumns{

    val TABLE_NAME = "cacheDetail"
    val _ID = "id"
    val NAME_COL = "name"
    val CODE_COL = "code"
    val TYPE_COL = "type"
    val SIZE_COL = "size"
    val TERRAIN_COL = "terrain"
    val DIF_COL = "difficulty"
    val FIND_COL = "foundIt"
    val HINT_COL = "hint"
    val LAT_COL = "latitude"
    val LON_COL = "longitude"
    val FAV_COL = "favourites"
    fun getAllColumns(): Array<String> {
        return arrayOf(NAME_COL, CODE_COL, TYPE_COL, SIZE_COL, TERRAIN_COL, DIF_COL, FIND_COL, HINT_COL,
                LAT_COL, LON_COL, FAV_COL)
    }
}

object LogEntry : BaseColumns{

    val TABLE_NAME = "log"
    val _ID = "id"
    val LOG_TYPE_COL = "type"
    val LOG_DATE_COL = "date"
    val CACHE_DETAIL_ID_FK_COL = "cacheDetailID_FK"
    fun getAllColumns(): Array<String> {
        return arrayOf(_ID, LOG_TYPE_COL, LOG_DATE_COL, CACHE_DETAIL_ID_FK_COL)
    }
}

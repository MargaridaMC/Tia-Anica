package net.teamtruta.tiaires.db

import android.provider.BaseColumns

val DATABASE_NAME = "tiaires.db"
val DATABASE_VERSION = 19

object TourEntry : BaseColumns{
    val TABLE_NAME = "tour"
    val _ID = "id"
    val NAME_COL = "name"
    val CURRENT_TOUR_COL = "isCurrentTour"
    val STARTING_POINT_LAT = "startingPointLatitude"
    val STARTING_POINT_LON = "startingPointLongitude"
    fun getAllColumns(): Array<String> {
        return arrayOf(_ID, NAME_COL, CURRENT_TOUR_COL, STARTING_POINT_LAT, STARTING_POINT_LON)
    }
}

object GeoCacheEntry : BaseColumns{

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
    val GEO_CACHE_DETAIL_ID_FK_COL = "cacheDetailID_FK"
    val ORDER_COL = "orderBy"
    val IMAGE_COL = "image"
    val VISIT_DATETIME_COL = "visitDatetime"
    fun getAllColumns(): Array<String> {
        return arrayOf(_ID, VISIT_DATETIME_COL, NEEDS_MAINTENANCE_COL, VISIT_COL,
                NOTES_COL, FOUND_TRACKABLE_COL, DROPPED_TRACKABLE_COL, FAV_POINT_COL,
                TOUR_ID_FK_COL, GEO_CACHE_DETAIL_ID_FK_COL, ORDER_COL, IMAGE_COL)
    }
}


object GeoCacheDetailEntry : BaseColumns{

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
    val GEO_CACHE_DETAIL_ID_FK_COL = "cacheDetailID_FK"
    fun getAllColumns(): Array<String> {
        return arrayOf(_ID, LOG_TYPE_COL, LOG_DATE_COL, GEO_CACHE_DETAIL_ID_FK_COL)
    }
}

object AttributeEntry : BaseColumns{
    val TABLE_NAME = "attribute"
    val _ID = "id"
    val ATTRIBUTE_TYPE = "type"
    val GEO_CACHE_DETAIL_ID_FK_COL = "cacheDetailID_FK"
}

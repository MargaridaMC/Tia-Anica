package net.teamtruta.tiaires.data.models

import androidx.room.*
import java.util.*

/**
 * This class represents, in a very simplified way, a cache log. A string saying if it was a "Found it", "write note", "did not find", and the date.
 * It can be extended with more data as relevant for the use.
 */
@Entity(tableName = "log",
        foreignKeys = [ForeignKey(entity = GeoCache::class,
                parentColumns = ["id"],
                childColumns = ["cacheDetailIDFK"],
                onDelete = ForeignKey.CASCADE)])

data class GeoCacheLog(

        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val logType : VisitOutcomeEnum,
        val logDate : Date,

        @ColumnInfo(index = true)
        var cacheDetailIDFK: Long) {
        constructor(logType: VisitOutcomeEnum, logDate: Date) : this(0, logType, logDate, 0)
}
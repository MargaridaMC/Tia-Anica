package net.teamtruta.tiaires.data

import androidx.room.*
import net.teamtruta.tiaires.VisitOutcomeEnum
import java.time.Instant

@Entity(tableName = "cache",
        foreignKeys = [ForeignKey(
                entity = GeoCache::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("geoCacheDetailIDFK"),
                onDelete = ForeignKey.CASCADE),
                ForeignKey(
                entity = GeocachingTour::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("tourIDFK"),
                onDelete = ForeignKey.CASCADE)])

class GeoCacheInTour(

        var notes : String = "",
        var currentVisitOutcome: VisitOutcomeEnum = VisitOutcomeEnum.NotAttempted,
        var needsMaintenance : Boolean = false,
        var currentVisitDatetime : Instant? = null,
        var foundTrackable : String? = null,
        var droppedTrackable : String? = null,
        var favouritePoint : Boolean = false,
        var orderIdx : Int = -1,
        var pathToImage : String? = null,
        var draftUploaded : Boolean = false,

        @PrimaryKey(autoGenerate = true)
        val id : Long = 0,

        @ColumnInfo(index = true)
        val geoCacheDetailIDFK: Long,

        @ColumnInfo(index = true)
        val tourIDFK: Long,
){


    // the following fields have the "In Tour" semantics.
    // So if a geoCache was found but not in the tour, currentVisitOutcome will be = NotAttempted;
    // same logic applies to other fields.

    /*
    constructor(geoCache: GeoCache): this(geoCache = geoCache, _dbConnection = null)

    fun saveChanges() {
        _dbConnection?.geoCacheTable?.updateEntry(this)
    }

    companion object {
        fun getGeoCacheFromID(geoCacheID: Long, dbConnection: DbConnection): GeoCacheInTour {
            return dbConnection.geoCacheTable.getGeoCacheFromID(geoCacheID, dbConnection)
        }
    }*/
}
package net.teamtruta.tiaires

import net.teamtruta.tiaires.db.DbConnection
import java.time.Instant

/**
 * GeoCacheInTour
 */
class GeoCacheInTour(
    val geoCache: GeoCache,
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
    val _id : Long = -1,
    private var _dbConnection : DbConnection? = null){

    // the following fields have the "In Tour" semantics.
    // So if a geoCache was found but not in the tour, currentVisitOutcome will be = NotAttempted;
    // same logic applies to other fields.

    constructor(geoCache: GeoCache): this(geoCache = geoCache, _dbConnection = null)

    fun saveChanges() {
        _dbConnection?.geoCacheTable?.updateEntry(this)
    }

    companion object {
        fun getGeoCacheFromID(geoCacheID: Long, dbConnection: DbConnection): GeoCacheInTour {
            return dbConnection.geoCacheTable.getGeoCacheFromID(geoCacheID, dbConnection)
        }
    }
}
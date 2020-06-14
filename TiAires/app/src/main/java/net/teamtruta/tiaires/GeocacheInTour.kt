package net.teamtruta.tiaires

import java.util.*

/**
 * GeocacheInTour
 */
class GeocacheInTour {
    // These set and get methods needs to be public otherwise the serialization will not work.
    var geocache: Geocache? = null

    // the following fields have the "In Tour" semantics. So if a cache was found but not in the tour, _found will be = NotAttempted; same logic applies to other fields.
    var notes = ""

    // não gosto deste nome. attempt, visit, ...?
    var visit : FoundEnumType = FoundEnumType.NotAttempted
    var needsMaintenance = false
    var foundDate: Date? = null
    var foundTrackable = false
    var droppedTrackable = false
    var favouritePoint = false
    @JvmField
    var _id: Long = 0
    var _dbConnection: DbConnection? = null

    constructor(gc: Geocache?, dbConnection: DbConnection?) {
        geocache = gc
        _dbConnection = dbConnection
        // TODO: what if we've already done this cache?
    }

    constructor() {}

    fun setNewVisit(visit: FoundEnumType) {
        this.visit = visit
        val success = _dbConnection!!.cacheTable.setGeocacheVisit(this)
    }

    fun saveChanges() {
        val success = _dbConnection!!.cacheTable.setGeocacheVisit(this)
    }

    companion object {
        @JvmStatic
        fun getGeocacheFromID(cacheID: Long, dbConnection: DbConnection): GeocacheInTour {
            return dbConnection.cacheTable.getGeocacheFromID(cacheID, dbConnection)
        }
    }
}
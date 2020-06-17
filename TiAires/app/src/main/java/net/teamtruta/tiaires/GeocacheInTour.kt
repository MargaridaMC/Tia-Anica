package net.teamtruta.tiaires

import java.util.*

/**
 * GeocacheInTour
 */
class GeocacheInTour(val geocache: Geocache, var notes : String = "",
                     var visit: FoundEnumType = FoundEnumType.NotAttempted,
                     var needsMaintenance : Boolean = false, var foundDate : Date? = null,
                     var foundTrackable : Boolean = false, var droppedTrackable : Boolean = false,
                     var favouritePoint : Boolean = false, val _id : Long = -1,
                     val _dbConnection : DbConnection? = null) {

    // the following fields have the "In Tour" semantics. So if a cache was found but not in the tour, _found will be = NotAttempted; same logic applies to other fields.
/*    constructor(geocache: Geocache, dbConnection: DbConnection) :
            this(geocache, visit = geocache.foundIt, _dbConnection = dbConnection)*/

    constructor(geocache: Geocache) : this(geocache = geocache, _dbConnection = null)

    /*constructor(gc: Geocache, dbConnection: DbConnection?) {
        geocache = gc
        _dbConnection = dbConnection
        // TODO: what if we've already done this cache?
    }

    constructor() {}*/

    fun setNewVisit(visit: FoundEnumType) {
        this.visit = visit
        _dbConnection?.cacheTable?.updateEntry(this) //returns a boolean to check for success
    }

    fun saveChanges() {
        _dbConnection?.cacheTable?.updateEntry(this)
    }

    companion object {
        fun getGeocacheFromID(cacheID: Long, dbConnection: DbConnection): GeocacheInTour {
            return dbConnection.cacheTable.getGeocacheFromID(cacheID, dbConnection)
        }
    }
}
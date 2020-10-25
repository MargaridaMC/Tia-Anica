package net.teamtruta.tiaires

import net.teamtruta.tiaires.db.DbConnection
import java.util.*

/**
 * GeocacheInTour
 */
class GeocacheInTour(val geocache: Geocache, var notes : String = "",
                     var visit: FoundEnumType = FoundEnumType.NotAttempted,
                     var needsMaintenance : Boolean = false, var foundDate : Date? = null,
                     var foundTrackable : String? = null, var droppedTrackable : String? = null,
                     var favouritePoint : Boolean = false, var orderIdx : Int = -1,
                     var pathToImage : String? = null, val _id : Long = -1,
                     private val _dbConnection : DbConnection? = null) {

    // the following fields have the "In Tour" semantics. So if a cache was found but not in the tour, _found will be = NotAttempted; same logic applies to other fields.

    constructor(geocache: Geocache) : this(geocache = geocache, _dbConnection = null)

    fun saveChanges() {
        _dbConnection?.cacheTable?.updateEntry(this)
    }

    companion object {
        fun getGeocacheFromID(cacheID: Long, dbConnection: DbConnection): GeocacheInTour {
            return dbConnection.cacheTable.getGeocacheFromID(cacheID, dbConnection)
        }
    }
}
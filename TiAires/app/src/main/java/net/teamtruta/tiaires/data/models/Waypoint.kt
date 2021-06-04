package net.teamtruta.tiaires.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(foreignKeys = [ForeignKey(entity = GeoCache::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("cacheDetailIDFK"),
                onDelete = ForeignKey.CASCADE)])
class Waypoint(
    @PrimaryKey(autoGenerate = true)
        val id: Long,

    var name: String = "",
    var latitude: Coordinate?,
    var longitude: Coordinate?,

    @ColumnInfo(name="isDone") // this is me being lazy and not wanting to change the name in the db
    var waypointState: Int = WAYPOINT_NOT_ATTEMPTED,
    val isParking: Boolean = false,

    var notes: String = "",

    @ColumnInfo(index = true)
        var cacheDetailIDFK: Long
) {

    constructor(name:String, latitude: Coordinate?, longitude: Coordinate?,
                    waypointState: Int, isParking: Boolean, notes: String):
                this(0, name, latitude, longitude,
                    waypointState, isParking, notes, 0)

        constructor(name: String, latitude: String, longitude: String,
                    waypointState: Int, isParking: Boolean, notes: String) :
                this(0, name, Coordinate(latitude), Coordinate(longitude),
                    waypointState, isParking, notes, 0)

        constructor(name: String, latitude: Coordinate?, longitude: Coordinate?):
                this(0, name, latitude, longitude,
                    WAYPOINT_NOT_ATTEMPTED, false, "", 0)

        constructor(name: String, latitude: String, longitude: String):
                this(0, name, Coordinate(latitude), Coordinate(longitude),
                    WAYPOINT_NOT_ATTEMPTED, false, "", 0)

    fun isDone(): Boolean{
        return waypointState == WAYPOINT_DONE || waypointState == WAYPOINT_NOT_FOUND
    }

    companion object{
        val WAYPOINT_NOT_ATTEMPTED = 0
        val WAYPOINT_DONE = 1
        val WAYPOINT_NOT_FOUND = 2
    }

}
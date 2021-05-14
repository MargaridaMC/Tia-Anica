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

        val name: String = "",
        val latitude: Coordinate?,
        val longitude: Coordinate?,

        var isDone: Boolean,
        val isParking: Boolean = false,

        val notes: String = "",

        @ColumnInfo(index = true)
        var cacheDetailIDFK: Long
) {

    constructor(name:String, latitude: Coordinate?, longitude: Coordinate?,
                    waypointDone: Boolean, isParking: Boolean, notes: String):
                this(0, name, latitude, longitude,
                        waypointDone, isParking, notes, 0)

        constructor(name: String, latitude: String, longitude: String,
                    waypointDone: Boolean, isParking: Boolean, notes: String) :
                this(0, name, Coordinate(latitude), Coordinate(longitude),
                        waypointDone, isParking, notes, 0)

        constructor(name: String, latitude: Coordinate?, longitude: Coordinate?):
                this(0, name, latitude, longitude,
                        false, false, "", 0)

        constructor(name: String, latitude: String, longitude: String):
                this(0, name, Coordinate(latitude), Coordinate(longitude),
                        false, false, "", 0)

}
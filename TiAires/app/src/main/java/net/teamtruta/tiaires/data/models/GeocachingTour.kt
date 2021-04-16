package net.teamtruta.tiaires.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * GeocachingTour class, representing a tour to go out and find them gaches. Includes a list of Geocaches in tour, among others.
 */
@Entity(tableName = "tour")
class GeocachingTour() {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String = ""
    var isCurrentTour = false
    var startingPointLatitude: Coordinate? = null
    var startingPointLongitude: Coordinate? = null

    constructor(name: String) : this() {
        this.name = name
    }


    fun setStartingPoint(latitude: Double, longitude: Double){
        this.startingPointLatitude = Coordinate(latitude)
        this.startingPointLongitude = Coordinate(longitude)
    }


}
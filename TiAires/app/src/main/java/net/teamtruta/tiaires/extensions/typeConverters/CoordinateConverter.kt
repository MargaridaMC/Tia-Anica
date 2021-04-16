package net.teamtruta.tiaires.extensions.typeConverters

import androidx.room.TypeConverter
import net.teamtruta.tiaires.data.models.Coordinate

class CoordinateConverter{

    @TypeConverter
    fun toDouble(coordinate: Coordinate?): Double?{
        return coordinate?.value
    }

    @TypeConverter
    fun toCoordinate(value: Double?): Coordinate? {
        return value?.let { Coordinate(it) }
    }

}
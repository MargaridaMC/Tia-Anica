package net.teamtruta.tiaires.data.models

import java.util.*

class Coordinate {
    var value = 0.0
    var direction : String = ""

    constructor(coordinateString: String) {
        val success = parse(coordinateString)
        if (!success) {
            println("Couldn't parse coordinates")
        }
    }

    constructor(value: Double) {
        this.value = value
    }

    private fun parse(input_string: String): Boolean {
        val input = input_string.trim()

        val pattern = Regex("([NSEW])?\\s?([0-9]+)[°\\s]+([0-9]+)[.\\s]+([0-9]+)", RegexOption.IGNORE_CASE)
        if(pattern.matches(input)){
            val matchResults = pattern.find(input)
            val groups = matchResults?.destructured?.toList()
            val direction = groups?.get(0)
            val deg = groups?.get(1)?.let { parseDouble(it) }
            val min = parseDouble(groups?.get(2) + '.' + groups?.get(3))
            return if(direction == null || deg == null){
                false
            } else {
                value = DM2Decimal(deg, min, direction)
                this.direction = direction
                true
            }
        }

        return false
    }

    private fun parseDouble(numberStr: String): Double {
        return try {
            numberStr.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }


    override fun toString(): String {
        return value.toString()
    }

    private fun DM2Decimal(deg: Double, min: Double, dir: String): Double {
        var result = deg + min / 60.0
        if (dir.equals("S", ignoreCase = true)
                ||
                dir.equals("W", ignoreCase = true)
                ||
                dir.equals("-", ignoreCase = true)) result = -result
        return result
    }

    companion object {

        fun prettyPrint(latitude: Coordinate, longitude: Coordinate): String{
            // Pretty print coordinates
            val (latitudeDegrees, latitudeMinutes) = Decimal2DM(latitude.value)
            val latitudeDirection = if(latitude.value > 0) "N" else "S"
            val latitudeMinutesString = "%.${3}f".format(latitudeMinutes).padStart(6, '0')

            val (longitudeDegrees, longitudeMinutes) = Decimal2DM(longitude.value)
            val longitudeDirection = if(longitude.value > 0) "E" else "W"
            val longitudeMinutesString = "%.${3}f".format(longitudeMinutes).padStart(6, '0')

            return latitudeDirection + latitudeDegrees.toInt() + " " +
                    latitudeMinutesString +
                    " " + longitudeDirection + longitudeDegrees.toInt() + " " +
                    longitudeMinutesString
        }

        private fun Decimal2DM(coordinates: Double): DoubleArray {
            var degrees = coordinates.toInt().toDouble()
            var minutes = Math.abs(coordinates - degrees) * 60
            minutes = Math.round(minutes * 1000.0) / 1000.0
            if (minutes == 60.0) {
                degrees += 1.0
                minutes = 0.0
            }
            return doubleArrayOf(Math.abs(degrees), minutes)
        }

        fun fromFullCoordinates(coordinateString: String): Pair<Coordinate, Coordinate>?{

            return try {
                coordinateString.toUpperCase(Locale.ROOT)

                var indexOfLongitudeStart = coordinateString.indexOf("E")
                if(indexOfLongitudeStart == -1) indexOfLongitudeStart = coordinateString.indexOf("W")

                val latitude = Coordinate(coordinateString.substring(0, indexOfLongitudeStart).trim())
                val longitude = Coordinate(coordinateString.substring(indexOfLongitudeStart).trim())

                Pair(latitude, longitude)
            } catch (e: Exception){
                e.printStackTrace()
                null
            }


        }

    }
}

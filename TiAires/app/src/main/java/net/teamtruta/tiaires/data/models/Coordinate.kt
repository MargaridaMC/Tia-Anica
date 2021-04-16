package net.teamtruta.tiaires.data.models

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

    companion object {

        private fun DM2Decimal(deg: Double, min: Double, dir: String): Double {
            var result = deg + min / 60.0
            if (dir.equals("S", ignoreCase = true)
                    ||
                    dir.equals("W", ignoreCase = true)
                    ||
                    dir.equals("-", ignoreCase = true)) result = -result
            return result
        }
    }
}
package net.teamtruta.tiaires

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

    private fun parse(input: String): Boolean {
        val input = input.trim()

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
/*
        val p = Pattern.compile(
                "([NSEW])?\\s?([0-9]+)[°\\s]+([0-9]+)[.\\s]+([0-9]+)",
                Pattern.CASE_INSENSITIVE
        )
        val m = p.matcher(input)
        var minStr : String? = "0"
        if (m.matches()) {
            if (m.group(1) != null) direction = m.group(1)
            if (m.group(2) != null) deg = parseDouble(m.group(2))
            if (m.group(3) != null) {
                minStr = m.group(3)
                min = parseDouble(m.group(3))
            }
            if (m.group(4) != null) {
                min = parseDouble(minStr + "." + m.group(4))
            }
            if (deg != null) {
                value = DM2Decimal(deg, min, direction)
                return true
            }
        } //else  {// bad input format }*/
        return false
    }

    private fun parseDouble(numberStr: String): Double {
        return try {
            numberStr.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    /* private static double[] Decimal2DM(Double coordinates){

            double degrees = coordinates.intValue();
            double minutes = (coordinates - degrees) * 60;
            minutes = Math.round(minutes * 1000d) / 1000d;

            if(minutes==60.0) {
                degrees += 1;
                minutes = 0.0;
            }

            return new double[] {degrees, minutes};

        }*/
    override fun toString(): String {
        return value.toString()
    }

    companion object {
        /*public void setValue(double value){
            this.value = value;
        }

        public String getDirection() {

            return this.direction;
        }

        public void setDirection(String direction){
            this.direction = direction;
        }
*/
        private fun DM2Decimal(deg: Double, min: Double, dir: String): Double {
            val _d = deg
            val _m = min
            var result = _d + _m / 60.0
            if (dir.equals("S", ignoreCase = true)
                    ||
                    dir.equals("W", ignoreCase = true)
                    ||
                    dir.equals("-", ignoreCase = true)) result = -result
            return result
        }
    }
}
package net.teamtruta.tiaires;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Coordinate {

        private Double value = 0.0;
        private String direction = "";

        Coordinate(String coordinateString) {

            boolean success = parse(coordinateString);
            if(!success) {
                System.out.println("Couldn't parse coordinates");
            }
        }

        @JsonCreator
        Coordinate(@JsonProperty("value") double value){

            this.value = value;

        }

        private Boolean parse(String input){

            Double deg = null;
            Double min = null;

            input = input.trim();

            Pattern p = Pattern.compile(
                    "([NSEW])?\\s?([0-9]+)[°\\s]+([0-9]+)[.\\s]+([0-9]+)",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher m = p.matcher(input);

            String minStr = "0";

            if(m.matches()){
                if(m.group(1) != null)
                    direction = m.group(1);
                if(m.group(2) != null)
                    deg = parseDouble(m.group(2));
                if(m.group(3) != null) {
                    minStr = m.group(3);
                    min = parseDouble(m.group(3));
                }
                if(m.group(4) != null) {
                    min = parseDouble(minStr + "." + m.group(4));
                }
                if(deg != null) {
                    value = DM2Decimal(deg, min, direction);
                    return true;
                }
            } //else  {// bad input format }

            return false;
        }

        private Double parseDouble(String numberStr){
            try {
                return Double.parseDouble(numberStr);
            } catch (Exception e){
                return 0.0;
            }
        }

        public double getValue() {

            return this.value;
        }

        public void setValue(double value){
            this.value = value;
        }

        public String getDirection() {

            return this.direction;
        }

        public void setDirection(String direction){
            this.direction = direction;
        }

        private static double DM2Decimal(Double deg, Double min, String dir){
            double _d; double _m;

            _d = deg == null ? 0. : deg;
            _m = min == null ? 0. : min;

            double result = _d + _m / 60.0;

            if(
                    dir.equalsIgnoreCase("S")
                            ||
                            dir.equalsIgnoreCase("W")
                            ||
                            dir.equalsIgnoreCase("-")
            ) result = -result;

            return result;
        }

        private static double[] Decimal2DM(Double coordinates){

            double degrees = (double) coordinates.intValue();
            double minutes = (coordinates - degrees) * 60;
            minutes = Math.round(minutes * 1000d) / 1000d;

            if(minutes==60.0) {
                degrees += 1;
                minutes = 0.0;
            }

            return new double[] {degrees, minutes};

        }

        public String toString(){
            return value.toString();
        }

}

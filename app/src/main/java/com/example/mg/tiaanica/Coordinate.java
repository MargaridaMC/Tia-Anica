package com.example.mg.tiaanica;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Coordinate {

    private Double lat = null;
    private Double lon = null;

    private Double latDeg = null;
    private Double latMin = null;
    private Double latSec = null;
    private String latDir = "N";

    private Double lonDeg = null;
    private Double lonMin = null;
    private Double lonSec = null;
    private String lonDir = "E";

    Coordinate(String coord) {

        boolean success = parse(coord);
        if(!success) {
            System.out.println("Couldn't parse coordinates");
        }
    }

     Coordinate(String lat, String lon) {

        boolean success = parseLatitude(lat) && parseLongitude(lon);
        if(!success) {
            System.out.println("Couldn't parse coordinates");
        }
    }

    private Boolean parse(String input){

        input = input.trim();

        Pattern p = Pattern.compile(
                "([NSEW])?\\s?([0-9]+)[°\\s]+([0-9]+)[\\.\\s]+([0-9]+)" +
                        "\\s+" + "([NSEW])?\\s?([0-9]+)[°\\s]+([0-9]+)[\\.\\s]+([0-9]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(input);

        if(m.matches()){
            if(m.group(1) != null)
                latDir = m.group(1);
            if(m.group(2) != null)
                latDeg = parseDouble(m.group(2));
            if(m.group(3) != null)
                latMin = parseDouble(m.group(3));
            if(m.group(4) != null)
                latSec = parseDouble(m.group(4));
            if(m.group(5) != null)
                lonDir = m.group(5);
            if(m.group(6) != null)
                lonDeg = parseDouble(m.group(6));
            if(m.group(7) != null)
                lonMin = parseDouble(m.group(7));
            if(m.group(8) != null)
                lonSec = parseDouble(m.group(8));

            if(latDeg != null && latMin != null && latSec != null) {
                lat = DMS2Decimal(latDeg, latMin, latSec, latDir);
                lon = DMS2Decimal(lonDeg, lonMin, lonSec, lonDir);

                return true;
            } else if(latDeg != null && latMin != null && latSec == null) {
                lat = DM2Decimal(latDeg, latMin, latDir);
                lon = DM2Decimal(lonDeg, lonMin, lonDir);

                return true;
            } else if (latDeg != null && latMin == null && latSec == null){

                lat = latDeg;
                if(
                        latDir.equalsIgnoreCase("S")
                                ||
                                latDir.equalsIgnoreCase("-")
                ) lat = -lat;

                lon = lonDeg;
                if(
                        lonDir.equalsIgnoreCase("W")
                                ||
                                lonDir.equalsIgnoreCase("-")
                ) lon = -lon;

                return true;
            }

        } else  {
            // bad input format
        }

        return false;
    }

    private Boolean parseLatitude(String input) {
        input = input.trim();

        Pattern p;
        p = Pattern.compile(
                "([NS]{1})?\\s?([0-9]{1,})[°\\s]{1,}([0-9]{1,})[\\.\\s]{1,}([0-9]{1,})",
                Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(input);

        if(m.matches()){
            if(m.group(1) != null)
                latDir = m.group(1);
            if(m.group(2) != null)
                latDeg = parseDouble(m.group(2));
            if(m.group(3) != null)
                latMin = parseDouble(m.group(3));
            if(m.group(4) != null)
                latSec = parseDouble(m.group(4));

            if(latDeg != null && latMin != null && latSec != null) {
                lat = DMS2Decimal(latDeg, latMin, latSec, latDir);

                return true;
            } else if(latDeg != null && latMin != null && latSec == null) {
                lat = DM2Decimal(latDeg, latMin, latDir);

                return true;
            } else if (latDeg != null && latMin == null && latSec == null){

                lat = latDeg;
                if(
                        latDir.equalsIgnoreCase("S")
                                ||
                                latDir.equalsIgnoreCase("-")
                ) lat = -lat;

                return true;
            }

        } else  {
            // bad input format
        }

        return false;
    }

    private Boolean parseLongitude(String input) {
        input = input.trim();

        Pattern p = Pattern.compile(
                "([EW]{1})?\\s?([0-9]{1,})[°\\s]{1,}([0-9]{1,})[\\.\\s]{1,}([0-9]{1,})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(input);

        if(m.matches()){
            if(m.group(1) != null)
                lonDir = m.group(1);
            if(m.group(2) != null)
                lonDeg = parseDouble(m.group(2));
            if(m.group(3) != null)
                lonMin = parseDouble(m.group(3));
            if(m.group(4) != null)
                lonSec = parseDouble(m.group(4));

            if(lonDeg != null && lonMin != null && lonSec != null) {
                lon = DMS2Decimal(lonDeg, lonMin, lonSec, lonDir);

                return true;
            } else if(lonDeg != null && lonMin != null && lonSec == null) {
                lon = DM2Decimal(lonDeg, lonMin, lonDir);

                return true;
            } else if (lonDeg != null && lonMin == null && lonSec == null){

                lon = lonDeg;
                if(
                        lonDir.equalsIgnoreCase("W")
                                ||
                                lonDir.equalsIgnoreCase("-")
                ) lon = -lon;

                return true;
            }

        } else  {
            // bad input format
        }

        return false;
    }

    private Double parseDouble(String numberStr){
        try {
            return Double.parseDouble(numberStr);
        } catch (Exception e){
            return 0.0;
        }
    }

    private String getLatitude() {

        String latDegInt = String.format("%02d", latDeg.intValue());
        String latMinInt = String.format("%02d", latMin.intValue());
        String latSecInt = String.format("%03d", latSec.intValue());

        return latDir + latDegInt + " " + latMinInt + "." + latSecInt;
    }

    private String getLongitude() {

        String lonDegInt = String.format("%02d", lonDeg.intValue());
        String lonMinInt = String.format("%02d", lonMin.intValue());
        String lonSecInt = String.format("%03d", lonSec.intValue());

        return lonDir + lonDegInt + " " + lonMinInt + "." + lonSecInt;
    }

    private void setLatitude(Double lat) {
        this.lat = lat;
    }

    private void setLongitude(Double lon) {
        this.lon = lon;
    }

    private void setLatDeg(Double latDeg) {
        this.latDeg = latDeg;
    }

    private void setLonDeg(Double lonDeg) {
        this.lonDeg = lonDeg;
    }

    private void setLatMin(Double latVals) {
        this.latMin = latVals;
    }

    private void setLonMin(Double lonMin) {
        this.lonMin = lonMin;
    }

    private void setLatSec(Double latSec) {
        this.latSec = latSec;
    }

    private void setLonSec(Double lonSec) {
        this.lonSec = lonSec;
    }
    private static double DM2Decimal(Double latDeg2, Double latMin2, String dir){
        double _d; double _m;

        _d = latDeg2 == null ? 0. : latDeg2;
        _m = latMin2 == null ? 0. : latMin2;

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

    private static double DMS2Decimal(Double lonDeg2, Double lonMin2, Double lonSec2, String dir){
        double _d; double _m; double _s;

        _d = lonDeg2 == null ? 0. : lonDeg2;
        _m = lonMin2 == null ? 0. : lonMin2;
        _s = lonSec2 == null ? 0. : lonSec2;

        double result = _d + _m / 60.0 + _s / 3600;
        if(
                dir.equalsIgnoreCase("S")
                        ||
                        dir.equalsIgnoreCase("W")
                        ||
                        dir.equalsIgnoreCase("-")
        ) result = -result;

        return result;
    }

    private static double[] Decimal2DMS(Double coordinates){

        double degrees = (double) coordinates.intValue();
        double minutes = (coordinates - degrees) * 60;
        double seconds;
        minutes = Math.round(minutes * 1000d) / 1000d;

        if(minutes==60.0) {
            degrees += 1;
            minutes = 0.0;
        }

        seconds = (minutes - (int) minutes) * 1000;

        return new double[] {degrees, minutes, seconds};

    }

     void Offset(double angle, double distanceInMeters){

        double X = lat;
        double Y = lon;

        double rad = Math.PI * angle / 180;

        double xRad = Math.PI * X / 180; // convert to radians
        double yRad = Math.PI * Y / 180;

        double R = 6378100; //Radius of the Earth in meters
        double x = Math.asin(Math.sin(xRad) * Math.cos(distanceInMeters/ R)
                + Math.cos(xRad) * Math.sin(distanceInMeters/ R) * Math.cos(rad));

        double y = yRad + Math.atan2(Math.sin(rad) * Math.sin(distanceInMeters/ R) * Math.cos(xRad), Math.cos(distanceInMeters/ R) - Math.sin(xRad) * Math.sin(x));

        x = x * 180 / Math.PI; // convert back to degrees
        y = y * 180 / Math.PI;

        double[] latVals = Decimal2DMS(x);
        double[] lonVals = Decimal2DMS(y);

        this.setLatitude(x);
        this.setLongitude(y);

        this.setLatDeg(latVals[0]);
        this.setLatMin(latVals[1]);
        this.setLatSec(latVals[2]);

        this.setLonDeg(lonVals[0]);
        this.setLonMin(lonVals[1]);
        this.setLonSec(lonVals[2]);

    }

    String getFullCoordinates() {
        return this.getLatitude() + " " + this.getLongitude();
    }
}

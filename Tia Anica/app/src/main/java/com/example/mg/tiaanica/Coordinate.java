package com.example.mg.tiaanica;

import android.annotation.SuppressLint;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Coordinate implements CoordinateOffset{

    private Double lat = 0.0;
    private Double lon = 0.0;

    private Double latDeg = null;
    private Double latMin = null;
    private String latDir = "N";

    private Double lonDeg = null;
    private Double lonMin = null;
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

    Coordinate(double lat, double lon){

        this.lat = lat;
        this.lon = lon;

        double[] latValues = Decimal2DM(lat);
        double[] lonValues = Decimal2DM(lon);

        this.latDeg = latValues[0];
        this.latMin = latValues[1];

        this.lonDeg = lonValues[0];
        this.lonMin = lonValues[1];
    }

    private Boolean parse(String input){

        input = input.trim();

        Pattern p = Pattern.compile(
                "([NSEW])?\\s?([0-9]+)[°\\s]+([0-9]+)[.\\s]+([0-9]+)\\s+"+
                        "([NSEW])?\\s?([0-9]+)[°\\s]+([0-9]+)[.\\s]+([0-9]+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(input);

        String latMinStr = "0";
        String lonMinStr = "0";

        if(m.matches()){
            if(m.group(1) != null)
                latDir = m.group(1);
            if(m.group(2) != null)
                latDeg = parseDouble(m.group(2));
            if(m.group(3) != null) {
                latMinStr = m.group(3);
                latMin = parseDouble(m.group(3));
            }
            if(m.group(4) != null) {
                latMin = parseDouble(latMinStr + "." + m.group(4));
            }
            if(m.group(5) != null)
                lonDir = m.group(5);
            if(m.group(6) != null)
                lonDeg = parseDouble(m.group(6));
            if(m.group(7) != null) {
                lonMinStr = m.group(7);
                lonMin = parseDouble(m.group(7));
            }
            if(m.group(8) != null) {
                lonMin  = parseDouble(lonMinStr + "." + m.group(8));
            }
            if(latDeg != null && latMin != null) {
                lat = DM2Decimal(latDeg, latMin, latDir);
                lon = DM2Decimal(lonDeg, lonMin, lonDir);

                return true;
            } else {// if (latDeg != null && latMin == null){

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

        } //else  {// bad input format }

        return false;
    }

    private Boolean parseLatitude(String input) {
        input = input.trim();

        Pattern p = Pattern.compile(
                "([NS])?\\s?([0-9]+)[°\\s]+([0-9]+)[.\\s]?([0-9]*)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(input);
        String latMinStr = "0";

        if(m.matches()){
            if(m.group(1) != null)
                latDir = m.group(1);
            if(m.group(2) != null)
                latDeg = parseDouble(m.group(2));
            if(m.group(3) != null){
                latMinStr = m.group(3);
                latMin = parseDouble(m.group(3));
                if(latMinStr.length() > 2) latMin /= 1000;
            }
            if(m.group(4) != null && !m.group(4).equals("")) {
                latMin = parseDouble(latMinStr + "." + m.group(4));
            }

            if(latDeg != null && latMin != null) {
                lat = DM2Decimal(latDeg, latMin, latDir);
                return true;
            } else {//if (latDeg != null && latMin == null){

                lat = latDeg;
                if(
                        latDir.equalsIgnoreCase("S")
                                ||
                                latDir.equalsIgnoreCase("-")
                ) lat = -lat;

                return true;
            }

        } //else  {
            // bad input format
       // }

        return false;
    }

    private Boolean parseLongitude(String input) {
        input = input.trim();

        Pattern p = Pattern.compile(
                "([EW])?\\s?([0-9]+)[°\\s]+([0-9]+)[.\\s]?([0-9]*)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m = p.matcher(input);
        String lonMinStr = "0";

        if(m.matches()){
            if(m.group(1) != null)
                lonDir = m.group(1);
            if(m.group(2) != null)
                lonDeg = parseDouble(m.group(2));
            if(m.group(3) != null){
                lonMinStr = m.group(3);
                lonMin = parseDouble(m.group(3));
                if(lonMinStr.length() > 2) lonMin /= 1000;
            }
            if(m.group(4) != null && !m.group(4).equals("")){
                lonMin = parseDouble(lonMinStr + "." + m.group(4));
            }

            if(lonDeg != null && lonMin != null) {
                lon = DM2Decimal(lonDeg, lonMin, lonDir);

                return true;
            } else {// if (lonDeg != null && lonMin == null){

                lon = lonDeg;
                if(
                        lonDir.equalsIgnoreCase("W")
                                ||
                                lonDir.equalsIgnoreCase("-")
                ) lon = -lon;

                return true;
            }

        } //else  {
            // bad input format
     //   }

        return false;
    }

    private Double parseDouble(String numberStr){
        try {
            return Double.parseDouble(numberStr);
        } catch (Exception e){
            return 0.0;
        }
    }

    double getLatitude() {

        return this.lat;
    }

    double getLongitude() {

        return this.lon;
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

    public void Offset(double angle, double distanceInMeters){

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

        System.out.println(x);
        System.out.println(y);

        double[] latVals = Decimal2DM(x);
        double[] lonVals = Decimal2DM(y);

        this.lat = x;
        this.lon = y;

        this.latDeg = latVals[0];
        this.latMin = latVals[1];

        this.lonDeg = lonVals[0];
        this.lonMin = lonVals[1];

    }

    String getFullCoordinates() {

        String latDegInt = StringUtils.leftPad(Integer.toString(latDeg.intValue()), 2);
        @SuppressLint("DefaultLocale") String latMinStr = String.format("%.3f", latMin);
        latMinStr = StringUtils.leftPad(latMinStr, 6, "0");
        String latitude = latDir + latDegInt + "°" + " " + latMinStr;

        String lonDegInt = StringUtils.leftPad(Integer.toString(lonDeg.intValue()), 2);
        @SuppressLint("DefaultLocale") String lonMinStr = String.format("%.3f", lonMin);
        lonMinStr = StringUtils.leftPad(lonMinStr, 6, "0");
        String longitude = lonDir + lonDegInt + "°" + " " + lonMinStr;

        return latitude + " " + longitude;
    }
}

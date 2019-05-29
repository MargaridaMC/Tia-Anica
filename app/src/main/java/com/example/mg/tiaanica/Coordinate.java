package com.example.mg.tiaanica;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class Coordinate {

    Coordinate coordinate;
    String originalCoord = "";
    private Map<String, String> latitude = new HashMap<String, String>();
    private Map<String, String> longitude = new HashMap<String, String>();
    private Map<String, String> originalLatitude = new HashMap<String, String>();
    private Map<String, String> originalLongitude = new HashMap<String, String>();

    String[] fields = {"CardinalDirection", "Degrees", "Minutes", "MinuteDecimals"};

    List<String> neededLetters;
    static Map<String, Integer> variables;
    boolean fullCoordinates = false;

    public Coordinate(String lat, String lon) {

        for(String f: fields) {
            latitude.put(f, "");
            longitude.put(f, "");
            originalLatitude.put(f, "");
            originalLongitude.put(f, "");
        }

        lat = lat.toUpperCase();
        lon = lon.toUpperCase();
        lat = lat.replaceAll(" ", "");
        lon = lon.replaceAll(" ", "");

        // Interpret Latitude
        Matcher m = Pattern.compile("([NS])(.*?)°(.*)").matcher(lat);

        if(m.find()) {

            fullCoordinates = true;
            originalLatitude.put("CardinalDirection", m.group(1));
            originalLatitude.put("Degrees", m.group(2));

            String minutes = m.group(3);
            Matcher minMatcher = Pattern.compile("(.*?)\\.(.*)").matcher(minutes);
            if(minMatcher.find()) {
                originalLatitude.put("Minutes", minMatcher.group(1));
                originalLatitude.put("MinuteDecimals", minMatcher.group(2));
            }

            else originalLatitude.put("Minutes", minutes);

        }

        // Interpret Longitude
        m = Pattern.compile("([WE])(.*?)°(.*)").matcher(lon);

        if(fullCoordinates && m.find()) { // that is if the latitude also matched the desired formating

            originalLongitude.put("CardinalDirection", m.group(1));
            originalLongitude.put("Degrees", m.group(2));

            String minutes = m.group(3);
            Matcher minMatcher = Pattern.compile("(.*?)\\.(.*)").matcher(minutes);
            if(minMatcher.find()) {
                originalLongitude.put("Minutes", minMatcher.group(1));
                originalLongitude.put("MinuteDecimals", minMatcher.group(2));
            }

            else originalLongitude.put("Minutes", minutes);

        }
        else fullCoordinates = false;

        if(!fullCoordinates) {
            System.out.println("WARNING: the input coordinates are not in the expected formatting");
            return;
        }

        latitude = originalLatitude;
        longitude = originalLongitude;

    }

    public Coordinate(String coord) {


        for(String f: fields) {
            latitude.put(f, "");
            longitude.put(f, "");
            originalLatitude.put(f, "");
            originalLongitude.put(f, "");
        }

        coord = coord.toUpperCase();
        coord = coord.replaceAll(" ", "");
        coord = coord.replaceAll("÷", "/");
        coord = coord.replaceAll("\\[", "(");
        coord = coord.replaceAll("\\]", ")");

        originalCoord = coord;

        // Check if input String matches a full set of coordinates
        Matcher matcher = Pattern.compile("([NS])(.*?)°(.*?)([EW])(.*?)°(.*)").matcher(originalCoord);
        if(matcher.find()) {

            fullCoordinates = true;

            // Read Latitude
            originalLatitude.put("CardinalDirection", matcher.group(1));
            originalLatitude.put("Degrees", tokenize(matcher.group(2)));

            String latMinutes = matcher.group(3);
            Matcher m = Pattern.compile("(.*?)\\.(.*)").matcher(latMinutes);
            if(m.find()) {
                originalLatitude.put("Minutes", tokenize(m.group(1)));
                originalLatitude.put("MinuteDecimals", tokenize(m.group(2)));
            }
            else originalLatitude.put("Minutes", tokenize(latMinutes));

            // Read Longitude
            originalLongitude.put("CardinalDirection", matcher.group(4));
            originalLongitude.put("Degrees", tokenize(matcher.group(5)));

            String lonMinutes = matcher.group(6);
            m = Pattern.compile("(.*?)\\.(.*)").matcher(lonMinutes);
            if(m.find()) {
                originalLongitude.put("Minutes", tokenize(m.group(1)));
                originalLongitude.put("MinuteDecimals", tokenize(m.group(2)));
            }
            else originalLongitude.put("Minutes", tokenize(lonMinutes));
        }
        //here we can allow the coordinates not to be in coordinate format;
        //in this case we just work directly with the full input string
        else {
            originalLatitude.put("Degrees", tokenize(originalCoord));
        }


        neededLetters = new ArrayList<String>();
        Pattern p = Pattern.compile("[A-Z]");
        Matcher m;

        // Look for letters in latitude
        for (Map.Entry<String, String> entry : originalLatitude.entrySet()) {

            if(!entry.getKey().matches("CardinalDirection")) {
                m = p.matcher(entry.getValue());
                while(m.find()) {
                    if (!neededLetters.contains(m.group()))
                        neededLetters.add(m.group());
                }

            }
        }

        // Look for letters in longitude
        for (Map.Entry<String, String> entry : originalLongitude.entrySet()) {

            if(!entry.getKey().matches("CardinalDirection")) {
                m = p.matcher(entry.getValue());

                while(m.find()) {
                    if (!neededLetters.contains(m.group()))
                        neededLetters.add(m.group());
                }

            }
        }

        Collections.sort(neededLetters);
        variables = new HashMap<>();

    }

    public void setVariables(Map<String, Integer> vars) {
        variables = vars;
    }

    public String getNeededVariables(){

        String neededLettersString = neededLetters.toString();
        return neededLettersString.substring(1, neededLettersString.length() - 1);

    }

    public Map<String, String> getLatitude() {
        return latitude;
    }

    public Map<String, String> getLongitude() {
        return longitude;
    }

    public Map<String, String> getOriginalLatitude() {
        return originalLatitude;
    }

    public Map<String, String> getOriginalLongitude() {
        return originalLongitude;
    }

    private void setValue(Map<String, String> coordinates,  String[] values, boolean pad) {

        setValue(coordinates, "Degrees", values[0], pad);
        setValue(coordinates, "Minutes", values[1], pad);
        setValue(coordinates, "MinuteDecimals", values[2], pad);
    }

    private void setValue(Map<String, String> coordinates, String key, String value, boolean pad) {

        if(pad) {
            int l = value.length();

            switch(key) {
                case "Degrees":
                    // The value for degrees should have at least two digits
                    coordinates.put("Degrees", StringUtils.leftPad(value, 2, '0'));
                    break;

                case "Minutes":
                    // The value for minutes should either have two digits (e.g. 08) or 5 (or six characters, including the decimals e.g. 08.473)
                    if(l > 2 && l <=5) coordinates.put("Minutes", StringUtils.leftPad(value, 5, '0'));
                    else coordinates.put("Minutes", StringUtils.leftPad(value, 2, '0'));
                    break;

                case "MinuteDecimals":
                    // The value for minute decimals should be zero (in case these are included in the minutes value) or 3
                    if(l > 0 && l <=3) coordinates.put("MinuteDecimals", StringUtils.leftPad(value, 3, '0'));
                    else coordinates.put("MinuteDecimals", value);
                    break;


            }
        }
        else coordinates.put(key, value);

    }

    private String tokenize(String coord){

        Matcher matcher = Pattern.compile("[A-Z]|[0-9]+").matcher(coord);

        while(matcher.find()) {
            String m = matcher.group(0);
            coord = coord.replaceAll(m, "(" + m + ")");
        }

        matcher = Pattern.compile("\\)([+-\\\\/\\\\*÷]){1}\\(").matcher(coord);

        while(matcher.find()) {
            String m = matcher.group(0);
            coord = coord.replace(m, m.substring(1,2));
        }

        return coord;
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public void evaluate(){

        latitude = originalLatitude;
        longitude = originalLongitude;


        for (Map.Entry<String, Integer> pair : variables.entrySet()) {
            String key = pair.getKey();
            String value = Integer.toString(pair.getValue());

            setValue(latitude, "Degrees", latitude.get("Degrees").replaceAll(key, value), false);
            setValue(latitude, "Minutes", latitude.get("Minutes").replaceAll(key, value), false);
            setValue(latitude, "MinuteDecimals", latitude.get("MinuteDecimals").replaceAll(key, value), false);
            setValue(longitude, "Degrees", longitude.get("Degrees").replaceAll(key, value), false);
            setValue(longitude, "Minutes", longitude.get("Minutes").replaceAll(key, value), false);
            setValue(longitude, "MinuteDecimals", longitude.get("MinuteDecimals").replaceAll(key, value), false);

        }


        Pattern token = Pattern.compile("\\(((?![\\)\\(]).)*\\)");
        Matcher matchedTokens;

        // evaluate latitude expression
        for(String key:latitude.keySet()) {

            if(!key.equals("CardinalDirection")) {

                String value = latitude.get(key);
                matchedTokens = token.matcher(value);

                while (matchedTokens.find()) {
                    String toEval = matchedTokens.group(0);
                    String replacement = Integer.toString((int) eval(toEval));
                    value = value.replace( toEval  , replacement);
                    matchedTokens = token.matcher(value);
                }

                setValue(latitude, key, value, true);

            }

        }

        //evaluate longitude expression
        for(String key:longitude.keySet()) {

            if(!key.equals("CardinalDirection")) {

                String value = longitude.get(key);
                matchedTokens = token.matcher(value);

                while (matchedTokens.find()) {
                    String toEval = matchedTokens.group(0);
                    String replacement = Integer.toString((int) coordinate.eval(toEval));
                    value = value.replace(toEval, replacement);
                    matchedTokens = token.matcher(value);
                }

                setValue(longitude, key, value, true);
            }
        }


    }

    public static double degreesMinutesToDegrees(Map<String, String> coordinates) {

        double degrees = Double.parseDouble(coordinates.get("Degrees"));
        double minutes = Double.parseDouble(coordinates.get("Minutes")) + Double.parseDouble(coordinates.get("MinuteDecimals")) / 1000;

        return degrees + minutes/60.0;

    }

    public static String[] degreesToDegreesMinutes(double coordinates) {

        int degrees = (int) coordinates;
        double minutes = (coordinates - degrees) * 60;
        double minuteDecimals;
        minutes = Math.round(minutes * 1000d) / 1000d;

        if(minutes==60.0) {
            degrees += 1;
            minutes = 0.0;
        }

        minuteDecimals = (minutes - (int) minutes) * 1000;

        String[] values = {Integer.toString(degrees), Integer.toString((int) minutes), Integer.toString((int) minuteDecimals)};

        return values;
    }

    public void Offset(double angle, double distanceInMeters){

        double X = degreesMinutesToDegrees(latitude);
        double Y = degreesMinutesToDegrees(longitude);

        double rad = Math.PI * angle / 180;

        double xRad = Math.PI * X / 180; // convert to radians
        double yRad = Math.PI * Y / 180;

        double R = 6378100; //Radius of the Earth in meters
        double x = Math.asin(Math.sin(xRad) * Math.cos(distanceInMeters/ R)
                + Math.cos(xRad) * Math.sin(distanceInMeters/ R) * Math.cos(rad));

        double y = yRad + Math.atan2(Math.sin(rad) * Math.sin(distanceInMeters/ R) * Math.cos(xRad), Math.cos(distanceInMeters/ R) - Math.sin(xRad) * Math.sin(x));

        x = x * 180 / Math.PI; // convert back to degrees
        y = y * 180 / Math.PI;

        String[] latVals = degreesToDegreesMinutes(x);
        String[] lonVals = degreesToDegreesMinutes(y);

        setValue(latitude, latVals, true);
        setValue(longitude, lonVals, true);
    }

    String getFinalCoordinates() {

        if(fullCoordinates){

        String latMinutes = latitude.get("Minutes");
        int l = latMinutes.length();
        if(l > 2) {
            latMinutes = latMinutes.substring(0,l - 3) + "." + latMinutes.substring(l-3);
        }
        else {
            latMinutes = latMinutes + "." + latitude.get("MinuteDecimals");

        }

        String lonMinutes = longitude.get("Minutes");
        l = lonMinutes.length();
        if(l > 2) {
            lonMinutes = lonMinutes.substring(0, l-3) + "." + lonMinutes.substring(l-3);
        }
        else {
            lonMinutes = lonMinutes + "." + longitude.get("MinuteDecimals");
        }

        return latitude.get("CardinalDirection") + " " + latitude.get("Degrees") + "° " + latMinutes + " " + longitude.get("CardinalDirection") + " " + longitude.get("Degrees") + "° " + lonMinutes;
        }

        else return latitude.get("Degrees");
    }
}

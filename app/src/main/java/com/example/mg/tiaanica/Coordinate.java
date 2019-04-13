package com.example.mg.tiaanica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coordinate {

    Coordinate coordinate;
    String originalCoord = "";
    String latitude = "";
    String longitude = "";
    String latitudeCardinalDirection = "";
    String longitudeCardinalDirection = "";

    List<String> neededLetters;
    static Map<String, Integer> variables;

    /*public Coordinate(String lat, String lon) {

        latitudeCardinalDirection = lat.substring(0,1);
        longitudeCardinalDirection = lon.substring(0,1);

        setLatitude(lat.substring(1));
        setLongitude(lon.substring(1));

    }
*/
    public Coordinate(String coord) {

        coord = coord.toUpperCase();
        coord = coord.replaceAll(" ", "");
        coord = coord.replaceAll("÷", "/");

        originalCoord = coord;
        separateCoordinates();

        neededLetters = new ArrayList<String>();

        Pattern p = Pattern.compile("[A-Z]");
        Matcher m = p.matcher(latitude);
        while (m.find()) {
            if (!neededLetters.contains(m.group()))
                neededLetters.add(m.group());
        }
        m = p.matcher(longitude);
        while (m.find())
            if(!neededLetters.contains(m.group()))
                neededLetters.add(m.group());

        Collections.sort(neededLetters);
        setLatitude(tokenize(latitude));
        setLongitude(tokenize(longitude));
        variables = new HashMap<>();

    }

    private void setLatitude(String lat) {
        latitude = lat;
    }

    private  void setLongitude(String lon) {
        longitude = lon;
    }

    public void setVariables(Map<String, Integer> vars) {
        variables = vars;
    }

    public String getNeededVariables(){

        String neededLettersString = neededLetters.toString();
        return neededLettersString.substring(1, neededLettersString.length() - 1);

    }

    private void separateCoordinates() {

        if(originalCoord.substring(0,1).matches("[NS]")) {
            latitudeCardinalDirection = originalCoord.substring(0,1);
            originalCoord = originalCoord.substring(1);
        }

        Matcher matcher = Pattern.compile("(.*?)E(\\d{2}°(.*))").matcher(originalCoord);
        while(matcher.find()) {

            setLatitude(matcher.group(1));
            setLongitude(matcher.group(2));
            longitudeCardinalDirection = "E";
        }

        if(latitude.equals("") && longitude.equals("")) {

            matcher = Pattern.compile("(.*?)W(\\d{2}°(.*))").matcher(originalCoord);
            while(matcher.find()) {
                setLatitude(matcher.group(1));
                setLongitude(matcher.group(2));
                longitudeCardinalDirection = "W";
            }

            if(latitude.equals("") && longitude.equals("")) setLatitude(originalCoord);
        }
    }

    private String tokenize(String coordinates) {

        StringBuilder tokenizedCoord = new StringBuilder();

        String[] parts = coordinates.split("");
        int maxLength = coordinates.length() + 1;

        int i = 0;
        String currentChar;
        String nextChar;
        String nextToNextChar;
        StringBuilder toAdd;
        int extraParenthesis = 0;

        while(i < maxLength) {

            currentChar = parts[i];
            toAdd = new StringBuilder();

            if(currentChar.equals("(")) {

                toAdd.append("(");

                i += 1;
                currentChar = parts[i];
                toAdd.append(currentChar);

                while(!currentChar.equals(")")){
                    i += 1;
                    currentChar = parts[i];
                    toAdd.append(currentChar);
                }
            }
            else if(currentChar.matches("[0-9\\.°]")){
                toAdd.append(currentChar);
            }
            else if(currentChar.matches("[A-Z]")) {

                toAdd.append("(");
                toAdd.append(currentChar);

                if(i + 2 < maxLength) {

                    nextChar = parts[i+1];
                    nextToNextChar = parts[i+2]; //Check for length

                    while(i + 2 < maxLength && nextChar.matches("[+\\-\\/\\*÷]") && nextToNextChar.matches("[A-Z0-9\\(\\)]")) {

                        toAdd.append(nextChar);
                        toAdd.append(nextToNextChar);

                        if(nextToNextChar.equals("(")) {
                            i += 1;
                            nextToNextChar = parts[i+2];
                            toAdd.append(nextToNextChar);
                            extraParenthesis += 1;
                        }

                        if(nextToNextChar.matches("[0-9]")) { // if it is a number with more than one digit we need to add them all

                            int j = i + 3;

                            if(j < maxLength) {

                                String theOneAfterThat;

                                while(j < maxLength) {
                                    theOneAfterThat = parts[j];
                                    if(theOneAfterThat.matches("[0-9]")) toAdd.append(theOneAfterThat);
                                    else break;
                                    j += 1;
                                }
                            }
                            i = j - 3;
                        }

                        i += 2;

                        if(i + 2 < maxLength) {
                            nextChar = parts[i+1];
                            nextToNextChar = parts[i+2];
                        }

                    }

                    for(int j = 0; j < extraParenthesis; j++) toAdd.append(")");

                }

                toAdd.append(")");
            }

            tokenizedCoord.append(toAdd);

            i += 1;
        }
        return tokenizedCoord.toString();

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

        for (Map.Entry<String, Integer> pair : variables.entrySet()) {
            String key = pair.getKey();
            String value = Integer.toString(pair.getValue());
            setLatitude(latitude.replaceAll(key, value));
            setLongitude(longitude.replaceAll(key, value));

        }


        Pattern token = Pattern.compile("\\(((?![\\)\\(]).)*\\)");
        Matcher latTokens = token.matcher(latitude);

        while (latTokens.find()) {
            String toEval = latTokens.group();
            setLatitude(latitude.replace(toEval, Integer.toString((int) eval(toEval))));
            latTokens = token.matcher(latitude);

        }

        Matcher lonTokens = token.matcher(longitude);
        while (lonTokens.find()) {
            String toEval = lonTokens.group();
            setLongitude(longitude.replace(toEval, Integer.toString((int) eval(toEval))));
            lonTokens = token.matcher(longitude);

        }
    }

    public static double degreesMinutesToDegrees(String coordinates) {

        double degrees = 0;
        double minutes = 0;
        coordinates = coordinates.replaceAll(" ", "");

        Matcher m = Pattern.compile("(.*?)°(.*)").matcher(coordinates);

        while(m.find()) {
            degrees = Double.parseDouble(m.group(1));
            minutes = Double.parseDouble(m.group(2));
        }

        return degrees + minutes/60.0;

    }

    public static String degreesToDegreesMinutes(double coordinates) {

        int degrees = (int) coordinates;
        double minutes = (coordinates - degrees) * 60;
        minutes = Math.round(minutes * 1000d) / 1000d;

        if(minutes==60.0) {
            degrees += 1;
            minutes = 0.0;
        }

        return degrees + "° " + minutes;
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

        setLatitude(degreesToDegreesMinutes(x));
        setLongitude(degreesToDegreesMinutes(y));
    }

    public String getFinalCoordinates() {

        return latitudeCardinalDirection + " " + latitude + "  " + longitudeCardinalDirection + " " + longitude;
    }
}
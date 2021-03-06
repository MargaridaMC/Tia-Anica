package com.example.mg.tiaanica;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CoordinateFormula {

    private String lat = "";
    private String lon = "";
    private String latDir = "";
    private String lonDir = "";

    List<String> neededLetters;
    private static Map<String, Double> variables;

    long Es;
    long Ws;

    boolean successfulParsing = true;
    private String returnStr;

    CoordinateFormula(String coord) {

        coord = coord.toUpperCase();
        coord = coord.trim();
        coord = coord.replaceAll("÷", "/");
        coord = coord.replaceAll("\\[", "(");
        coord = coord.replaceAll("]", ")");
        //coord = coord.replaceAll("\\{", "(");
        //coord = coord.replaceAll("}", ")");
        coord = coord.replaceAll("\n", " ");

        long Ns = StringUtils.countMatches(coord, "N");
        long Ss = StringUtils.countMatches(coord, "S");
        Es = StringUtils.countMatches(coord, "E");
        Ws = StringUtils.countMatches(coord, "W");

        if(coord.substring(0, 1).equals("N") || coord.substring(0, 1).equals("S"))  {

            Pattern p;
            if(Ns >= 1) {
                // we are definitely in the north
                latDir = "N";
                if(Es == 1) {
                    // we are to the east
                    lonDir = "E";
                    p = Pattern.compile("N(.*?)E(.*)", Pattern.CASE_INSENSITIVE);
                }
                else if(Ws == 1) {
                    // we are to the west
                    lonDir = "W";
                    p = Pattern.compile("N(.*?)E(.*)", Pattern.CASE_INSENSITIVE);
                }
                else {
                    //either E or W occur more than once
                    System.out.println("The cardinal direction of the latitude (E or W) shows up in the formula. Please replace theses instances with another letter");
                    successfulParsing = false;
                    return;
                }
            }
            else if(Ss >= 1) {
                // We are south
                latDir = "S";
                if(Es == 1) {
                    // we are to the east
                    lonDir = "E";
                    p = Pattern.compile("S(.*?)E(.*)", Pattern.CASE_INSENSITIVE);
                }
                else if(Ws == 1) {
                    // we are to the west
                    lonDir = "W";
                    p = Pattern.compile("S(.*?)W(.*)", Pattern.CASE_INSENSITIVE);
                }
                else {
                    //either E or W occur more than once
                    System.out.println("The cardinal direction of the latitude (E or W) shows up in the formula. Please replace theses instances with another letter");
                    successfulParsing = false;
                    return;
                }

            } else {
                // Either S or N occur more than once
                System.out.println("The cardinal direction of the longitude (N or S) shows up in the formula. Please replace theses instances with another letter");
                return;
            }


            Matcher m = p.matcher(coord);

            if(m.find()) {
                this.lat = m.group(1);
                this.lon = m.group(2);
            }

        }
        else {
            // Assume this is a formula for something other than "proper" coordinates
            this.lat = coord;
        }


        neededLetters = new ArrayList<>();
        Pattern letterPattern = Pattern.compile("[A-Z]");
        Matcher letterMatcher;

        // Look for letters in latitude
        letterMatcher = letterPattern.matcher(lat);
        while(letterMatcher.find()) {
            if (!neededLetters.contains(letterMatcher.group()))
                neededLetters.add(letterMatcher.group());
        }

        letterMatcher = letterPattern.matcher(lon);
        while(letterMatcher.find()) {
            if (!neededLetters.contains(letterMatcher.group()))
                neededLetters.add(letterMatcher.group());
        }

        Collections.sort(neededLetters);
        variables = new HashMap<>();

    }

    void setVariables(Map<String, Double> vars) {
        variables = vars;
    }

    String getNeededVariables(){
        if(neededLetters == null) return null;
        String neededLettersString = neededLetters.toString();
        return neededLettersString.substring(1, neededLettersString.length() - 1);

    }

    String getLatitude() {
        return lat;
    }

    String getLongitude() {
        return lon;
    }

    String getLatDir(){
        return latDir;
    }

    String getLonDir(){
        return lonDir;
    }

    private static double eval(final String str) {
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
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        default:
                            throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    void evaluate(){

        for (Map.Entry<String, Double> pair : variables.entrySet()) {
            String key = pair.getKey();
            double value = pair.getValue();
            String valueStr;
            if(value % 1 == 0){
                // value is an int
                valueStr = Integer.toString((int) value);
            } else { // value is double
                valueStr = Double.toString(pair.getValue());
            }

            lat = lat.replace(key,  valueStr);
            lon = lon.replace(key,  valueStr);
        }

        // Look for sections within parenthesis
        Pattern sectionPattern = Pattern.compile("(\\([0-9+\\-/*\\^\\s\\.]+\\))");
        Matcher sectionMatcher = sectionPattern.matcher(lat);

        while (sectionMatcher.find()) {
            String group = sectionMatcher.group(1);
            double result = eval(group);
            if(result % 1 == 0){
                lat = lat.replace(group, Integer.toString((int) result));
            } else {
                lat = lat.replace(group, Double.toString(result));
            }
            sectionMatcher = sectionPattern.matcher(lat);
        }

        sectionMatcher = sectionPattern.matcher(lon);

        while (sectionMatcher.find()) {
            String group = sectionMatcher.group(1);
            double result = eval(group);
            if(result % 1 == 0){
                lon = lon.replace(group, Integer.toString((int) result));
            } else {
                lon = lon.replace(group, Double.toString(result));
            }
            sectionMatcher = sectionPattern.matcher(lon);
        }

        // Check if there are still sections with operation signs (+, -, /, *)
        Pattern operationPattern = Pattern.compile("([\\d\\.]+\\s*([+\\-/*\\^]+\\s*[\\s\\d\\.]+)+)");
        Matcher operationMatcher = operationPattern.matcher(lat);
        while(operationMatcher.find()) {
            String group = operationMatcher.group(0);
            double result = eval(group);
            if(result % 1 == 0){
                lat = lat.replace(group, Integer.toString((int) result));
            } else {
                lat = lat.replace(group, Double.toString(result));
            }

        }

        operationMatcher = operationPattern.matcher(lon);
        while(operationMatcher.find()) {
            String group = operationMatcher.group(0);
            double result = eval(group);
            if(result % 1 == 0){
                lon = lon.replace(group, Integer.toString((int) result));
            } else {
                lon = lon.replace(group, Double.toString(result));
            }
        }

        returnStr = this.latDir + this.lat + " " + this.lonDir + this.lon;
    }

    boolean resultAreProperCoordinates(){

        try{

            Coordinate coordinate = new Coordinate(this.latDir + this.lat, this.lonDir + this.lon);
            returnStr = coordinate.getFullCoordinates();

        } catch(Exception e) {
            //returnStr = this.latDir + this.lat + " " + this.lonDir + this.lon;
            return false;
        }

        return true;

    }

    String getFullCoordinates() {

        // TODO: check if anything is wrong with the output coordinates
        // Check if any value is negative
        //resultAreProperCoordinates();
        return returnStr;
    }

}
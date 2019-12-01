package com.example.mg.tiaanica;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoordinateFormula {

    private String lat = "";
    private String lon = "";
    private String latDir = "N";
    private String lonDir = "E";

    List<String> neededLetters;
    private static Map<String, Integer> variables;

    CoordinateFormula(String coord) {

        coord = coord.toUpperCase();
        coord = coord.trim();
        coord = coord.replaceAll("÷", "/");
        coord = coord.replaceAll("\\[", "(");
        coord = coord.replaceAll("\\]", ")");

        long Ns = StringUtils.countMatches(coord, "N");
        long Ss = StringUtils.countMatches(coord, "S");
        long Es = StringUtils.countMatches(coord, "E");
        long Ws = StringUtils.countMatches(coord, "W");

        if(coord.substring(0, 1).equals("N") || coord.substring(0, 1).equals("S"))  {

            Pattern p;
            if(Ns == 1) {
                // we are definitely in the north
                if(Es == 1) {
                    // we are to the east
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
                    return;
                }
            }
            else if(Ss == 1) {
                // We are south
                latDir = "S";
                if(Es == 1) {
                    // we are to the east
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

    void setVariables(Map<String, Integer> vars) {
        variables = vars;
    }

    String getNeededVariables(){

        String neededLettersString = neededLetters.toString();
        return neededLettersString.substring(1, neededLettersString.length() - 1);

    }

    public String getLatitude() {
        return lat;
    }

    public String getLongitude() {
        return lon;
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

        for (Map.Entry<String, Integer> pair : variables.entrySet()) {
            String key = pair.getKey();
            String value = Integer.toString(pair.getValue());

            lat = lat.replace(key,  value);
            lon = lon.replace(key,  value);
        }

        // Look for sections within parenthesis
        Pattern sectionPattern = Pattern.compile("(\\([0-9+\\-/*\\s]+\\))");
        Matcher sectionMatcher = sectionPattern.matcher(lat);

        while (sectionMatcher.find()) {
            String group = sectionMatcher.group(1);
            lat = lat.replace(group, Integer.toString((int)eval(group)));
            sectionMatcher = sectionPattern.matcher(lat);
        }

        sectionMatcher = sectionPattern.matcher(lon);

        while (sectionMatcher.find()) {
            String group = sectionMatcher.group(1);
            lon = lon.replace(group, Integer.toString((int)eval(group)));
            sectionMatcher = sectionPattern.matcher(lon);
        }

        // Check if there are still sections with operation signs (+, -, /, *)
        Pattern operationPattern = Pattern.compile("([\\d]+([+\\-/*\\s]+[\\s\\d]+)+)");
        Matcher operationMatcher = operationPattern.matcher(lat);
        while(operationMatcher.find()) {
            String group = operationMatcher.group(0);
            lat = lat.replace(group, Integer.toString((int)eval(group)));
        }
    }

    String getFullCoordinates() {
        Coordinate coordinate = new Coordinate(this.latDir + this.lat + " " + this.lonDir + this.lon);
        return coordinate.getFullCoordinates();
    }
}